package com.atlas.sdk.core

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class WebSocketMessageParser(val gson: Gson) {
    fun parse(message: String): WebSocketMessage? {
        return gson.fromJson(message, WebSocketMessage::class.java)
    }
    @Keep
    data class WebSocketMessage(
        @SerializedName("packet_type")
        val packetType: String,
        val payload: Any?
    ) {
        companion object {
            const val PACKET_TYPE_CONVERSATION_UPDATED = "CONVERSATION_UPDATED"
            const val PACKET_TYPE_AGENT_MESSAGE = "AGENT_MESSAGE"
            const val PACKET_TYPE_BOT_MESSAGE = "BOT_MESSAGE"
            const val PACKET_TYPE_MESSAGE_READ = "MESSAGE_READ"
            const val PACKET_TYPE_CHATBOT_WIDGET_RESPONSE = "CHATBOT_WIDGET_RESPONSE"
            const val PACKET_TYPE_CONVERSATION_HIDDEN = "CONVERSATION_HIDDEN"
        }
    }

}