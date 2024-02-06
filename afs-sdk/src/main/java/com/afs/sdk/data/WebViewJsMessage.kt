package com.afs.sdk.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class WebViewJsMessage(
    val type: String,
    val errorMessage: String?,
    val ticketId: String?,
    val atlasId: String?,
    val userId: String?,
    val userHash: String?
) : Parcelable {
}