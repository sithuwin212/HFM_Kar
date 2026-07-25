package com.hfm.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import com.hfm.tv.data.AppDatabase
import com.hfm.tv.network.HFMWebSocketServer
import com.hfm.tv.ui.library.LibraryFragment
import com.hfm.tv.ui.pairing.PairingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : FragmentActivity() {

    val database: AppDatabase get() = (application as HFMApplication).database
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var webSocketServer: HFMWebSocketServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(androidx.leanback.R.layout.lb_browse_fragment)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, PairingFragment())
                .commit()
        }

        startWebSocketServer()
    }

    private fun startWebSocketServer() {
        webSocketServer = HFMWebSocketServer(port = 8550) { message, conn ->
            // Handle commands from phone
            handlePhoneCommand(message, conn)
        }
        webSocketServer?.start()
    }

    private fun handlePhoneCommand(json: String, conn: org.java_websocket.WebSocket) {
        // Commands will be parsed here: search, queue_add, play, pause, etc.
        scope.run {
            try {
                val cmd = com.google.gson.Gson().fromJson(json, Map::class.java)
                @Suppress("UNCHECKED_CAST")
                val type = (cmd as? Map<*, *>)?.get("type") as? String ?: return@run

                when (type) {
                    "search" -> { /* handled via LiveData/Flow */ }
                    "get_library" -> { /* send library list */ }
                    "queue_add" -> { /* add to queue */ }
                    "play" -> { /* start playback */ }
                    "pause" -> { /* pause */ }
                    "next" -> { /* skip */ }
                    "vocal_off" -> { /* toggle vocal */ }
                }
            } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        webSocketServer?.stop()
        super.onDestroy()
    }
}