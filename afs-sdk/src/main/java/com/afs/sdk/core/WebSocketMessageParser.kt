package com.afs.sdk.core

import androidx.annotation.Keep
import com.afs.sdk.data.Conversation
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
        val payload: Payload?
    ) {
        @Keep
        data class Payload(val conversation: Conversation?, val conversationId: String?,
            val message: Message?)
        @Keep
        data class Message(val conversationId: String?)

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