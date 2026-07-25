package com.hfm.tv

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.hfm.tv.data.SongEntity
import com.hfm.tv.network.HFMWebSocketServer
import com.hfm.tv.ui.player.PlayerActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class PhoneActivity : ComponentActivity() {

    private lateinit var songList: ListView
    private lateinit var statusText: TextView
    private var songs = listOf<SongEntity>()
    private var wsServer: HFMWebSocketServer? = null

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) loadSongs() else statusText.text = "Permission denied"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Header
        layout.addView(TextView(this).apply {
            text = "🎵 HFM Karaoke"
            setTextSize(26f)
        })

        // Buttons
        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        btnRow.addView(Button(this).apply {
            text = "📂 Pick Folder"
            setOnClickListener {
                startActivity(android.content.Intent(this@PhoneActivity,
                    com.hfm.tv.ui.settings.SettingsActivity::class.java))
            }
        })
        btnRow.addView(Button(this).apply {
            text = "🔄 Scan"
            setOnClickListener { loadSongs() }
        })
        btnRow.addView(Button(this).apply {
            text = "📡 QR"
            setOnClickListener {
                Toast.makeText(this@PhoneActivity, "Server: ${wsServer?.getLocalIpAddress()}:8550", Toast.LENGTH_LONG).show()
            }
        })
        layout.addView(btnRow)

        // Status
        statusText = TextView(this).apply {
            text = "Initializing..."
            setPadding(0, 12, 0, 12)
        }
        layout.addView(statusText)

        // Song list
        songList = ListView(this)
        songList.setOnItemClickListener { _, _, pos, _ ->
            if (pos < songs.size) {
                val intent = PlayerActivity.createIntent(this, songs[pos])
                startActivity(intent)
            }
        }
        layout.addView(songList, LinearLayout.LayoutParams(-1, 0).apply { weight = 1f })

        setContentView(layout)

        // Start WebSocket server
        wsServer = HFMWebSocketServer(8550) { _, _ -> }
        wsServer?.start()
        statusText.text = "📡 TV: ${wsServer?.getLocalIpAddress()}:8550"

        // Request storage permission
        if (Build.VERSION.SDK_INT >= 33) {
            storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            loadSongs()
        }
    }

    private fun loadSongs() {
        val db = (application as HFMApplication).database
        Thread {
            val songsFromDb = db.songDao().getAllSongs()
            runOnUiThread {
                songs = emptyList()
                val adapter = ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, emptyList())
                songList.adapter = adapter
            }
            // Observe from Room
            kotlinx.coroutines.MainScope().launch {
                db.songDao().getAllSongs().collect { list ->
                    songs = list
                    val titles = list.map { "${it.artist} - ${it.title}" }
                    runOnUiThread {
                        songList.adapter = ArrayAdapter(this@PhoneActivity,
                            android.R.layout.simple_list_item_1, titles)
                        statusText.text = "${list.size} songs | 📡 ${wsServer?.getLocalIpAddress()}:8550"
                    }
                }
            }
        }.start()
    }

    override fun onDestroy() {
        wsServer?.stop()
        super.onDestroy()
    }
}