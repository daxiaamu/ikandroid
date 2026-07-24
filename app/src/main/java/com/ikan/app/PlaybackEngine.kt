package com.ikan.app

import android.content.Context
import android.net.http.HttpEngine
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.annotation.OptIn
import androidx.media3.common.MimeTypes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.HttpEngineDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.ikan.app.model.PlayEpisode
import com.ikan.app.model.PlayLine
import com.ikan.app.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** App-wide playback networking and segment cache. */
@OptIn(UnstableApi::class)
class PlaybackEngine(context: Context) {
    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 iKan/1.0"
        private const val STOP_REASON_USER_PAUSED = 1
    }

    private val appContext = context.applicationContext
    private val dispatcher = Dispatcher().apply {
        maxRequests = 40
        maxRequestsPerHost = 32
    }
    private val httpClient = OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val httpEngineExecutor = Executors.newFixedThreadPool(4)
    private val downloadExecutor = AdaptiveDownloadExecutor(appContext)
    private val upstreamFactory: DataSource.Factory = createUpstreamFactory()
    private val databaseProvider = StandaloneDatabaseProvider(appContext)
    private val mediaCache = SimpleCache(
        File(appContext.filesDir, "offline-media"),
        NoOpCacheEvictor(),
        databaseProvider,
    )
    private val cacheFactory = CacheDataSource.Factory()
        .setCache(mediaCache)
        .setUpstreamDataSourceFactory(upstreamFactory)
        // Explicit downloads are persistent. Normal playback reads them but does not silently
        // turn every watched stream into an unbounded offline download.
        .setCacheWriteDataSinkFactory(null)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    private val _downloads = MutableStateFlow<List<CachedEpisode>>(emptyList())
    val downloads = _downloads.asStateFlow()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var pollingProgress = false
    private val downloadSpeedSamples = mutableMapOf<String, DownloadSpeedSample>()
    @Volatile private var playbackPlayer: ExoPlayer? = null
    @Volatile var backgroundSessionRequested: Boolean = false
        private set

    val downloadManager: DownloadManager by lazy {
        DownloadManager(
            appContext,
            databaseProvider,
            mediaCache,
            upstreamFactory,
            downloadExecutor,
        ).apply {
            maxParallelDownloads = 2
            minRetryCount = 4
            addListener(object : DownloadManager.Listener {
                override fun onInitialized(downloadManager: DownloadManager) {
                    refreshDownloads(downloadManager)
                    startProgressPolling(downloadManager)
                }

                override fun onDownloadChanged(
                    downloadManager: DownloadManager,
                    download: Download,
                    finalException: Exception?,
                ) {
                    refreshDownloads(downloadManager)
                    startProgressPolling(downloadManager)
                }

                override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                    refreshDownloads(downloadManager)
                }
            })
            resumeDownloads()
            refreshDownloads(this)
            startProgressPolling(this)
        }
    }

    fun obtainPlayer(): ExoPlayer = playbackPlayer ?: synchronized(this) {
        playbackPlayer ?: createPlayer().also { playbackPlayer = it }
    }

    fun setBackgroundSessionRequested(requested: Boolean) {
        backgroundSessionRequested = requested
    }

    fun stopPlayback() {
        playbackPlayer?.stop()
    }

    fun releasePlayerIfSessionInactive() {
        if (backgroundSessionRequested) return
        synchronized(this) {
            if (backgroundSessionRequested) return
            playbackPlayer?.release()
            playbackPlayer = null
        }
    }

    private fun createPlayer(): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15_000, // Keep enough media ready for unstable mobile networks.
                50_000,
                400,    // A seek is a user action: resume as soon as a safe buffer is ready.
                1_200,
            )
            // Nearby backward seeks can reuse memory instead of reopening the stream or disk cache.
            .setBackBuffer(30_000, false)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        return ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheFactory))
            .setLoadControl(loadControl)
            .build()
            .apply {
                // Seeking to the nearest keyframe avoids decoding a long span from an exact timestamp.
                setSeekParameters(SeekParameters.CLOSEST_SYNC)
                addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        downloadExecutor.setPlaybackState(
                            mediaItem?.localConfiguration?.uri?.toString(),
                            playbackState == Player.STATE_BUFFERING,
                        )
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        downloadExecutor.setPlaybackState(
                            currentMediaItem?.localConfiguration?.uri?.toString(),
                            playbackState == Player.STATE_BUFFERING,
                        )
                    }
                })
            }
    }

    fun enqueue(video: Video, line: PlayLine, episode: PlayEpisode) {
        downloadManager
        val metadata = JSONObject()
            .put("videoId", video.id)
            .put("title", video.title)
            .put("poster", video.poster)
            .put("lineId", line.id)
            .put("lineName", line.name)
            .put("episodeName", episode.name)
            .put("url", episode.url)
            .toString()
            .toByteArray(Charsets.UTF_8)
        val request = DownloadRequest.Builder(downloadId(video.id, episode.url), android.net.Uri.parse(episode.url))
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .setData(metadata)
            .build()
        DownloadService.sendAddDownload(
            appContext,
            MediaDownloadService::class.java,
            request,
            true,
        )
    }

    fun enqueueLine(video: Video, line: PlayLine) {
        line.episodes.forEach { enqueue(video, line, it) }
    }

    fun removeDownload(id: String) {
        downloadManager
        DownloadService.sendRemoveDownload(appContext, MediaDownloadService::class.java, id, true)
    }

    fun setDownloadPaused(id: String, paused: Boolean) {
        downloadManager
        DownloadService.sendSetStopReason(
            appContext,
            MediaDownloadService::class.java,
            id,
            if (paused) STOP_REASON_USER_PAUSED else Download.STOP_REASON_NONE,
            true,
        )
    }

    fun removeAllDownloads() {
        downloadManager
        DownloadService.sendRemoveAllDownloads(appContext, MediaDownloadService::class.java, true)
    }

    private fun refreshDownloads(manager: DownloadManager) {
        _downloads.value = runCatching {
            val current = manager.currentDownloads.associateBy { it.request.id }
            val transferringDownloads = current.values.filter {
                it.state == Download.STATE_DOWNLOADING ||
                    it.state == Download.STATE_RESTARTING
            }
            val nowMs = SystemClock.elapsedRealtime()
            val perDownloadSpeeds = sampleDownloadSpeeds(transferringDownloads, nowMs)
            downloadExecutor.observe(
                totalBytes = transferringDownloads.sumOf { it.bytesDownloaded },
                activeUrls = transferringDownloads.mapTo(mutableSetOf()) {
                    it.request.uri.toString()
                },
                nowMs = nowMs,
            )
            val performance = downloadExecutor.performance
            manager.downloadIndex.getDownloads().use { cursor ->
                buildList<Pair<CachedEpisode, Long>> {
                    while (cursor.moveToNext()) {
                        val indexed = cursor.download
                        val download = current[indexed.request.id] ?: indexed
                        val metadata = runCatching {
                            JSONObject(download.request.data.toString(Charsets.UTF_8))
                        }.getOrNull() ?: continue
                        add(
                            CachedEpisode(
                                id = download.request.id,
                                videoId = metadata.optString("videoId"),
                                title = metadata.optString("title"),
                                poster = metadata.optString("poster"),
                                lineId = metadata.optString("lineId"),
                                lineName = metadata.optString("lineName"),
                                episodeName = metadata.optString("episodeName"),
                                url = metadata.optString("url", download.request.uri.toString()),
                                state = download.state,
                                percent = download.percentDownloaded,
                                bytesDownloaded = download.bytesDownloaded,
                                contentLength = download.contentLength,
                                speedBytesPerSecond = perDownloadSpeeds[download.request.id] ?: 0L,
                                connections = performance.connections,
                            ) to download.updateTimeMs,
                        )
                    }
                }.sortedByDescending { it.second }.map { it.first }
            }
        }.getOrDefault(_downloads.value)
    }

    private fun sampleDownloadSpeeds(
        activeDownloads: List<Download>,
        nowMs: Long,
    ): Map<String, Long> {
        val activeIds = activeDownloads.mapTo(mutableSetOf()) { it.request.id }
        downloadSpeedSamples.keys.retainAll(activeIds)
        return activeDownloads.associate { download ->
            val previous = downloadSpeedSamples[download.request.id]
            val elapsed = previous?.let { nowMs - it.sampleTimeMs } ?: 0L
            val speed = if (
                previous != null &&
                elapsed >= 400L &&
                download.bytesDownloaded >= previous.bytesDownloaded
            ) {
                val instant =
                    (download.bytesDownloaded - previous.bytesDownloaded) * 1_000L / elapsed
                if (previous.smoothedBytesPerSecond == 0L) {
                    instant
                } else {
                    (previous.smoothedBytesPerSecond * 65L + instant * 35L) / 100L
                }
            } else {
                previous?.smoothedBytesPerSecond ?: 0L
            }
            if (previous == null || elapsed >= 400L) {
                downloadSpeedSamples[download.request.id] = DownloadSpeedSample(
                    bytesDownloaded = download.bytesDownloaded,
                    sampleTimeMs = nowMs,
                    smoothedBytesPerSecond = speed,
                )
            }
            download.request.id to speed
        }
    }

    private fun startProgressPolling(manager: DownloadManager) {
        if (pollingProgress) return
        pollingProgress = true
        mainHandler.post(object : Runnable {
            override fun run() {
                refreshDownloads(manager)
                val active = manager.currentDownloads.any {
                    it.state == Download.STATE_DOWNLOADING ||
                        it.state == Download.STATE_QUEUED ||
                        it.state == Download.STATE_RESTARTING
                }
                if (active) {
                    mainHandler.postDelayed(this, 1_000)
                } else {
                    pollingProgress = false
                }
            }
        })
    }

    private fun downloadId(videoId: String, url: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$videoId|$url".toByteArray())
            .joinToString("") { "%02x".format(it) }
        return "episode-$digest"
    }

    private fun createUpstreamFactory(): DataSource.Factory {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runCatching { createHttpEngineFactory() }.getOrNull()?.let { return it }
        }
        return OkHttpDataSource.Factory(httpClient).setUserAgent(USER_AGENT)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun createHttpEngineFactory(): DataSource.Factory {
        val metadataDirectory = File(appContext.cacheDir, "http-engine").apply { mkdirs() }
        val engine = HttpEngine.Builder(appContext)
            .setStoragePath(metadataDirectory.absolutePath)
            // Store only connection metadata. Media bytes remain in Media3's shared segment cache.
            .setEnableHttpCache(HttpEngine.Builder.HTTP_CACHE_DISK_NO_HTTP, 10L * 1024 * 1024)
            .setEnableHttp2(true)
            .setEnableQuic(true)
            .setUserAgent(USER_AGENT)
            .build()
        return HttpEngineDataSource.Factory(engine, httpEngineExecutor)
            .setConnectionTimeoutMs(8_000)
            .setReadTimeoutMs(20_000)
            .setUserAgent(USER_AGENT)
    }

    private data class DownloadSpeedSample(
        val bytesDownloaded: Long,
        val sampleTimeMs: Long,
        val smoothedBytesPerSecond: Long,
    )
}
