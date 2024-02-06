package com.afs.sdk.repository

import com.afs.sdk.api.ConversationsRequest
import com.afs.sdk.data.AtlasUser
import com.afs.sdk.data.ConversationsResponse
import com.google.gson.Gson

class ConversationsRemoteRepository(gson: Gson) : AbstractRemoteRepository(gson) {

    suspend fun fetchConversations(
        atlasUser: AtlasUser
    ): ConversationsResponse? {
        return executeWithResponse<ConversationsResponse>(
            ConversationsRequest(
                atlasUser,
                gson
            ).generateRequest()
        )
    }

}