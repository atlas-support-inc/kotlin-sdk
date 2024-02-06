package com.afs.sdk.api

import com.afs.sdk.core.Config
import com.afs.sdk.data.AtlasUser
import com.google.gson.Gson
import okhttp3.Request

class ConversationsRequest(
    private val atlasUser: AtlasUser,
    private val gson: Gson
): AbstractRequest {

    override fun generateRequest(): Request {
        return Request.Builder()
            .url(Config.CONVERSATIONS_URL.plus(atlasUser.atlasId))
            .header("x-atlas-user-hash", atlasUser.hash)
            .header("Content-Type", "application/json")
            .get()
            .build()
    }
}