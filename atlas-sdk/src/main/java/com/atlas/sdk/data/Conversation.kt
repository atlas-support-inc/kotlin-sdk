package com.atlas.sdk.data

import androidx.annotation.Keep

@Keep
data class Conversation(val id: String, val messages: List<Message>, val closed: Boolean?) {
    @Keep
    data class Message(val read: Boolean? = null, val side: Int, val closed: Boolean) {

        val isBotOrAgent: Boolean
            get() = side == MessageSide.AGENT.value || side == MessageSide.BOT.value

        @Keep
        enum class MessageSide(val value: Int) {
            CUSTOMER(1),
            AGENT(2),
            BOT(3)
        }
    }
}