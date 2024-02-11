package com.atlas.sdk.data

import androidx.annotation.Keep

@Keep
data class ConversationsResponse(
    val data: List<Conversation>? = null,
    val detail: String? = null
) {

    val isSuccessful: Boolean
        get() = data != null
}