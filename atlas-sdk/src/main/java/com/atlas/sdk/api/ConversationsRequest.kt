package com.atlas.sdk.api

import com.atlas.sdk.core.Config
import com.atlas.sdk.data.AtlasUser
import okhttp3.Request

class ConversationsRequest(
    private val atlasUser: AtlasUser
): AbstractRequest {

    override fun generateRequest(): Request {
        val req = Request.Builder()
            .url(Config.CONVERSATIONS_URL.plus(atlasUser.atlasId))
            .header("Content-Type", "application/json")

        atlasUser.hash?.let {
            req.header("x-atlas-user-hash", it)
        }

        return req.get().build()
    }
}