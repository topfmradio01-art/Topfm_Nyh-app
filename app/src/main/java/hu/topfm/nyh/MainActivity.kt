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
import java.util.Calendar

class MainActivity : Activity() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private lateinit var statusText: TextView
    private lateinit var playButton: Button
    private lateinit var homeView: View

    private var showingSchedule = false
    private var showingContact = false

    private val gold = Color.rgb(212, 175, 55)
    private val dark = Color.rgb(12, 12, 12)
    private val streamUrl = "https://s02.diazol.hu:10012/stream"

    private data class ProgramItem(
        val time: String,
        val name: String
    )

    private val weeklySchedule = mapOf(
        Calendar.MONDAY to listOf(
            ProgramItem("00:00 – 20:00", "Csak Top Slágerek"),
            ProgramItem("20:00 – 22:00", "Back To The Music"),
            ProgramItem("22:00 – 24:00", "Csak Top Slágerek")
        ),
        Calendar.TUESDAY to listOf(
            ProgramItem("00:00 – 19:00", "Csak Top Slágerek"),
            ProgramItem("19:00 – 20:00", "Back In Time Classics"),
            ProgramItem("20:00 – 22:00", "Top Night Out"),
            ProgramItem("22:00 – 24:00", "Csak Top Slágerek")
        ),
        Calendar.WEDNESDAY to listOf(
            ProgramItem("00:00 – 20:00", "Csak Top Slágerek"),
            ProgramItem("20:00 – 22:00", "I Love 2000’s"),
            ProgramItem("22:00 – 24:00", "Csak Top Slágerek")
        ),
        Calendar.THURSDAY to listOf(
            ProgramItem("00:00 – 20:00", "Csak Top Slágerek"),
            ProgramItem("20:00 – 22:00", "Spy Time"),
            ProgramItem("22:00 – 24:00", "Csak Top Slágerek")
        ),
        Calendar.FRIDAY to listOf(
            ProgramItem("00:00 – 19:00", "Csak Top Slágerek"),
            ProgramItem("19:00 – 20:00", "Classic Night"),
            ProgramItem("20:00 – 21:00", "Eurodance Party"),
            ProgramItem("21:00 – 24:00", "Csak Top Slágerek")
        ),
        Calendar.SATURDAY to listOf(
            ProgramItem("00:00 – 16:00", "Csak Top Slágerek"),
            ProgramItem("16:00 – 18:00", "Saturday Party"),
            ProgramItem("18:00 – 20:00", "Millenium Party Show"),
            ProgramItem("20:00 – 21:00", "Top Night Pulse"),
            ProgramItem("21:00 – 24:00", "Csak Top Slágerek")
        ),
        Calendar.SUNDAY to listOf(
            ProgramItem("00:00 – 15:00", "Csak Top Slágerek"),
            ProgramItem("15:00 – 16:00", "Begurulva"),
            ProgramItem("16:00 – 24:00", "Csak Top Slágerek")
        )
    )

    private val dayNames = mapOf(
        Calendar.MONDAY to "HÉTFŐ",
        Calendar.TUESDAY to "KEDD",
        Calendar.WEDNESDAY to "SZERDA",
        Calendar.THURSDAY to "CSÜTÖRTÖK",
        Calendar.FRIDAY to "PÉNTEK",
        Calendar.SATURDAY to "SZOMBAT",
        Calendar.SUNDAY to "VASÁRNAP"
    )

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
                // A logó hiánya nem akadályozza az indulást.
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
            showSchedule()
        }

        addCard(cards, "♪", "MOST ADÁSBAN") {
            showNowPlaying()
        }

        addCard(cards, "✉", "KAPCSOLAT") {
            showContact()
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
            text = "TOPFM_NYH  •  CSAK TOP SLÁGEREK"
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

        homeView = scrollView
        setContentView(homeView)

        connectToRadioService()
    }private fun addCard(
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

    card.addView(icon, LinearLayout.LayoutParams(dp(48), dp(42)))

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

private fun addContactCard(
    parent: LinearLayout,
    symbol: String,
    label: String,
    detail: String,
    action: () -> Unit
) {
    val card = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(18), dp(14), dp(18), dp(14))
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

    card.addView(icon, LinearLayout.LayoutParams(dp(48), dp(48)))

    val textContainer = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_VERTICAL
    }

    val cardLabelText = TextView(this).apply {
        text = label
        textSize = 15f
        setTextColor(gold)
        setTypeface(null, android.graphics.Typeface.BOLD)
        gravity = Gravity.CENTER_VERTICAL
    }

    val detailText = TextView(this).apply {
        text = detail
        textSize = 14f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER_VERTICAL
        includeFontPadding = true
    }

    textContainer.addView(
    cardLabelText,
    LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
)

textContainer.addView(
    detailText,
    LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
)

    card.addView(
        textContainer,
        LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
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
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(12)
        }
    )
}

    // MŰSORREND KÉPERNYŐ
    private fun showSchedule() {
        showingSchedule = true

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        renderSchedule(today, today)
    }

    private fun renderSchedule(selectedDay: Int, today: Int) {
        val tomorrow = if (today == Calendar.SATURDAY) {
            Calendar.SUNDAY
        } else if (today == Calendar.SUNDAY) {
            Calendar.MONDAY
        } else {
            today + 1
        }

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(dark)
            isVerticalScrollBarEnabled = false
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(49), dp(22), dp(30))
            setBackgroundColor(dark)
        }

        val backButton = Button(this).apply {
            text = "‹  VISSZA"
            textSize = 15f
            setTextColor(gold)
            background = darkButtonBackground()
            setOnClickListener {
                showingSchedule = false
                setContentView(homeView)
            }
        }

        layout.addView(
            backButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
            ).apply {
                bottomMargin = dp(22)
            }
        )

        val heading = TextView(this).apply {
            text = "MŰSORAINK"
            textSize = 27f
            setTextColor(gold)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        layout.addView(
            heading,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(22)
            }
        )

        val selector = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val todayButton = scheduleSelectorButton(
            "MA",
            selectedDay == today
        ) {
            renderSchedule(today, today)
        }

        val tomorrowButton = scheduleSelectorButton(
            "HOLNAP",
            selectedDay == tomorrow
        ) {
            renderSchedule(tomorrow, today)
        }

        selector.addView(
            todayButton,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            ).apply {
                rightMargin = dp(6)
            }
        )

        selector.addView(
            tomorrowButton,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            ).apply {
                leftMargin = dp(6)
            }
        )

        layout.addView(
            selector,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(24)
            }
        )

        val dayTitle = TextView(this).apply {
            text = dayNames[selectedDay] ?: ""
            textSize = 21f
            setTextColor(gold)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        layout.addView(
            dayTitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(18)
            }
        )

        val programs = weeklySchedule[selectedDay] ?: emptyList()

        for (program in programs) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                background = GradientDrawable().apply {
                    setColor(Color.rgb(25, 25, 25))
                    setStroke(dp(1), gold)
                    cornerRadius = dp(12).toFloat()
                }
            }

            val timeText = TextView(this).apply {
                text = program.time
                textSize = 14f
                setTextColor(gold)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val nameText = TextView(this).apply {
                text = program.name
                textSize = 17f
                setTextColor(Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            row.addView(timeText)

            row.addView(
                nameText,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(6)
                }
            )

            layout.addView(
                row,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(12)
                }
            )
        }

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    private fun scheduleSelectorButton(
        label: String,
        selected: Boolean,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(if (selected) Color.BLACK else gold)
            background = if (selected) {
                GradientDrawable().apply {
                    setColor(gold)
                    cornerRadius = dp(12).toFloat()
                }
            } else {
                darkButtonBackground()
            }
            setOnClickListener { action() }
        }
    }

    private fun darkButtonBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.rgb(25, 25, 25))
            setStroke(dp(1), gold)
            cornerRadius = dp(12).toFloat()
        }
    }

    // KAPCSOLAT KÉPERNYŐ
    private fun showContact() {
        showingContact = true

        val scroll = ScrollView(this).apply {
            setBackgroundColor(dark)
            isVerticalScrollBarEnabled = false
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(49), dp(22), dp(30))
            setBackgroundColor(dark)
        }

        val back = Button(this).apply {
            text = "‹  VISSZA"
            setTextColor(gold)
            background = darkButtonBackground()
            setOnClickListener {
                showingContact = false
                setContentView(homeView)
            }
        }

        layout.addView(
            back,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
            ).apply {
                bottomMargin = dp(24)
            }
        )

        val heading = TextView(this).apply {
            text = "KAPCSOLAT"
            textSize = 28f
            setTextColor(gold)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        layout.addView(heading)

        val subtitle = TextView(this).apply {
            text = "Lépj kapcsolatba velünk!"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        layout.addView(
            subtitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
                bottomMargin = dp(28)
            }
        )

        addContactCard(
            layout, "✉", "E-MAIL KÜLDÉSE",
            "info@topfmnyiregyhaza.hu"
        ) {
            openLink("mailto:info@topfmnyiregyhaza.hu")
        }

        addContactCard(
            layout, "🌐", "WEBOLDAL",
            "www.topfmnyiregyhaza.hu"
        ) {
            openLink("https://www.topfmnyiregyhaza.hu")
        }

        addContactCard(
            layout, "f", "FACEBOOK",
            "Topfm_NYH Facebook oldal"
        ) {
            openLink("https://www.facebook.com/share/1GJY6YrXL9/")
        }

        addContactCard(
            layout, "✉", "SMS / VIBER",
            "06-70/585-16-52", true
        ) {
            showMessageChoice()
        }

        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun addContactCard(
        parent: LinearLayout,
        symbol: String,
        title: String,
        detail: String,
        whiteDetail: Boolean = false,
        action: () -> Unit
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
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

        card.addView(icon, LinearLayout.LayoutParams(dp(45), dp(48)))

        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        val titleText = TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(gold)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val detailText = TextView(this).apply {
            text = detail
            textSize = 14f
            setTextColor(if (whiteDetail) Color.WHITE else Color.LTGRAY)
        }

        texts.addView(titleText)
        texts.addView(
            detailText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(5)
            }
        )

        card.addView(
            texts,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val arrow = TextView(this).apply {
            text = "›"
            textSize = 28f
            setTextColor(gold)
        }

        card.addView(arrow)

card.minimumHeight = dp(90)

parent.addView(
    card,
    LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        bottomMargin = dp(14)
    }
)
    }    private fun showMessageChoice() {
        AlertDialog.Builder(this)
            .setTitle("Kapcsolatfelvétel")
            .setItems(arrayOf("SMS küldése", "Viber megnyitása")) { _, which ->
                if (which == 0) {
                    openLink("smsto:06705851652")
                } else {
                    openLink("viber://chat?number=%2B36705851652")
                }
            }
            .setNegativeButton("MÉGSE", null)
            .show()
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

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (showingSchedule || showingContact) {
            showingSchedule = false
            showingContact = false
            setContentView(homeView)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onDestroy()
    }
}
