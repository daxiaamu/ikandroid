package com.ikan.app

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadContentDirectoryTest {
    @Test
    fun freshInstallUsesExternalDownloadsDirectory() {
        withDirectories { internal, external ->
            assertEquals(
                File(external, "downloads"),
                resolveDownloadContentDirectory(internal, external),
            )
        }
    }

    @Test
    fun missingExternalStorageFallsBackToInternalDownloadsDirectory() {
        withDirectories { internal, _ ->
            assertEquals(
                File(internal, "downloads"),
                resolveDownloadContentDirectory(internal, null),
            )
        }
    }

    @Test
    fun existingLegacyCacheIsMovedWithoutCopying() {
        withDirectories { internal, external ->
            val legacy = File(internal, "offline-media").apply { mkdirs() }
            File(legacy, "cache.v3.exo").writeText("cached")

            val selected = resolveDownloadContentDirectory(internal, external)

            assertEquals(File(external, "downloads"), selected)
            assertEquals("cached", File(selected, "cache.v3.exo").readText())
        }
    }

    private fun withDirectories(block: (File, File) -> Unit) {
        val root = Files.createTempDirectory("ikan-download-directory").toFile()
        try {
            block(
                File(root, "internal").apply { mkdirs() },
                File(root, "external").apply { mkdirs() },
            )
        } finally {
            root.deleteRecursively()
        }
    }
}
