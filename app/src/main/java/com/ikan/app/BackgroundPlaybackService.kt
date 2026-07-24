package com.ikan.app

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Hosts the app-wide player in a standard Media3 session while background playback is enabled.
 * The system-provided MediaStyle notification is also what ColorOS uses for media controls.
 */
class BackgroundPlaybackService : MediaSessionService() {
    private lateinit var playbackEngine: PlaybackEngine
    private lateinit var player: Player
    private lateinit var sessionPlayer: Player
    private var mediaSession: MediaSession? = null
    private val controllerCommands =
        mutableMapOf<MediaSession.ControllerInfo, ControllerCommands>()
    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateAvailableCommands()
        }
    }
    private val sessionCallback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val defaults = super.onConnect(session, controller)
            controllerCommands[controller] = ControllerCommands(
                defaults.availableSessionCommands,
                defaults.availablePlayerCommands,
            )
            return MediaSession.ConnectionResult.accept(
                defaults.availableSessionCommands,
                episodeBoundaryCommands(defaults.availablePlayerCommands),
            )
        }

        override fun onPostConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ) {
            super.onPostConnect(session, controller)
            updateAvailableCommands()
        }

        override fun onDisconnected(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ) {
            controllerCommands.remove(controller)
            super.onDisconnected(session, controller)
        }
    }

    override fun onCreate() {
        super.onCreate()
        playbackEngine = (application as IKanApplication).playbackEngine
        playbackEngine.setBackgroundSessionRequested(true)
        player = playbackEngine.obtainPlayer()
        sessionPlayer = EpisodeBoundaryPlayer(player)
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        mediaSession = MediaSession.Builder(this, sessionPlayer)
            .setSessionActivity(sessionActivity)
            .setCallback(sessionCallback)
            .build()
        player.addListener(playerListener)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        player.removeListener(playerListener)
        controllerCommands.clear()
        mediaSession?.release()
        mediaSession = null
        playbackEngine.setBackgroundSessionRequested(false)
        super.onDestroy()
    }

    private fun episodeBoundaryCommands(base: Player.Commands): Player.Commands =
        base.withEpisodeBoundaries(player)

    private fun updateAvailableCommands() {
        val session = mediaSession ?: return
        session.connectedControllers.forEach { controller ->
            val commands = controllerCommands[controller] ?: return@forEach
            session.setAvailableCommands(
                controller,
                commands.session,
                episodeBoundaryCommands(commands.player),
            )
        }
    }

    private data class ControllerCommands(
        val session: androidx.media3.session.SessionCommands,
        val player: Player.Commands,
    )

    /**
     * Media3's per-controller command filtering does not affect every vendor's legacy
     * PlaybackState-based PiP implementation. Expose the same episode boundaries directly from
     * the session player so those compatibility controls cannot advertise an impossible action.
     */
    private class EpisodeBoundaryPlayer(private val delegate: Player) :
        ForwardingPlayer(delegate) {
        override fun getAvailableCommands(): Player.Commands =
            delegate.availableCommands.withEpisodeBoundaries(delegate)

        override fun isCommandAvailable(command: Int): Boolean =
            availableCommands.contains(command)
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

private fun Player.Commands.withEpisodeBoundaries(player: Player): Player.Commands =
    buildUpon()
        .removeIf(
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
            !player.hasPreviousMediaItem(),
        )
        .removeIf(
            Player.COMMAND_SEEK_TO_PREVIOUS,
            !player.hasPreviousMediaItem(),
        )
        .removeIf(
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            !player.hasNextMediaItem(),
        )
        .removeIf(
            Player.COMMAND_SEEK_TO_NEXT,
            !player.hasNextMediaItem(),
        )
        .build()
