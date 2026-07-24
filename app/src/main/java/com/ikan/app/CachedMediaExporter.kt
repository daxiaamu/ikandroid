package com.ikan.app

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultDecoderFactory
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ExoPlayerAssetLoader
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.Executors

enum class MediaExportStage {
    Converting,
    Saving,
}

data class MediaExportState(
    val itemId: String,
    val progress: Int,
    val stage: MediaExportStage,
)

@OptIn(UnstableApi::class)
class CachedMediaExporter(
    context: Context,
    private val mediaSourceFactory: MediaSource.Factory,
) {
    private val appContext = context.applicationContext
    private val copyExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val _state = MutableStateFlow<MediaExportState?>(null)
    val state = _state.asStateFlow()
    private var activeTransformer: Transformer? = null

    fun export(
        item: CachedEpisode,
        onCompleted: (String) -> Unit,
        onError: (String) -> Unit,
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            onError("导出 MP4 需要 Android 10 或更高版本")
            return false
        }
        if (!item.completed) {
            onError("请等待缓存完成后再导出")
            return false
        }
        if (activeTransformer != null) {
            onError("已有视频正在导出，请稍后再试")
            return false
        }

        val exportDirectory = File(appContext.cacheDir, "media-exports").apply { mkdirs() }
        val temporaryFile = File(exportDirectory, "${item.id.hashCode().toUInt()}.mp4")
        if (temporaryFile.exists() && !temporaryFile.delete()) {
            onError("无法创建导出文件")
            return false
        }

        val transformer = Transformer.Builder(appContext)
            .setAssetLoaderFactory(
                ExoPlayerAssetLoader.Factory(
                    appContext,
                    DefaultDecoderFactory.Builder(appContext)
                        .setEnableDecoderFallback(true)
                        .build(),
                    Clock.DEFAULT,
                    mediaSourceFactory,
                ),
            )
            .addListener(object : Transformer.Listener {
                override fun onCompleted(
                    composition: Composition,
                    exportResult: ExportResult,
                ) {
                    _state.value = MediaExportState(item.id, 90, MediaExportStage.Saving)
                    copyExecutor.execute {
                        runCatching {
                            publishToMediaStore(item, temporaryFile) { copyProgress ->
                                _state.value = MediaExportState(
                                    item.id,
                                    90 + copyProgress.coerceIn(0, 100) / 10,
                                    MediaExportStage.Saving,
                                )
                            }
                        }.onSuccess { displayPath ->
                            temporaryFile.delete()
                            appContext.mainExecutor.execute {
                                activeTransformer = null
                                _state.value = null
                                onCompleted(displayPath)
                            }
                        }.onFailure { error ->
                            temporaryFile.delete()
                            appContext.mainExecutor.execute {
                                activeTransformer = null
                                _state.value = null
                                onError(error.message ?: "导出失败")
                            }
                        }
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException,
                ) {
                    temporaryFile.delete()
                    activeTransformer = null
                    _state.value = null
                    onError(exportException.message ?: "该视频暂不支持导出 MP4")
                }
            })
            .build()

        activeTransformer = transformer
        _state.value = MediaExportState(item.id, 0, MediaExportStage.Converting)
        return runCatching {
            transformer.start(
                MediaItem.Builder()
                    .setUri(item.url)
                    .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
                    .build(),
                temporaryFile.absolutePath,
            )
            pollProgress(transformer, item.id)
            true
        }.getOrElse { error ->
            temporaryFile.delete()
            activeTransformer = null
            _state.value = null
            onError(error.message ?: "无法开始导出")
            false
        }
    }

    private fun pollProgress(transformer: Transformer, itemId: String) {
        val holder = ProgressHolder()
        mainHandler.post(object : Runnable {
            override fun run() {
                if (
                    activeTransformer !== transformer ||
                    _state.value?.stage != MediaExportStage.Converting
                ) {
                    return
                }
                if (transformer.getProgress(holder) == Transformer.PROGRESS_STATE_AVAILABLE) {
                    _state.value = MediaExportState(
                        itemId,
                        holder.progress.coerceIn(0, 100) * 90 / 100,
                        MediaExportStage.Converting,
                    )
                }
                mainHandler.postDelayed(this, 250)
            }
        })
    }

    private fun publishToMediaStore(
        item: CachedEpisode,
        source: File,
        onProgress: (Int) -> Unit,
    ): String {
        val resolver = appContext.contentResolver
        val displayName = buildExportName(item)
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(
                MediaStore.Video.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOWNLOADS}/ikandroid",
            )
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY,
        )
        val uri = checkNotNull(resolver.insert(collection, values)) {
            "无法在系统影片目录中创建文件"
        }
        return try {
            resolver.openOutputStream(uri, "w")!!.use { output ->
                source.inputStream().buffered(1024 * 1024).use { input ->
                    val buffer = ByteArray(1024 * 1024)
                    val totalBytes = source.length().coerceAtLeast(1L)
                    var copiedBytes = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        copiedBytes += count
                        onProgress((copiedBytes * 100 / totalBytes).toInt())
                    }
                }
            }
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) },
                null,
                null,
            )
            "Download/ikandroid/$displayName"
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    private fun buildExportName(item: CachedEpisode): String {
        val rawName = listOf(item.title, item.episodeName)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        val safeName = rawName
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .trim()
            .take(120)
            .ifBlank { "爱看视频" }
        return "$safeName.mp4"
    }
}
