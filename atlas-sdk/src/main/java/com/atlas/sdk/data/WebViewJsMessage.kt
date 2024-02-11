package com.atlas.sdk.data

import androidx.annotation.Keep

@Keep
data class WebViewJsMessage(
    val type: String,
    val errorMessage: String?,
    val ticketId: String?,
    val atlasId: String?,
    val userId: String?,
    val userHash: String?
)