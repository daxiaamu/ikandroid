package com.ikan.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val version: String,
    val name: String,
    val releaseUrl: String,
    val apkUrl: String,
)

class AppUpdater(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun check(): UpdateInfo = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.UPDATE_JSON_URL + "?t=" + System.currentTimeMillis())
            .header("Cache-Control", "no-cache")
            .build()
        val body = client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "检查更新失败（HTTP ${response.code}）" }
            response.body.string()
        }
        val json = JSONObject(body)
        val tag = json.optString("tag")
        val version = json.optString("versionName").ifBlank { tag.removePrefix("v") }
        check(version.isNotBlank()) { "仓库尚未发布版本" }
        UpdateInfo(
            version = version,
            name = json.optString("name").ifBlank { tag },
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
        val fileName = "ikandroid-${info.version}.apk"
        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("爱看 ${info.version}")
            .setDescription("下载完成后将打开系统安装界面")
            .setMimeType(APK_MIME)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
        return manager.enqueue(request).also { id ->
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(KEY_DOWNLOAD, id).apply()
        }
    }

    companion object {
        const val PREFS = "app_update"
        const val KEY_DOWNLOAD = "download_id"
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
        val manager = context.getSystemService(DownloadManager::class.java)
        val uri = manager.getUriForDownloadedFile(completed) ?: return
        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, AppUpdater.APK_MIME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION),
        )
    }
}
