package com.ikan.app

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Hosts the app-wide player in a standard Media3 session while background playback is enabled.
 * The system-provided MediaStyle notification is also what ColorOS uses for media controls.
 */
class BackgroundPlaybackService : MediaSessionService() {
    private lateinit var playbackEngine: PlaybackEngine
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        playbackEngine = (application as IKanApplication).playbackEngine
        playbackEngine.setBackgroundSessionRequested(true)
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        mediaSession = MediaSession.Builder(this, playbackEngine.obtainPlayer())
            .setSessionActivity(sessionActivity)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        playbackEngine.setBackgroundSessionRequested(false)
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            val app = context.applicationContext as IKanApplication
            app.playbackEngine.setBackgroundSessionRequested(true)
            // MediaSessionService promotes itself when playback starts and owns the
            // MediaStyle notification lifecycle. Starting it as an FGS here races that
            // promotion on some ColorOS versions and triggers the platform timeout.
            context.startService(Intent(context, BackgroundPlaybackService::class.java))
        }

        fun stop(context: Context) {
            val app = context.applicationContext as IKanApplication
            app.playbackEngine.setBackgroundSessionRequested(false)
            context.stopService(Intent(context, BackgroundPlaybackService::class.java))
        }
    }
}
