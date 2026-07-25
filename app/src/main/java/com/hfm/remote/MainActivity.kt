package com.hfm.remote

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hfm.remote.ui.search.SearchFragment
import com.hfm.remote.ui.queue.QueueFragment
import com.hfm.remote.ui.remote.RemoteFragment

class MainActivity : AppCompatActivity() {

    var tvWebSocket: com.hfm.remote.network.TVWebSocketClient? = null
    val httpServer = com.hfm.remote.server.LocalHttpServer(8551)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tabLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        tabLayout.addView(Button(this).apply {
            text = "🔍 Search"
            setOnClickListener { showFragment(SearchFragment()) }
        })
        tabLayout.addView(Button(this).apply {
            text = "📋 Queue"
            setOnClickListener { showFragment(QueueFragment()) }
        })
        tabLayout.addView(Button(this).apply {
            text = "🎮 Remote"
            setOnClickListener { showFragment(RemoteFragment()) }
        })
        layout.addView(tabLayout)

        setContentView(layout)
        showFragment(SearchFragment())

        httpServer.start()
    }

    private fun showFragment(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, f).commitNowAllowingStateLoss()
    }

    fun connectToTV(ip: String, port: Int = 8550) {
        tvWebSocket?.close()
        tvWebSocket = com.hfm.remote.network.TVWebSocketClient(ip, port)
        tvWebSocket?.connect()
    }

    fun sendCommand(type: String, data: Map<String, Any> = emptyMap()) {
        val msg = com.google.gson.Gson().toJson(mapOf("type" to type) + data)
        tvWebSocket?.send(msg)
    }

    override fun onDestroy() {
        tvWebSocket?.close()
        httpServer.stop()
        super.onDestroy()
    }
}