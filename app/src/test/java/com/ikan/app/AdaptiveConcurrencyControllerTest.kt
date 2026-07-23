package com.ikan.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveConcurrencyControllerTest {
    @Test
    fun `probes upward and rolls back when aggregate speed drops`() {
        val controller = AdaptiveConcurrencyController()

        assertEquals(2, controller.update(1_000, 1_000, true, 8, PlaybackPriority.NONE))
        assertEquals(4, controller.update(3_000, 1_000, true, 32, PlaybackPriority.NONE))
        assertEquals(8, controller.update(6_000, 1_100, true, 32, PlaybackPriority.NONE))
        assertEquals(4, controller.update(9_000, 800, true, 32, PlaybackPriority.NONE))
    }

    @Test
    fun `prioritizes player according to whether active download is current media`() {
        val controller = AdaptiveConcurrencyController()

        assertEquals(
            32,
            controller.update(1_000, 1_000, true, 32, PlaybackPriority.CURRENT_DOWNLOAD),
        )
        assertEquals(
            1,
            controller.update(2_000, 1_000, true, 32, PlaybackPriority.FOREGROUND_STREAM),
        )
        assertEquals(
            2,
            controller.update(3_000, 0, false, 32, PlaybackPriority.NONE),
        )
    }

    @Test
    fun `respects cellular cap`() {
        val controller = AdaptiveConcurrencyController()

        controller.update(1_000, 1_000, true, 8, PlaybackPriority.NONE)
        controller.update(3_000, 1_000, true, 8, PlaybackPriority.NONE)
        controller.update(6_000, 1_100, true, 8, PlaybackPriority.NONE)
        assertEquals(8, controller.connections)
        assertEquals(
            8,
            controller.update(9_000, 1_200, true, 8, PlaybackPriority.NONE),
        )
    }
}
