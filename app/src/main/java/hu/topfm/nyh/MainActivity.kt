package hu.topfm.nyh

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.graphics.BitmapFactory
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
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

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.rgb(12, 12, 12))
            isVerticalScrollBarEnabled = false
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.rgb(12, 12, 12))
        }

    scaleType = ImageView.ScaleType.FIT_CENTER
    adjustViewBounds = true
        }

        layout.addView(
            logo,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(190)
            ).apply {
                bottomMargin = dp(16)
            }
        )

        val title = TextView(this).apply {
            text = "TOPFM_NYH"
            textSize = 32f
            setTextColor(Color.rgb(212, 175, 55))
            gravity = Gravity.CENTER
        }

        layout.addView(title)

        val slogan = TextView(this).apply {
            text = "CSAK TOP SLÁGEREK"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        layout.addView(
            slogan,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        )

        statusText = TextView(this).apply {
            text = "Kapcsolódás..."
            textSize = 16f
            setTextColor(Color.rgb(212, 175, 55))
            gravity = Gravity.CENTER
        }

        layout.addView(
            statusText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(28)
            }
        )

        playButton = Button(this).apply {
            text = "LEJÁTSZÁS"
            textSize = 18f
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.rgb(212, 175, 55))
            setOnClickListener {
                toggleRadio()
            }
        }

        layout.addView(
            playButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(60)
            ).apply {
                topMargin = dp(16)
                bottomMargin = dp(24)
            }
        )

        scrollView.addView(
    layout,
    android.widget.FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
)

        setContentView(scrollView)

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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onDestroy()
    }
}
