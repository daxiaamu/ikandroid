package com.ikan.app.model

data class Video(
    val id: String,
    val title: String,
    val poster: String = "",
    val subtitle: String = "",
    val year: String = "",
    val area: String = "",
    val actors: String = "",
)

data class VideoSection(val title: String, val videos: List<Video>)

data class CatalogPage(
    val title: String,
    val filters: List<CatalogFilter> = emptyList(),
    val videos: List<Video> = emptyList(),
    val sections: List<VideoSection> = emptyList(),
    val nextPath: String? = null,
)

data class CatalogFilter(val label: String, val path: String, val selected: Boolean = false)

data class PlayEpisode(val name: String, val url: String)
data class PlayLine(val id: String, val name: String, val episodes: List<PlayEpisode>)

data class VideoDetail(
    val video: Video,
    val mediaType: Int,
    val lines: List<PlayLine>,
)

enum class HomeCategory(val label: String, val path: String) {
    RECOMMEND("推荐", "/"),
    MOVIE("电影", "/hot/index-movie-%E7%83%AD%E9%97%A8.html"),
    TV("剧集", "/hot/index-tv-%E7%83%AD%E9%97%A8.html"),
    BILLBOARD("榜单", "/billboard.html"),
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }
