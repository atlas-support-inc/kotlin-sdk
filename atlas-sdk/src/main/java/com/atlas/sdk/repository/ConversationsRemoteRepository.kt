package com.atlas.sdk.repository

import com.atlas.sdk.api.ConversationsRequest
import com.atlas.sdk.data.AtlasUser
import com.atlas.sdk.data.ConversationsResponse
import com.google.gson.Gson

class ConversationsRemoteRepository(gson: Gson) : AbstractRemoteRepository(gson) {

    suspend fun fetchConversations(
        atlasUser: AtlasUser
    ): ConversationsResponse? {
        return executeWithResponse(ConversationsRequest(atlasUser).generateRequest())
    }

}