package com.afs.sdk.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AtlasUser(
    val id: String,
    val hash: String,
    var atlasId: String? = null,
    val name: String? = null,
    val email: String? = null
) : Parcelable