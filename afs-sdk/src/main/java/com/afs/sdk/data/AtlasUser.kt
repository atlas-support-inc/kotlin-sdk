package com.afs.sdk.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AtlasUser(
    val id: String,
    val hash: String,
    var atlasId: String? = null,
    val name: String? = null,
    val email: String? = null
) : Parcelable