package com.ikan.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IkanClientTest {
    private val client = IkanClient()

    @Test
    fun tokenMatchesWebsiteAlgorithm() {
        assertEquals(
            "499ec6f6914635326ecc3e34c4fd2a59",
            client.buildToken("976624", "n499ec6f6s91463532p626ecc3e34cdc4fd2a59"),
        )
    }

    @Test
    fun parsesMultipleLinesAndEpisodes() {
        val json = """{"state":1,"data":{"list":[
            {"id":10,"resData":"[{\"url\":\"第1集${'$'}https://a.test/1.m3u8#第2集${'$'}https://a.test/2.m3u8\"}]"},
            {"id":11,"resData":"[{\"url\":\"HD${'$'}https://b.test/movie.m3u8\"}]"}
        ]}}"""
        val lines = client.parseLines(json)
        assertEquals(2, lines.size)
        assertEquals(listOf("第1集", "第2集"), lines[0].episodes.map { it.name })
        assertEquals("https://b.test/movie.m3u8", lines[1].episodes.single().url)
    }

    @Test
    fun parsesHomeSectionsWithoutWebView() {
        val html = """
            <div class="v-list"><h4>最近热门电影</h4><div>
              <a class="item" href="/play/123"><img alt="测试电影" data-src="https://img.test/a.jpg"><p>测试电影</p></a>
            </div></div>
        """.trimIndent()
        val page = client.parseCatalog(html, "推荐", true)
        assertEquals("最近热门电影", page.sections.single().title)
        assertEquals("123", page.sections.single().videos.single().id)
        assertTrue(page.sections.single().videos.single().poster.startsWith("https://"))
    }

    @Test
    fun catalogFiltersIgnoreGlobalAndMobileNavigation() {
        val html = """
            <ul class="nav"><li><a href="/">首页</a></li><li><a href="/category/">分类</a></li></ul>
            <h5>最近热门电影</h5>
            <ul class="nav"><li class="disabled"><a href="/hot/index-movie-热门.html">热门</a></li><li><a href="/hot/index-movie-最新.html">最新</a></li></ul>
            <a href="/play/123"><img alt="测试" data-src="https://img.test/a.jpg"></a>
            <ul class="nav"><li><a href="/history.html">播放历史</a></li></ul>
        """.trimIndent()
        val page = client.parseCatalog(html, "电影", false)
        assertEquals(listOf("热门", "最新"), page.filters.map { it.label })
        assertEquals(listOf(true, false), page.filters.map { it.selected })
    }
}
