package com.ikan.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

data class DownloadPerformance(
    val bytesPerSecond: Long = 0,
    val connections: Int = 2,
)

/**
 * Executes independent HLS segment downloads in parallel and adjusts concurrency according to
 * measured aggregate throughput. Current-video caching gets priority while player buffering;
 * unrelated background downloads yield their network slots to playback.
 */
class AdaptiveDownloadExecutor(context: Context) : Executor {
    private val connectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)
    private val threadNumber = AtomicInteger()
    private val executor = ThreadPoolExecutor(
        MIN_CONNECTIONS,
        MAX_CONNECTIONS,
        10,
        TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        ThreadFactory { task ->
            Thread(task, "ikan-hls-${threadNumber.incrementAndGet()}").apply {
                priority = Thread.NORM_PRIORITY
            }
        },
    ).apply {
        allowCoreThreadTimeOut(true)
    }
    private val controller = AdaptiveConcurrencyController()

    @Volatile
    private var playingUrl: String? = null

    @Volatile
    private var playerBuffering = false

    @Volatile
    var performance = DownloadPerformance()
        private set

    private var lastBytes = 0L
    private var lastSampleTimeMs = 0L
    private var smoothedBytesPerSecond = 0L

    override fun execute(command: Runnable) {
        executor.execute(command)
    }

    fun setPlaybackState(url: String?, buffering: Boolean) {
        playingUrl = url
        playerBuffering = buffering
    }

    @Synchronized
    fun observe(totalBytes: Long, activeUrls: Set<String>, nowMs: Long = SystemClock.elapsedRealtime()) {
        val active = activeUrls.isNotEmpty()
        val elapsed = nowMs - lastSampleTimeMs
        if (active && lastSampleTimeMs > 0L && elapsed >= 400L && totalBytes >= lastBytes) {
            val instant = (totalBytes - lastBytes) * 1_000L / elapsed
            smoothedBytesPerSecond = if (smoothedBytesPerSecond == 0L) {
                instant
            } else {
                (smoothedBytesPerSecond * 65L + instant * 35L) / 100L
            }
        } else if (!active) {
            smoothedBytesPerSecond = 0L
        }
        lastBytes = totalBytes
        lastSampleTimeMs = nowMs

        val currentUrl = playingUrl
        val playbackPriority = when {
            !playerBuffering -> PlaybackPriority.NONE
            currentUrl != null && currentUrl in activeUrls -> PlaybackPriority.CURRENT_DOWNLOAD
            else -> PlaybackPriority.FOREGROUND_STREAM
        }
        val connections = controller.update(
            nowMs = nowMs,
            bytesPerSecond = smoothedBytesPerSecond,
            active = active,
            maximum = networkMaximum(),
            playbackPriority = playbackPriority,
        )
        resize(connections)
        performance = DownloadPerformance(smoothedBytesPerSecond, connections)
    }

    private fun resize(size: Int) {
        if (executor.corePoolSize == size) return
        executor.corePoolSize = size
        if (size > executor.poolSize) executor.prestartAllCoreThreads()
    }

    private fun networkMaximum(): Int {
        val capabilities = connectivityManager
            .getNetworkCapabilities(connectivityManager.activeNetwork)
            ?: return 3
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> 32
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 8
            else -> 4
        }
    }

    companion object {
        private const val MIN_CONNECTIONS = 2
        private const val MAX_CONNECTIONS = 32
    }
}

internal enum class PlaybackPriority {
    NONE,
    CURRENT_DOWNLOAD,
    FOREGROUND_STREAM,
}

/**
 * Conservative hill-climbing controller inspired by mudl-c: probe one extra worker, retain it
 * when aggregate throughput improves, and roll it back when the connection becomes slower.
 */
internal class AdaptiveConcurrencyController {
    var connections: Int = 2
        private set

    private var lastEvaluationMs = 0L
    private var lastProbeMs = 0L
    private var probeBaseline = 0L
    private var probePreviousConnections = 2
    private var probing = false

    fun update(
        nowMs: Long,
        bytesPerSecond: Long,
        active: Boolean,
        maximum: Int,
        playbackPriority: PlaybackPriority,
    ): Int {
        val cap = maximum.coerceIn(2, 32)
        if (!active) {
            reset()
            return connections
        }
        if (playbackPriority == PlaybackPriority.FOREGROUND_STREAM) {
            connections = 1
            probing = false
            return connections
        }
        if (playbackPriority == PlaybackPriority.CURRENT_DOWNLOAD) {
            connections = cap
            probing = false
            lastProbeMs = nowMs
            return connections
        }

        connections = connections.coerceIn(2, cap)
        if (bytesPerSecond <= 0L || nowMs - lastEvaluationMs < EVALUATION_INTERVAL_MS) {
            return connections
        }
        lastEvaluationMs = nowMs

        if (probing) {
            when {
                bytesPerSecond < probeBaseline * 85L / 100L -> {
                    connections = max(2, probePreviousConnections)
                    probing = false
                    lastProbeMs = nowMs
                }
                bytesPerSecond >= probeBaseline * 103L / 100L && connections < cap -> {
                    probeBaseline = bytesPerSecond
                    probePreviousConnections = connections
                    connections = nextLevel(connections, cap)
                }
                else -> {
                    probing = false
                    lastProbeMs = nowMs
                }
            }
        } else if (connections < cap &&
            (lastProbeMs == 0L || nowMs - lastProbeMs >= REPROBE_INTERVAL_MS)
        ) {
            probeBaseline = bytesPerSecond
            probePreviousConnections = connections
            connections = nextLevel(connections, cap)
            probing = true
        }
        return connections
    }

    private fun nextLevel(current: Int, cap: Int): Int =
        CONNECTION_LEVELS.firstOrNull { it > current && it <= cap } ?: cap

    private fun reset() {
        connections = 2
        lastEvaluationMs = 0L
        lastProbeMs = 0L
        probeBaseline = 0L
        probePreviousConnections = 2
        probing = false
    }

    companion object {
        private const val EVALUATION_INTERVAL_MS = 2_500L
        private const val REPROBE_INTERVAL_MS = 8_000L
        private val CONNECTION_LEVELS = intArrayOf(2, 4, 8, 12, 16, 24, 32)
    }
}
