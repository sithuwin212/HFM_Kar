package com.hfm.tv

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.hfm.tv.data.AppDatabase
import com.hfm.tv.data.SongEntity
import com.hfm.tv.network.HFMWebSocketServer
import com.hfm.tv.ui.player.PlayerActivity
import kotlinx.coroutines.*
import java.io.File

class PhoneActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var searchInput: EditText
    private lateinit var songGrid: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var tabSongs: Button
    private lateinit var tabQueue: Button
    private lateinit var tabSettings: Button
    private lateinit var contentArea: LinearLayout
    private var wsServer: HFMWebSocketServer? = null
    private var allSongs = listOf<SongEntity>()
    private var currentTab = "songs"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = (application as HFMApplication).database

        // ── Root Layout ──
        val root = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
            background = ContextCompat.getDrawable(this@PhoneActivity, android.R.color.background_dark)?.apply {
                // dark background
            }
            setBackgroundColor(0xFF1A1025.toInt())
        }

        // ── Header ──
        layout.addView(TextView(this).apply {
            text = "🎤 HFM KTV Player"
            setTextSize(26f)
            setTextColor(0xFFF5F0FC.toInt())
        })
        layout.addView(TextView(this).apply {
            text = "မိသားစု Karaoke"
            setTextSize(16f)
            setTextColor(0xFF8B7EA0.toInt())
            setPadding(0, 0, 0, 20)
        })

        // ── Server Status ──
        statusText = TextView(this).apply {
            text = "📡 Starting server..."
            setTextSize(16f)
            setTextColor(0xFFC084FC.toInt())
            setPadding(0, 0, 0, 12)
        }
        layout.addView(statusText)

        // ── Search ──
        searchInput = EditText(this).apply {
            hint = "🔍 Search songs or artists..."
            setTextSize(18f)
            setHintTextColor(0xFF5C4E70.toInt())
            setTextColor(0xFFF5F0FC.toInt())
            setBackgroundColor(0xFF1E1530.toInt())
            setPadding(16, 12, 16, 12)
            setOnClickListener { /* focus */ }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) { filterSongs(s?.toString() ?: "") }
            })
        }
        layout.addView(searchInput, ViewGroup.LayoutParams(-1, -2).apply {
            // full width
        })

        // ── Tabs ──
        val tabRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 16, 0, 8) }
        tabSongs = Button(this).apply {
            text = "🎵 Songs"; tag = "songs"
            setOnClickListener { switchTab("songs") }
        }
        tabQueue = Button(this).apply {
            text = "📋 Queue"; tag = "queue"
            setOnClickListener { switchTab("queue") }
        }
        tabSettings = Button(this).apply {
            text = "⚙️"; tag = "settings"
            setOnClickListener { switchTab("settings") }
        }
        tabRow.addView(tabSongs); tabRow.addView(tabQueue); tabRow.addView(tabSettings)
        layout.addView(tabRow)

        // ── Content Area ──
        contentArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(-1, 0).apply { weight = 1f }
        }
        songGrid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        contentArea.addView(songGrid)
        layout.addView(contentArea)

        root.addView(layout)
        setContentView(root)

        // ── Init ──
        wsServer = HFMWebSocketServer(8550) { _, _ -> }
        wsServer?.start()
        statusText.text = "📡 ${wsServer?.getLocalIpAddress()}:8550"

        checkPermissions()
        switchTab("songs")
        loadSongs()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) loadSongs()
            }.launch(Manifest.permission.READ_MEDIA_VIDEO)
        }
    }

    private fun loadSongs() {
        scope.launch {
            db.songDao().getAllSongs().collect { list ->
                allSongs = list
                filterSongs(searchInput.text.toString())
                statusText.text = "📡 ${wsServer?.getLocalIpAddress()}:8550  |  ${list.size} songs"
            }
        }
    }

    private fun filterSongs(query: String) {
        songGrid.removeAllViews()
        val filtered = if (query.isBlank()) allSongs
        else allSongs.filter { it.title.contains(query, true) || it.artist.contains(query, true) }

        if (filtered.isEmpty()) {
            songGrid.addView(TextView(this).apply {
                text = "No songs found. Tap ⚙️ to pick Karaoke/ folder"
                setTextColor(0xFF8B7EA0.toInt())
                setPadding(0, 40, 0, 0)
            })
            return
        }

        // Create song cards
        for (song in filtered) {
            val card = createSongCard(song)
            songGrid.addView(card)
        }
    }

    private fun createSongCard(song: SongEntity): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xFF1E1530.toInt())
            setOnClickListener {
                startActivity(PlayerActivity.createIntent(this@PhoneActivity, song))
            }
        }
        val lp = ViewGroup.MarginLayoutParams(-1, -2)
        lp.setMargins(0, 0, 0, 8)
        card.layoutParams = lp

        // Placeholder thumbnail
        card.addView(TextView(this).apply {
            text = "🎤"
            setTextSize(28f)
            setPadding(0, 0, 16, 0)
        })

        // Info
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        info.addView(TextView(this).apply {
            text = song.title
            setTextSize(18f); setTextColor(0xFFF5F0FC.toInt())
        })
        info.addView(TextView(this).apply {
            text = song.artist
            setTextSize(14f); setTextColor(0xFF8B7EA0.toInt())
        })
        card.addView(info, ViewGroup.LayoutParams(0, -2).apply { weight = 1f })

        // Duration
        card.addView(TextView(this).apply {
            val sec = song.durationMs / 1000
            text = "${sec / 60}:${"%02d".format(sec % 60)}"
            setTextSize(14f); setTextColor(0xFF5C4E70.toInt())
        })

        return card
    }

    private fun switchTab(tab: String) {
        currentTab = tab
        songGrid.removeAllViews()
        contentArea.removeAllViews()
        when (tab) {
            "songs" -> {
                tabSongs.setBackgroundColor(0xFF7C3AED.toInt())
                tabQueue.setBackgroundColor(0xFF1E1530.toInt())
                tabSongs.setTextColor(0xFFFFFFFF.toInt())
                tabQueue.setTextColor(0xFF8B7EA0.toInt())
                filterSongs(searchInput.text.toString())
                contentArea.addView(songGrid)
            }
            "queue" -> {
                tabQueue.setBackgroundColor(0xFF7C3AED.toInt())
                tabSongs.setBackgroundColor(0xFF1E1530.toInt())
                tabQueue.setTextColor(0xFFFFFFFF.toInt())
                tabSongs.setTextColor(0xFF8B7EA0.toInt())
                contentArea.addView(TextView(this).apply {
                    text = "📋 Queue\n\nSongs added from Remote app will appear here."
                    setTextColor(0xFF8B7EA0.toInt()); setTextSize(16f); setPadding(0, 40, 0, 0)
                })
            }
            "settings" -> {
                startActivity(Intent(this, com.hfm.tv.ui.settings.SettingsActivity::class.java))
                switchTab("songs") // return to songs after settings
            }
        }
    }

    override fun onDestroy() {
        wsServer?.stop()
        scope.cancel()
        super.onDestroy()
    }
}