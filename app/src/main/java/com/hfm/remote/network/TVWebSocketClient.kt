package com.hfm.remote.network

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class TVWebSocketClient(ip: String, port: Int) :
    WebSocketClient(URI("ws://$ip:$port")) {

    var onMessage: ((String) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d(TAG, "Connected to TV")
        onConnected?.invoke()
    }

    override fun onMessage(message: String) {
        onMessage?.invoke(message)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d(TAG, "Disconnected: $reason")
        onDisconnected?.invoke()
    }

    override fun onError(ex: Exception) {
        Log.e(TAG, "WS Error: ${ex.message}")
    }

    companion object {
        private const val TAG = "HFM-WS-Client"
    }
}