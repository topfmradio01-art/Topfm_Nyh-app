package hu.topfm.nyh

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class RadioService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
    super.onCreate()

    val exoPlayer = ExoPlayer.Builder(this).build()
    player = exoPlayer

    val mediaItem = MediaItem.Builder()
        .setUri("https://s02.diazol.hu:10012/stream")
        .build()

    exoPlayer.setMediaItem(mediaItem)
        exoPlayer.setMediaItem(mediaItem)

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }

        mediaSession = null
        player = null

        super.onDestroy()
    }
}
