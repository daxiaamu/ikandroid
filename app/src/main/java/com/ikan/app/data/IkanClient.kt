package com.ikan.app.data

import com.ikan.app.model.CatalogFilter
import com.ikan.app.model.CatalogPage
import com.ikan.app.model.HomeCategory
import com.ikan.app.model.PlayEpisode
import com.ikan.app.model.PlayLine
import com.ikan.app.model.Video
import com.ikan.app.model.VideoDetail
import com.ikan.app.model.VideoSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class IkanClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build(),
) {
    companion object {
        const val BASE_URL = "https://www1.ikanbot.com"
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 iKan/1.0"
    }

    suspend fun catalog(category: HomeCategory): CatalogPage = withContext(Dispatchers.IO) {
        parseCatalog(get(category.path), category.label, category == HomeCategory.RECOMMEND)
    }

    suspend fun catalog(path: String, title: String): CatalogPage = withContext(Dispatchers.IO) {
        parseCatalog(get(path), title, false)
    }

    suspend fun search(query: String): CatalogPage = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name())
        parseCatalog(get("/search?q=$encoded"), "搜索：${query.trim()}", false)
    }

    suspend fun detail(id: String): VideoDetail = withContext(Dispatchers.IO) {
        val path = "/play/$id"
        val document = Jsoup.parse(get(path), BASE_URL)
        val tokenSeed = document.selectFirst("#e_token")?.attr("value").orEmpty()
        val mediaType = document.selectFirst("#mtype")?.attr("value")?.toIntOrNull() ?: 1
        val token = buildToken(id, tokenSeed)
        val info = document.selectFirst(".result-info.active")
        val metadata = info?.select(".detail h3").orEmpty().map { it.text().trim() }
        val video = Video(
            id = id,
            title = document.selectFirst("#video_title")?.text()?.trim().orEmpty().ifBlank { "未知影片" },
            poster = info?.selectFirst("img")?.imageUrl().orEmpty()
                .ifBlank { document.selectFirst("meta[property=og:image]")?.attr("content").orEmpty() },
            subtitle = metadata.getOrNull(0).orEmpty(),
            year = metadata.getOrNull(1).orEmpty(),
            area = metadata.getOrNull(2).orEmpty(),
            actors = metadata.getOrNull(3).orEmpty(),
        )
        val json = get(
            "/api/getResN?videoId=$id&mtype=$mediaType&token=$token",
            referer = "$BASE_URL$path",
        )
        val lines = parseLines(json)
        VideoDetail(video, mediaType, lines)
    }

    private fun get(path: String, referer: String? = null): String {
        val url = if (path.startsWith("http")) path else "$BASE_URL$path"
        val request = Request.Builder().url(url).header("User-Agent", USER_AGENT).apply {
            referer?.let { header("Referer", it) }
            header("Accept-Language", "zh-CN,zh;q=0.9")
        }.build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("服务器返回 ${response.code}")
            response.body.string()
        }
    }

    internal fun parseCatalog(html: String, fallbackTitle: String, home: Boolean): CatalogPage {
        val doc = Jsoup.parse(html, BASE_URL)
        if (home) {
            val sections = doc.select(".v-list").mapNotNull { block ->
                val videos = parseVideos(block)
                if (videos.isEmpty()) null else VideoSection(
                    block.selectFirst("h4")?.ownText()?.trim().orEmpty().ifBlank { "推荐" },
                    videos,
                )
            }
            return CatalogPage(fallbackTitle, sections = sections)
        }
        // Only read the page-local filter bar next to its heading. Selecting every
        // `ul.nav` also captures the desktop header, hidden category link and mobile footer.
        val filters = doc.select("h5 + ul.nav a[href]").mapNotNull { link ->
            val href = link.attr("href")
            val label = link.text().trim()
            if (label.isBlank() || href == "/" || label in setOf("首页", "电影", "剧集", "榜单", "播放历史", "看过")) null
            else CatalogFilter(
                label = label,
                path = href,
                selected = link.parent()?.let { it.hasClass("disabled") || it.hasClass("active") } == true,
            )
        }.distinctBy { it.path }
        val title = doc.selectFirst("h4, h5")?.ownText()?.trim().orEmpty().ifBlank { fallbackTitle }
        val next = doc.select("a[href]").firstOrNull { it.text().contains("下一页") }?.attr("href")
        return CatalogPage(title, filters, parseVideos(doc), nextPath = next)
    }

    private fun parseVideos(root: Element): List<Video> = root.select("a[href^=/play/]")
        .mapNotNull { anchor ->
            val id = anchor.attr("href").substringAfterLast('/').substringBefore('?')
            if (id.toLongOrNull() == null) return@mapNotNull null
            val image = anchor.selectFirst("img")
            val title = anchor.selectFirst("p")?.text()?.trim().orEmpty()
                .ifBlank { image?.attr("alt")?.trim().orEmpty() }
                .ifBlank { anchor.text().trim() }
            if (title.isBlank()) null else Video(id, title, image?.imageUrl().orEmpty())
        }.distinctBy { it.id }

    private fun Element.imageUrl(): String = attr("data-src").ifBlank { attr("src") }
        .takeUnless { it.startsWith("data:") }.orEmpty()

    internal fun buildToken(id: String, seed: String): String {
        var remaining = seed
        return id.takeLast(4).mapNotNull { digit ->
            val start = (digit.digitToIntOrNull() ?: return@mapNotNull null) % 3 + 1
            if (remaining.length < start + 8) return@mapNotNull null
            val part = remaining.substring(start, start + 8)
            remaining = remaining.substring(start + 8)
            part
        }.joinToString("")
    }

    internal fun parseLines(json: String): List<PlayLine> {
        val root = JSONObject(json)
        if (root.optInt("state") != 1) error(root.optString("message", "线路加载失败"))
        val list = root.getJSONObject("data").getJSONArray("list")
        return buildList {
            for (lineIndex in 0 until list.length()) {
                val lineObject = list.getJSONObject(lineIndex)
                val resources = runCatching { JSONArray(lineObject.optString("resData")) }.getOrNull() ?: continue
                val episodes = buildList {
                    for (resourceIndex in 0 until resources.length()) {
                        val resource = resources.getJSONObject(resourceIndex)
                        val groupName = resource.optString("newName")
                        resource.optString("url").split('#').forEach { packed ->
                            val parts = packed.split('$')
                            val url = parts.getOrNull(1).orEmpty().trim()
                            if (url.startsWith("http") && url.substringBefore('?').endsWith(".m3u8", true)) {
                                val rawName = parts.firstOrNull().orEmpty().trim().ifBlank { "播放" }
                                add(PlayEpisode(groupName.ifBlank { rawName }, url))
                            }
                        }
                    }
                }.distinctBy { it.name to it.url }
                if (episodes.isNotEmpty()) add(
                    PlayLine(
                        id = lineObject.optLong("id", lineIndex.toLong()).toString(),
                        name = "线路 ${lineIndex + 1}",
                        episodes = episodes,
                    ),
                )
            }
        }
    }
}
