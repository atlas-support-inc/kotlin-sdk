package com.afs.sdk.data

import androidx.annotation.Keep

@Keep
data class AtlasStats(val conversations: MutableList<ConversationStats> = mutableListOf())