package com.ikan.app.data

import com.ikan.app.model.HomeCategory
import com.ikan.app.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class IKanRepository(
    private val remote: IkanClient,
    daoProvider: () -> LibraryDao,
    private val homeCache: CatalogCache,
) {
    private val dao by lazy(daoProvider)
    val favorites: Flow<List<LibraryEntity>> = flow { emitAll(dao.favorites()) }
    val history: Flow<List<LibraryEntity>> = flow { emitAll(dao.history()) }

    suspend fun cachedHome() = homeCache.read()
    suspend fun catalog(category: HomeCategory) = remote.catalog(category).also { page ->
        if (category == HomeCategory.RECOMMEND) withContext(Dispatchers.IO) { homeCache.write(page) }
    }
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
