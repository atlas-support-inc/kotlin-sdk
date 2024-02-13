package com.atlas.sdk.api

import com.atlas.sdk.core.Config
import com.atlas.sdk.data.AtlasUser
import okhttp3.Request

class ConversationsRequest(
    private val atlasUser: AtlasUser
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