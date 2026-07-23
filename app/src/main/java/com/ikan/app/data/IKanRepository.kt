package com.ikan.app.data

import com.ikan.app.model.HomeCategory
import com.ikan.app.model.Video

class IKanRepository(
    private val remote: IkanClient,
    private val dao: LibraryDao,
) {
    val favorites = dao.favorites()
    val history = dao.history()

    suspend fun catalog(category: HomeCategory) = remote.catalog(category)
    suspend fun catalog(path: String, title: String) = remote.catalog(path, title)
    suspend fun search(query: String) = remote.search(query)
    suspend fun detail(id: String) = remote.detail(id)
    suspend fun library(id: String) = dao.find(id)

    suspend fun toggleFavorite(video: Video): Boolean {
        val old = dao.find(video.id)
        val value = !(old?.favorite ?: false)
        dao.upsert(
            (old ?: LibraryEntity(video.id, video.title, video.poster)).copy(
                title = video.title,
                poster = video.poster,
                favorite = value,
            ),
        )
        dao.pruneEmpty()
        return value
    }

    suspend fun recordPlayback(
        video: Video,
        lineId: String,
        episodeName: String,
        streamUrl: String,
        positionMs: Long,
        durationMs: Long,
    ) {
        val old = dao.find(video.id)
        dao.upsert(
            (old ?: LibraryEntity(video.id, video.title, video.poster)).copy(
                title = video.title,
                poster = video.poster,
                playedAt = System.currentTimeMillis(),
                positionMs = positionMs.coerceAtLeast(0),
                durationMs = durationMs.coerceAtLeast(0),
                lineId = lineId,
                episodeName = episodeName,
                streamUrl = streamUrl,
            ),
        )
    }

    suspend fun clearHistory() {
        dao.clearHistory()
        dao.pruneEmpty()
    }
}
