package com.ikan.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Base64
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
        val version = info.version.removePrefix("v")
        val fileName = "ikandroid-$version.apk"
        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("爱看")
            .setDescription("正在下载更新，完成后将打开安装界面")
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
