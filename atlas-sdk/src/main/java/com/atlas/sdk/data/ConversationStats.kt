package com.atlas.sdk.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ConversationStats(val id: String, var unread: Int, var closed: Boolean) : Parcelable {

    companion object {

        fun fromConversation(conversation: Conversation): ConversationStats {
            var unread = 0
            conversation.messages.forEach { message ->
                if (message.read != null && !message.read && message.isBotOrAgent) {
                    unread++
                }
            }
            return ConversationStats(conversation.id, unread, conversation.closed == true)
        }
    }
}