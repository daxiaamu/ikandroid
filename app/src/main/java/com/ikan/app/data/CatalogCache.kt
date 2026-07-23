package com.ikan.app.data

import com.ikan.app.model.CatalogPage
import com.ikan.app.model.Video
import com.ikan.app.model.VideoSection
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class CatalogCache(private val file: File) {
    fun read(): CatalogPage? = runCatching {
        val root = JSONObject(file.readText())
        val sectionsJson = root.getJSONArray("sections")
        val sections = buildList {
            for (sectionIndex in 0 until sectionsJson.length()) {
                val sectionJson = sectionsJson.getJSONObject(sectionIndex)
                val videosJson = sectionJson.getJSONArray("videos")
                val videos = buildList {
                    for (videoIndex in 0 until videosJson.length()) {
                        val video = videosJson.getJSONObject(videoIndex)
                        add(
                            Video(
                                id = video.getString("id"),
                                title = video.getString("title"),
                                poster = video.optString("poster"),
                                subtitle = video.optString("subtitle"),
                                year = video.optString("year"),
                                area = video.optString("area"),
                                actors = video.optString("actors"),
                            ),
                        )
                    }
                }
                if (videos.isNotEmpty()) add(VideoSection(sectionJson.getString("title"), videos))
            }
        }
        CatalogPage(root.optString("title", "推荐"), sections = sections).takeIf { sections.isNotEmpty() }
    }.getOrNull()

    fun write(page: CatalogPage) {
        if (page.sections.isEmpty()) return
        val root = JSONObject()
            .put("title", page.title)
            .put("sections", JSONArray().apply {
                page.sections.forEach { section ->
                    put(
                        JSONObject()
                            .put("title", section.title)
                            .put("videos", JSONArray().apply {
                                section.videos.forEach { video ->
                                    put(
                                        JSONObject()
                                            .put("id", video.id)
                                            .put("title", video.title)
                                            .put("poster", video.poster)
                                            .put("subtitle", video.subtitle)
                                            .put("year", video.year)
                                            .put("area", video.area)
                                            .put("actors", video.actors),
                                    )
                                }
                            }),
                    )
                }
            })
        file.parentFile?.mkdirs()
        val temporary = File(file.parentFile, "${file.name}.tmp")
        temporary.writeText(root.toString())
        if (!temporary.renameTo(file)) {
            temporary.copyTo(file, overwrite = true)
            temporary.delete()
        }
    }
}
