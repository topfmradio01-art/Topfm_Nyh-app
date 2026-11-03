package hu.topfm.nyh

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : Activity() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private lateinit var statusText: TextView
    private lateinit var playButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(12, 12, 12))
        }

        val title = TextView(this).apply {
            text = "TOPFM_NYH"
            textSize = 34f
            setTextColor(Color.rgb(212, 175, 55))
            gravity = Gravity.CENTER
        }

        val slogan = TextView(this).apply {
            text = "CSAK TOP SLÁGEREK"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            text = "Kapcsolódás..."
            textSize = 16f
            setTextColor(Color.rgb(212, 175, 55))
            gravity = Gravity.CENTER
        }

        playButton = Button(this).apply {
            text = "LEJÁTSZÁS"
            textSize = 18f
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.rgb(212, 175, 55))
            setOnClickListener {
                toggleRadio()
            }
        }

        layout.addView(title)
        layout.addView(slogan)
        layout.addView(statusText)
        layout.addView(playButton)

        setContentView(layout)

        connectToRadioService()
    }

    private fun connectToRadioService() {
        val token = SessionToken(
            this,
            ComponentName(this, RadioService::class.java)
        )

        controllerFuture = MediaController.Builder(this, token)
            .buildAsync()

        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                statusText.text = "ÉLŐ ADÁSRA KÉSZ"
                updateButton()
            } catch (e: Exception) {
                statusText.text = "Kapcsolódási hiba"
            }
        }, { command -> runOnUiThread(command) })
    }

    private fun toggleRadio() {
        val controller = mediaController ?: return

        if (controller.isPlaying || controller.playWhenReady) {
            controller.pause()
            statusText.text = "LEÁLLÍTVA"
        } else {
            val metadata = MediaMetadata.Builder()
                .setTitle("TOPFM_NYH")
                .setArtist("Csak TOP slágerek")
                .build()

            val item = MediaItem.Builder()
                .setUri("https://s02.diazol.hu:10012/stream")
                .setMediaMetadata(metadata)
                .build()

            controller.setMediaItem(item)
            controller.prepare()
            controller.play()
            statusText.text = "KAPCSOLÓDÁS..."
        }

        updateButton()
    }

    private fun updateButton() {
        val controller = mediaController ?: return
        playButton.text =
            if (controller.isPlaying || controller.playWhenReady) {
                "SZÜNET"
            } else {
                "LEJÁTSZÁS"
            }
    }

    override fun onDestroy() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onDestroy()
    }
}
