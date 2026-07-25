package com.hfm.remote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    var tvWebSocket: com.hfm.remote.network.TVWebSocketClient? = null
    val httpServer = com.hfm.remote.server.LocalHttpServer(8551)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nav = BottomNavigationView(this).apply {
            inflateMenu()
            menu.add(0, 1, 0, "Search").setIcon(android.R.drawable.ic_menu_search)
            menu.add(0, 2, 0, "Queue").setIcon(android.R.drawable.ic_menu_sort_by_size)
            menu.add(0, 3, 0, "Remote").setIcon(android.R.drawable.ic_menu_play)
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    1 -> showFragment(com.hfm.remote.ui.search.SearchFragment())
                    2 -> showFragment(com.hfm.remote.ui.queue.QueueFragment())
                    3 -> showFragment(com.hfm.remote.ui.remote.RemoteFragment())
                }
                true
            }
        }
        setContentView(nav)
        showFragment(com.hfm.remote.ui.search.SearchFragment())

        httpServer.start()
    }

    private fun showFragment(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, f).commit()
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