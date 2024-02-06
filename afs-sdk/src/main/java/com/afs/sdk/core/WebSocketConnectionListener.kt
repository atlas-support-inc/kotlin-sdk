package com.afs.sdk.core

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketConnectionListener(val atlasId: String) : WebSocketListener() {

    private var webSocket: WebSocket? = null

    private fun run() {
        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(1000, TimeUnit.MILLISECONDS)
            .build()

        val request: Request = Request.Builder()
            .url(Config.ATLAS_WEB_SOCKET_BASE_URL.plus("/ws/CUSTOMER::$atlasId"))
            .build()
        client.newWebSocket(request, this)

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
//        client.dispatcher.executorService.shutdown()
    }

    fun connect() {
        run()
    }

    fun close() {
        webSocket?.close(1000, null)
    }

    fun shutdown() {
        webSocket?.cancel()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocketConnectionListener", "MESSAGE: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("WebSocketConnectionListener", "MESSAGE: " + bytes.hex())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        this.webSocket = null
        Log.d("WebSocketConnectionListener", "CLOSE: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        t.printStackTrace()
    }

}