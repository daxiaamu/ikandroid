package com.ikan.app

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler

data class CachedEpisode(
    val id: String,
    val videoId: String,
    val title: String,
    val poster: String,
    val lineId: String,
    val lineName: String,
    val episodeName: String,
    val url: String,
    val state: Int,
    val percent: Float,
    val bytesDownloaded: Long,
    val contentLength: Long,
    val speedBytesPerSecond: Long = 0,
    val connections: Int = 0,
) {
    val completed: Boolean get() = state == Download.STATE_COMPLETED
    val downloading: Boolean get() = state == Download.STATE_DOWNLOADING
    val paused: Boolean get() = state == Download.STATE_STOPPED
}

@OptIn(UnstableApi::class)
class MediaDownloadService : DownloadService(
    NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.cache_channel_name,
    R.string.cache_channel_description,
) {
    private val notificationHelper by lazy { DownloadNotificationHelper(this, CHANNEL_ID) }

    override fun getDownloadManager(): DownloadManager =
        (application as IKanApplication).playbackEngine.downloadManager

    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int,
    ): Notification {
        val pendingIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(
                this,
                0,
                it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
        return notificationHelper.buildProgressNotification(
            this,
            android.R.drawable.stat_sys_download,
            pendingIntent,
            "正在缓存视频，可返回应用查看详情",
            downloads,
            notMetRequirements,
        )
    }

    companion object {
        const val CHANNEL_ID = "video_cache"
        const val NOTIFICATION_ID = 1202
    }
}
