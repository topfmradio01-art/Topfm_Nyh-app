
package hu.topfm.nyh

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : Activity() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private lateinit var statusText: TextView
    private lateinit var playButton: Button

    private val gold = Color.rgb(212, 175, 55)
    private val dark = Color.rgb(12, 12, 12)
    private val streamUrl = "https://s02.diazol.hu:10012/stream"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(dark)
            isVerticalScrollBarEnabled = false
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(24), dp(22), dp(24))
            setBackgroundColor(dark)
        }

        // LOGÓ
        val logo = ImageView(this).apply {
            try {
                assets.open("topfm_logo.png").use {
                    setImageBitmap(BitmapFactory.decodeStream(it))
                }
            } catch (_: Exception) {
                // A logó hiánya nem akadályozza az alkalmazás indulását.
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
        }

        layout.addView(
            logo,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(170)
            ).apply {
                bottomMargin = dp(12)
            }
        )

        // RÁDIÓ NEVE
        val title = TextView(this).apply {
            text = "TOPFM_NYH"
            textSize = 30f
            setTextColor(gold)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
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
                topMargin = dp(6)
            }
        )

        // ÉLŐ ADÁS JELZÉS
        statusText = TextView(this).apply {
            text = "● KAPCSOLÓDÁS..."
            textSize = 15f
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        layout.addView(
            statusText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
                bottomMargin = dp(12)
            }
        )

        // LEJÁTSZÁS GOMB
        playButton = Button(this).apply {
            text = "▶  LEJÁTSZÁS"
            textSize = 18f
            setTextColor(Color.BLACK)
            setBackgroundColor(gold)
            setOnClickListener {
                toggleRadio()
            }
        }

        layout.addView(
            playButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58)
            ).apply {
                bottomMargin = dp(24)
            }
        )

        // FUNKCIÓKÁRTYÁK
        val cards = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        addCard(cards, "♫", "MŰSORAINK") {
    openLink("https://topfmnyiregyhaza.hu/musorok/")
        }

        addCard(cards, "♪", "MOST ADÁSBAN") {
            showNowPlaying()
        }

        addCard(cards, "✉", "ÜZENETKÜLDÉS") {
            openChat()
        }

        addCard(cards, "🌐", "WEBOLDAL") {
            openWebsite()
        }

        layout.addView(
            cards,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val footer = TextView(this).apply {
            text = "TOPFM_NYH  •  CSak TOP SLÁGEREK"
            textSize = 12f
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        layout.addView(
            footer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(55)
            ).apply {
                topMargin = dp(12)
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

    private fun addCard(
        parent: LinearLayout,
        symbol: String,
        label: String,
        action: () -> Unit
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(12))
            background = GradientDrawable().apply {
                setColor(Color.rgb(25, 25, 25))
                setStroke(dp(1), gold)
                cornerRadius = dp(12).toFloat()
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        val icon = TextView(this).apply {
            text = symbol
            textSize = 25f
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        card.addView(
            icon,
            LinearLayout.LayoutParams(dp(48), dp(42))
        )

        val cardLabelText = TextView(this).apply {
    text = label
    textSize = 15f
    setTextColor(Color.WHITE)
    setTypeface(null, android.graphics.Typeface.BOLD)
    gravity = Gravity.CENTER_VERTICAL
        }

        card.addView(
    cardLabelText,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                leftMargin = dp(12)
            }
        )

        val arrowText = TextView(this).apply {
    text = "›"
    textSize = 28f
    setTextColor(gold)
    gravity = Gravity.CENTER
        }

        card.addView(arrowText)

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(68)
            ).apply {
                bottomMargin = dp(12)
            }
        )
    }

    private fun connectToRadioService() {
        val token = SessionToken(
            this,
            ComponentName(this, RadioService::class.java)
        )

        controllerFuture = MediaController.Builder(this, token).buildAsync()

        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()

                mediaController?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updateButton()
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        updateButton()
                    }
                })

                updateButton()
            } catch (_: Exception) {
                statusText.text = "KAPCSOLÓDÁSI HIBA"
            }
        }, { command -> runOnUiThread(command) })
    }

    private fun toggleRadio() {
        val controller = mediaController ?: run {
            statusText.text = "KÉRJÜK, VÁRJ..."
            return
        }

        if (controller.isPlaying || controller.playWhenReady) {
            controller.pause()
        } else {
            val metadata = MediaMetadata.Builder()
                .setTitle("TOPFM_NYH")
                .setArtist("Csak TOP slágerek")
                .build()

            val item = MediaItem.Builder()
                .setUri(streamUrl)
                .setMediaMetadata(metadata)
                .build()

            controller.setMediaItem(item)
            controller.prepare()
            controller.play()
        }

        updateButton()
    }

    private fun updateButton() {
        val playing = mediaController?.isPlaying == true

        playButton.text = if (playing) {
            "Ⅱ  SZÜNET"
        } else {
            "▶  LEJÁTSZÁS"
        }

        statusText.text = if (playing) {
            "● ÉLŐ ADÁS"
        } else {
            "● TOPFM_NYH ONLINE"
        }
    }

    private fun showNowPlaying() {
        val controller = mediaController

        val songTitle = controller?.mediaMetadata?.title?.toString()
            ?: "TOPFM_NYH"

        val artist = controller?.mediaMetadata?.artist?.toString()
            ?: "Csak TOP slágerek"

        AlertDialog.Builder(this)
            .setTitle("MOST ADÁSBAN")
            .setMessage("$songTitle\n$artist")
            .setPositiveButton("BEZÁRÁS", null)
            .show()
    }

    private fun openWebsite() {
        openLink("https://www.topfmnyiregyhaza.hu")
    }

    private fun openChat() {
        openLink("https://topfmnyh.3cx.hu/#/office")
    }

    private fun openLink(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            statusText.text = "A LINK NEM NYITHATÓ MEG"
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
