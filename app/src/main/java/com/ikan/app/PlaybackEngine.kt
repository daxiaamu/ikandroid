package com.ikan.app

import android.content.Context
import android.net.http.HttpEngine
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.HttpEngineDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** App-wide playback networking and segment cache. */
class PlaybackEngine(context: Context) {
    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 iKan/1.0"
    }

    private val appContext = context.applicationContext
    private val dispatcher = Dispatcher().apply {
        maxRequests = 12
        maxRequestsPerHost = 6
    }
    private val httpClient = OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val httpEngineExecutor = Executors.newFixedThreadPool(4)
    private val upstreamFactory: DataSource.Factory = createUpstreamFactory()
    private val mediaCache = SimpleCache(
        File(appContext.cacheDir, "media-segments"),
        LeastRecentlyUsedCacheEvictor(256L * 1024 * 1024),
        StandaloneDatabaseProvider(appContext),
    )
    private val cacheFactory = CacheDataSource.Factory()
        .setCache(mediaCache)
        .setUpstreamDataSourceFactory(upstreamFactory)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    fun createPlayer(): ExoPlayer {
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
            }
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
}
