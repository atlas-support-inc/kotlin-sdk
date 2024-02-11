package com.atlas.sdk.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class LoginResponse(
    val id: String?,
    val detail: String?
) : Parcelable {

    val isSuccessful: Boolean
        get() = id != null
}