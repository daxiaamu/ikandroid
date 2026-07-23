package com.ikan.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ikan.app.data.IKanRepository
import com.ikan.app.data.LibraryEntity
import com.ikan.app.model.CatalogPage
import com.ikan.app.model.HomeCategory
import com.ikan.app.model.PlayEpisode
import com.ikan.app.model.PlayLine
import com.ikan.app.model.ThemeMode
import com.ikan.app.model.VideoDetail
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

data class CatalogUiState(
    val loading: Boolean = true,
    val page: CatalogPage? = null,
    val error: String? = null,
)

data class DetailUiState(
    val loading: Boolean = false,
    val detail: VideoDetail? = null,
    val favorite: Boolean = false,
    val resume: LibraryEntity? = null,
    val error: String? = null,
)

class MainViewModel(private val app: IKanApplication) : ViewModel() {
    private val repository: IKanRepository = app.repository
    private var catalogJob: Job? = null
    private var detailJob: Job? = null

    private val _category = MutableStateFlow(HomeCategory.RECOMMEND)
    val category = _category.asStateFlow()
    private val _searching = MutableStateFlow(false)
    val searching = _searching.asStateFlow()
    private val _catalog = MutableStateFlow(CatalogUiState())
    val catalog = _catalog.asStateFlow()
    private val _detail = MutableStateFlow(DetailUiState())
    val detail = _detail.asStateFlow()

    val favorites: StateFlow<List<LibraryEntity>> = repository.favorites.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )
    val history: StateFlow<List<LibraryEntity>> = repository.history.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )
    val theme = app.preferences.theme.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM,
    )
    val searchHistory = app.preferences.searchHistory.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )
    val downloads get() = app.playbackEngine.downloads

    init { loadCategory(HomeCategory.RECOMMEND, useStartupPrefetch = true) }

    fun loadCategory(value: HomeCategory, useStartupPrefetch: Boolean = false) {
        _category.value = value
        _searching.value = false
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            _catalog.value = CatalogUiState(loading = true)
            val cached = if (useStartupPrefetch && value == HomeCategory.RECOMMEND) app.cachedHome.await() else null
            if (cached != null) _catalog.value = CatalogUiState(page = cached, loading = true)
            runCatching {
                if (useStartupPrefetch && value == HomeCategory.RECOMMEND) app.initialHome.await()
                else repository.catalog(value)
            }.onSuccess {
                _catalog.value = CatalogUiState(page = it, loading = false)
            }.onFailure {
                _catalog.value = cached?.let { page -> CatalogUiState(page = page, loading = false) }
                    ?: CatalogUiState(false, error = readable(it))
            }
        }
    }

    fun loadPath(path: String, title: String) {
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            _catalog.value = CatalogUiState(loading = true, page = _catalog.value.page)
            _catalog.value = runCatching { repository.catalog(path, title) }
                .fold({ CatalogUiState(page = it, loading = false) }, { CatalogUiState(false, error = readable(it)) })
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        _searching.value = true
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            app.preferences.addSearch(query)
            _catalog.value = CatalogUiState(loading = true)
            _catalog.value = runCatching { repository.search(query) }
                .fold({ CatalogUiState(page = it, loading = false) }, { CatalogUiState(false, error = readable(it)) })
        }
    }

    fun loadDetail(id: String) {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _detail.value = DetailUiState(loading = true)
            _detail.value = runCatching {
                val detail = repository.detail(id)
                val library = repository.library(id)
                DetailUiState(
                    detail = detail,
                    favorite = library?.favorite == true,
                    resume = library?.takeIf { it.playedAt != null && it.streamUrl != null },
                )
            }.getOrElse { DetailUiState(error = readable(it)) }
        }
    }

    fun closeDetail() {
        detailJob?.cancel()
        _detail.value = DetailUiState()
    }

    fun toggleFavorite(onChanged: (Boolean) -> Unit = {}) {
        val video = _detail.value.detail?.video ?: return
        viewModelScope.launch {
            val favorite = repository.toggleFavorite(video)
            _detail.value = _detail.value.copy(favorite = favorite)
            onChanged(favorite)
        }
    }

    fun recordPlayback(lineId: String, episodeName: String, url: String, position: Long, duration: Long) {
        val video = _detail.value.detail?.video ?: return
        viewModelScope.launch {
            repository.recordPlayback(video, lineId, episodeName, url, position, duration)
        }
    }

    fun cacheEpisode(line: PlayLine, episode: PlayEpisode) {
        val video = _detail.value.detail?.video ?: return
        app.playbackEngine.enqueue(video, line, episode)
    }

    fun cacheLine(line: PlayLine) {
        val video = _detail.value.detail?.video ?: return
        app.playbackEngine.enqueueLine(video, line)
    }

    fun removeDownload(id: String) = app.playbackEngine.removeDownload(id)
    fun setDownloadPaused(id: String, paused: Boolean) =
        app.playbackEngine.setDownloadPaused(id, paused)
    fun clearDownloads() = app.playbackEngine.removeAllDownloads()

    fun clearHistory() = viewModelScope.launch { repository.clearHistory() }
    fun clearSearchHistory() = viewModelScope.launch { app.preferences.clearSearchHistory() }
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { app.preferences.setTheme(mode) }

    private fun readable(error: Throwable): String {
        val causes = generateSequence(error as Throwable?) { it.cause }.toList()
        return when {
            causes.any { it is UnknownHostException } ->
                "当前网络无法解析数据源域名，请检查 Wi-Fi、DNS 或代理后重试"
            causes.any { it is SocketTimeoutException } ->
                "连接数据源超时，请稍后重试或切换网络"
            causes.any { it is SSLException } ->
                "安全连接失败，请检查系统时间或网络代理后重试"
            else -> error.message?.takeIf { it.isNotBlank() }
                ?: "加载失败，请检查网络后重试"
        }
    }

    companion object {
        fun factory(app: IKanApplication): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(app) as T
        }
    }
}
