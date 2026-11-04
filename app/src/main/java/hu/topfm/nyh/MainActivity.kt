
package hu.topfm.nyh

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
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

    private val gold = Color.rgb(212, 175, 55)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 16, 24, 16)
            setBackgroundColor(Color.rgb(10, 10, 10))
        }

        val logo = ImageView(this).apply {
            val logoId = resources.getIdentifier(
    "topfm_logo",
    "drawable",
    packageName
)
if (logoId != 0) {
    setImageResource(logoId)
}
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
        }

        layout.addView(
            logo,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                4f
            )
        )

        val title = TextView(this).apply {
            text = "TOPFM_NYH"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        layout.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val slogan = TextView(this).apply {
            text = "CSAK TOP SLÁGEREK"
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        layout.addView(
            slogan,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                0.6f
            )
        )

        statusText = TextView(this).apply {
            text = "KAPCSOLÓDÁS..."
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        layout.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                0.8f
            )
        )

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

        layout.addView(
            playButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                56
            ).apply {
                topMargin = 12
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
