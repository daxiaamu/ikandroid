package com.ikan.app

import android.app.DownloadManager
import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val version: String,
    val name: String,
    val notes: String,
    val releaseUrl: String,
    val apkUrl: String,
)

class AppUpdater(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .build()
    private val updateSources = listOf(
        BuildConfig.UPDATE_JSON_URL,
        "https://cdn.jsdelivr.net/gh/daxiaamu/ikandroid@main/latest-release.json",
        "https://fastly.jsdelivr.net/gh/daxiaamu/ikandroid@main/latest-release.json",
        "https://gcore.jsdelivr.net/gh/daxiaamu/ikandroid@main/latest-release.json",
        "https://raw.githubusercontent.com/daxiaamu/ikandroid/main/latest-release.json",
    ).distinct()

    suspend fun check(): UpdateInfo = withContext(Dispatchers.IO) {
        var lastError: Throwable? = null
        for (source in updateSources) {
            try {
                return@withContext fetch(source)
            } catch (error: Throwable) {
                lastError = error
            }
        }
        throw IllegalStateException("暂时无法连接更新服务器，请稍后重试", lastError)
    }

    private fun fetch(source: String): UpdateInfo {
        val separator = if ('?' in source) '&' else '?'
        val request = Request.Builder()
            .url(source + separator + "t=" + System.currentTimeMillis())
            .header("Cache-Control", "no-cache")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "ikandroid/${BuildConfig.VERSION_NAME}")
            .build()
        val body = client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "检查更新失败（HTTP ${response.code}）" }
            response.body.string()
        }
        var json = JSONObject(body)
        if (json.optString("encoding").equals("base64", ignoreCase = true)) {
            val decoded = Base64.decode(json.getString("content"), Base64.DEFAULT)
            json = JSONObject(decoded.toString(Charsets.UTF_8))
        }
        val tag = json.optString("tag")
        val version = json.optString("versionName").ifBlank { tag.removePrefix("v") }
        check(version.isNotBlank()) { "仓库尚未发布版本" }
        return UpdateInfo(
            version = version,
            name = json.optString("name").ifBlank { tag },
            notes = json.optString("notes")
                .lineSequence()
                .map(String::trim)
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .joinToString("\n") { if (it.startsWith("- ")) "• ${it.drop(2)}" else it },
            releaseUrl = json.optString("releaseUrl"),
            apkUrl = json.optString("apkUrl"),
        )
    }

    fun isNewer(candidate: String, current: String = BuildConfig.VERSION_NAME): Boolean {
        fun parts(value: String) = value.removePrefix("v").split('.', '-', '_')
            .map { it.toIntOrNull() ?: 0 }
        val left = parts(candidate)
        val right = parts(current)
        repeat(maxOf(left.size, right.size)) { index ->
            val comparison = (left.getOrElse(index) { 0 }).compareTo(right.getOrElse(index) { 0 })
            if (comparison != 0) return comparison > 0
        }
        return false
    }

    fun download(info: UpdateInfo): Long {
        check(info.apkUrl.isNotBlank()) { "该 Release 没有 APK 附件" }
        val manager = context.getSystemService(DownloadManager::class.java)
        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("爱看")
            .setDescription("正在下载更新，完成后将提示安装")
            .setMimeType(APK_MIME)
            // Keep the system-owned notification only while downloading. On completion our
            // install notification takes over and can be cancelled immediately when opened.
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        // Let DownloadManager own the file and expose it through a content URI. Writing a
        // DownloadManager entry into Android/data is rejected by some Android 15/16 builds.
        return manager.enqueue(request).also { id ->
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(KEY_DOWNLOAD, id).apply()
        }
    }

    companion object {
        const val PREFS = "app_update"
        const val KEY_DOWNLOAD = "download_id"
        const val KEY_READY_DOWNLOAD = "ready_download_id"
        const val APK_MIME = "application/vnd.android.package-archive"
    }
}

class UpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val expected = context.getSharedPreferences(AppUpdater.PREFS, Context.MODE_PRIVATE)
            .getLong(AppUpdater.KEY_DOWNLOAD, -1L)
        val completed = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (expected < 0 || completed != expected) return
        if (!UpdateInstaller.isSuccessful(context, completed)) return

        UpdateInstaller.markReady(context, completed)
        UpdateInstaller.showInstallNotification(context, completed)
        if (UpdateInstaller.isAppForeground()) {
            UpdateInstallActivity.open(context, completed)
        }
    }
}

/**
 * Android 10+ blocks background broadcast receivers from opening activities. Keep a pending
 * install until the app is foreground again and always provide a user-initiated notification.
 */
object UpdateInstaller {
    private const val CHANNEL_ID = "app_update_install"
    private const val NOTIFICATION_ID = 0x494B

    fun markReady(context: Context, downloadId: Long) {
        context.getSharedPreferences(AppUpdater.PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(AppUpdater.KEY_READY_DOWNLOAD, downloadId)
            .apply()
    }

    fun pending(context: Context): Long =
        context.getSharedPreferences(AppUpdater.PREFS, Context.MODE_PRIVATE)
            .getLong(AppUpdater.KEY_READY_DOWNLOAD, -1L)

    fun pendingOrCompleted(context: Context): Long {
        pending(context).takeIf { it >= 0 }?.let { return it }
        val downloadId = context.getSharedPreferences(AppUpdater.PREFS, Context.MODE_PRIVATE)
            .getLong(AppUpdater.KEY_DOWNLOAD, -1L)
        if (downloadId >= 0 && isSuccessful(context, downloadId)) {
            markReady(context, downloadId)
            return downloadId
        }
        return -1L
    }

    fun isSuccessful(context: Context, downloadId: Long): Boolean {
        val manager = context.getSystemService(DownloadManager::class.java)
        return runCatching {
            manager.query(DownloadManager.Query().setFilterById(downloadId)).use { cursor ->
                cursor.moveToFirst() &&
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) ==
                    DownloadManager.STATUS_SUCCESSFUL &&
                    manager.getUriForDownloadedFile(downloadId) != null
            }
        }.getOrDefault(false)
    }

    fun clear(context: Context, downloadId: Long) {
        val preferences = context.getSharedPreferences(AppUpdater.PREFS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        if (preferences.getLong(AppUpdater.KEY_READY_DOWNLOAD, -1L) == downloadId) {
            editor.remove(AppUpdater.KEY_READY_DOWNLOAD)
        }
        if (preferences.getLong(AppUpdater.KEY_DOWNLOAD, -1L) == downloadId) {
            editor.remove(AppUpdater.KEY_DOWNLOAD)
        }
        editor.apply()
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    fun isAppForeground(): Boolean {
        val state = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(state)
        return state.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    fun showInstallNotification(context: Context, downloadId: Long) {
        val notifications = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifications.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "应用更新",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "新版本下载完成后的安装提示"
                },
            )
        }
        val action = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            UpdateInstallActivity.intent(context, downloadId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        notifications.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("爱看")
                .setContentText("更新已下载，点击安装")
                .setContentIntent(action)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM)
                .build(),
        )
    }

    fun launchInstaller(activity: Activity, downloadId: Long): Boolean {
        val manager = activity.getSystemService(DownloadManager::class.java)
        val uri = manager.getUriForDownloadedFile(downloadId) ?: return false
        return try {
            activity.startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, AppUpdater.APK_MIME)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            )
            clear(activity, downloadId)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}

class UpdateInstallActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, UpdateInstaller.pending(this))
        if (downloadId >= 0) {
            UpdateInstaller.launchInstaller(this, downloadId)
        }
        finish()
    }

    companion object {
        private const val EXTRA_DOWNLOAD_ID = "download_id"

        fun intent(context: Context, downloadId: Long): Intent =
            Intent(context, UpdateInstallActivity::class.java)
                .putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        fun open(context: Context, downloadId: Long) {
            context.startActivity(intent(context, downloadId))
        }
    }
}
