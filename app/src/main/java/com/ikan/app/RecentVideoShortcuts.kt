package com.ikan.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.ikan.app.data.LibraryEntity

object RecentVideoShortcuts {
    const val ACTION_OPEN_RECENT = "com.ikan.app.action.OPEN_RECENT"
    const val EXTRA_VIDEO_ID = "com.ikan.app.extra.VIDEO_ID"
    const val EXTRA_VIDEO_TITLE = "com.ikan.app.extra.VIDEO_TITLE"
    const val EXTRA_VIDEO_POSTER = "com.ikan.app.extra.VIDEO_POSTER"
    const val EXTRA_SHORTCUT_ID = "com.ikan.app.extra.SHORTCUT_ID"

    fun sync(context: Context, history: List<LibraryEntity>) {
        val limit = ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)
            .coerceAtMost(4)
            .coerceAtLeast(0)
        val shortcuts = history.take(limit).mapIndexed { rank, item ->
            val shortcutId = "recent:${Uri.encode(item.videoId)}"
            ShortcutInfoCompat.Builder(context, shortcutId)
                .setShortLabel(item.title.take(20))
                .setLongLabel("继续观看 ${item.title}".take(40))
                .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(
                    Intent(context, MainActivity::class.java)
                        .setAction(ACTION_OPEN_RECENT)
                        .putExtra(EXTRA_VIDEO_ID, item.videoId)
                        .putExtra(EXTRA_VIDEO_TITLE, item.title)
                        .putExtra(EXTRA_VIDEO_POSTER, item.poster)
                        .putExtra(EXTRA_SHORTCUT_ID, shortcutId),
                )
                .setRank(rank)
                .build()
        }

        if (shortcuts.isEmpty()) {
            ShortcutManagerCompat.removeAllDynamicShortcuts(context)
        } else {
            ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
        }
    }

    fun reportUsed(context: Context, shortcutId: String?) {
        shortcutId ?: return
        ShortcutManagerCompat.reportShortcutUsed(context, shortcutId)
    }
}
