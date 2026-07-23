package com.ikan.app.data

import com.ikan.app.model.CatalogPage
import com.ikan.app.model.Video
import com.ikan.app.model.VideoSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CatalogCacheTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun roundTripsHomeSections() {
        val cache = CatalogCache(temporaryFolder.newFile("home.json"))
        val page = CatalogPage(
            title = "推荐",
            sections = listOf(
                VideoSection("最近热门电影", listOf(Video("42", "影片", "https://img/poster.jpg"))),
            ),
        )

        cache.write(page)

        assertEquals(page, cache.read())
    }

    @Test
    fun ignoresInvalidOrEmptySnapshots() {
        val file = temporaryFolder.newFile("home.json").apply { writeText("not json") }
        assertNull(CatalogCache(file).read())
    }
}
