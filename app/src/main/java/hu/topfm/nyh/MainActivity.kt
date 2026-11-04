
package hu.topfm.nyh

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import android.graphics.Typeface
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

    private val gold = Color.rgb(212, 175, 55)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            setBackgroundColor(Color.rgb(10, 10, 10))
        }

        val logo = ImageView(this).apply {
            val logoId = resources.getIdentifier(
                "fb_img_1767705001216_removebg_preview",
                "drawable",
                packageName
            )
            if (logoId != 0) {
                setImageResource(logoId)
                scaleType = ImageView.ScaleType.FIT_CENTER
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        layout.addView(
            logo,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                260
            )
        )

        val title = TextView(this).apply {
            text = "TOPFM_NYH"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        val slogan = TextView(this).apply {
            text = "CSAK TOP SLÁGEREK"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            text = "KAPCSOLÓDÁS..."
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        playButton = Button(this).apply {
            text = "LEJÁTSZÁS"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            setBackgroundColor(gold)
            setOnClickListener {
                toggleRadio()
            }
        }

        layout.addView(title)
        layout.addView(slogan)

        val spacer = View(this)
        layout.addView(
            spacer,
            LinearLayout.LayoutParams(1, 35)
        )

        layout.addView(statusText)

        layout.addView(
            playButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                60
            ).apply {
                topMargin = 20
            }
        )

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
                statusText.text = "KAPCSOLÓDÁSI HIBA"
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
