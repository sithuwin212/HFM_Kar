package com.hfm.remote.server

import android.util.Log
import fi.iki.elonen.NanoHTTPD

class LocalHttpServer(port: Int) : NanoHTTPD(port) {

    private val files = mutableMapOf<String, ByteArray>()

    fun serveFile(id: String, data: ByteArray) {
        files[id] = data
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri.removePrefix("/")
        if (uri == "ping") return newFixedLengthResponse("pong")

        val data = files[uri]
        if (data != null) {
            return newChunkedResponse(Response.Status.OK, "video/mp4", data.inputStream())
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found")
    }

    companion object {
        private const val TAG = "HFM-HTTP"
    }
}