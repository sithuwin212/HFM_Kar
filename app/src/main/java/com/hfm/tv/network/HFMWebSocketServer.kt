package com.hfm.tv.network

import android.util.Log
import com.hfm.tv.data.QueueEntity
import com.hfm.tv.data.SongEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.net.NetworkInterface

class HFMWebSocketServer(
    port: Int = 8550,
    private val onCommand: (String, WebSocket) -> Unit
) : WebSocketServer(InetSocketAddress(port)) {

    private val _connectedPhones = MutableStateFlow<List<PhoneInfo>>(emptyList())
    val connectedPhones: StateFlow<List<PhoneInfo>> = _connectedPhones

    data class PhoneInfo(
        val id: String,
        val name: String,
        val ip: String,
        val connectedAt: Long = System.currentTimeMillis()
    )

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        val ip = conn.remoteSocketAddress?.hostName ?: "unknown"
        val phoneId = handshake.getFieldValue("phone-id").ifEmpty { "phone-${ip}" }
        val name = handshake.getFieldValue("phone-name").ifEmpty { "Phone" }

        _connectedPhones.value = _connectedPhones.value + PhoneInfo(
            id = phoneId, name = name, ip = ip
        )
        Log.d(TAG, "Phone connected: $name @ $ip (total: ${_connectedPhones.value.size})")
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        val ip = conn.remoteSocketAddress?.hostName ?: "unknown"
        _connectedPhones.value = _connectedPhones.value.filter { it.ip != ip }
        Log.d(TAG, "Phone disconnected: $ip")
    }

    override fun onMessage(conn: WebSocket, message: String) {
        onCommand(message, conn)
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        Log.e(TAG, "WS Error: ${ex.message}")
    }

    override fun onStart() {
        Log.d(TAG, "WS Server started on port $port")
    }

    // Broadcast to all connected phones
    fun broadcastMessage(message: String) {
        connections.forEach { it.send(message) }
    }

    /** Get the server's LAN IP address */
    fun getLocalIpAddress(): String {
        NetworkInterface.getNetworkInterfaces()?.asSequence()
            ?.flatMap { it.inetAddresses.asSequence() }
            ?.firstOrNull {
                !it.isLoopbackAddress && it.hostAddress?.startsWith("192.168") == true
            }?.let { return it.hostAddress }

        NetworkInterface.getNetworkInterfaces()?.asSequence()
            ?.flatMap { it.inetAddresses.asSequence() }
            ?.firstOrNull {
                !it.isLoopbackAddress && it.hostAddress?.contains(".") == true
            }?.let { return it.hostAddress }

        return "127.0.0.1"
    }

    companion object {
        private const val TAG = "HFM-WS"
    }

    private fun ClientHandshake.getFieldValue(key: String): String {
        val raw = getFieldValue("X-$key") ?: getFieldValue(key)
        return raw?.takeIf { it.isNotEmpty() } ?: ""
    }
}