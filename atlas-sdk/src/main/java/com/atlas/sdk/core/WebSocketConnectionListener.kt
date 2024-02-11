package com.atlas.sdk.core

import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketConnectionListener(val atlasId: String, val gson: Gson) : WebSocketListener() {

    private var webSocket: WebSocket? = null
    private val webSocketMessageParser = WebSocketMessageParser(gson)

    var webSocketMessageHandler: WebSocketMessageHandler? = null

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
        webSocketMessageHandler = null
        webSocket?.close(1000, null)
    }

    fun shutdown() {
        webSocketMessageHandler = null
        webSocket?.cancel()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket
        val jsonObject = JSONObject()
        jsonObject.put("channel_id", atlasId)
        jsonObject.put("channel_kind", "CUSTOMER")
        jsonObject.put("packet_type", "SUBSCRIBE")
        jsonObject.put("payload",  "{}")
        webSocket.send(jsonObject.toString())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocketConnectionListener", "MESSAGE: $text")
        webSocketMessageHandler?.onNewMessage(webSocketMessageParser.parse(text))
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        this.webSocket = null
        this.webSocketMessageHandler = null
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        t.printStackTrace()
    }

    interface WebSocketMessageHandler {
        fun onNewMessage(webSocketMessage: WebSocketMessageParser.WebSocketMessage?)
    }

}