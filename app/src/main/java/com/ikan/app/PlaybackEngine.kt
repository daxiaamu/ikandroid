package com.ikan.app

import android.content.Context
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
import java.util.concurrent.TimeUnit

/** App-wide playback networking and segment cache. */
class PlaybackEngine(context: Context) {
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
    private val upstreamFactory = OkHttpDataSource.Factory(httpClient)
        .setUserAgent("Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 iKan/1.0")
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
                600,    // Start and resume quickly once a small playable buffer is ready.
                1_200,
            )
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
}
