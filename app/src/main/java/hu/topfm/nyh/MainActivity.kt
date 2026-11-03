
package hu.topfm.nyh

import android.app.Activity
import android.os.Bundle
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private var player: MediaPlayer? = null
    private var isLoading = false
    private var isPlaying = false

    private val streamUrl = "https://s02.diazol.hu:10012/stream"

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
            text = "ÉLŐ ADÁSRA KÉSZ"
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
                if (isPlaying || isLoading) {
                    stopRadio()
                } else {
                    startRadio()
                }
            }
        }

        layout.addView(title)
        layout.addView(slogan)
        layout.addView(statusText)
        layout.addView(playButton)

        setContentView(layout)
    }

    private fun startRadio() {
        stopRadio()

        isLoading = true
        statusText.text = "Kapcsolódás..."
        playButton.text = "LEÁLLÍTÁS"

        val newPlayer = MediaPlayer()
        player = newPlayer

        newPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        newPlayer.setOnPreparedListener { mp ->
            isLoading = false
            isPlaying = true
            statusText.text = "ÉLŐ ADÁS"
            mp.start()
        }

        newPlayer.setOnErrorListener { _, _, _ ->
            stopRadio()
            statusText.text = "Kapcsolódási hiba"
            true
        }

        try {
            newPlayer.setDataSource(streamUrl)
            newPlayer.prepareAsync()
        } catch (e: Exception) {
            stopRadio()
            statusText.text = "Nem sikerült elindítani"
        }
    }

    private fun stopRadio() {
        isLoading = false
        isPlaying = false

        player?.let {
            try {
                it.setOnPreparedListener(null)
                it.setOnErrorListener(null)
                it.stop()
            } catch (_: Exception) {
            }

            try {
                it.release()
            } catch (_: Exception) {
            }
        }

        player = null

        if (::playButton.isInitialized) {
            playButton.text = "LEJÁTSZÁS"
        }

        if (::statusText.isInitialized) {
            statusText.text = "LEÁLLÍTVA"
        }
    }

    override fun onDestroy() {
        stopRadio()
        super.onDestroy()
    }
}
