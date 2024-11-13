package com.atlas.sdk.repository

import com.atlas.sdk.api.ConversationsRequest
import com.atlas.sdk.data.AtlasUser
import com.atlas.sdk.data.ConversationsResponse
import com.google.gson.Gson
import java.util.concurrent.CompletableFuture

class ConversationsRemoteRepository(gson: Gson) : AbstractRemoteRepository(gson) {

    fun fetchConversations(
        atlasUser: AtlasUser
    ): CompletableFuture<ConversationsResponse?> {
        return CompletableFuture.supplyAsync{
            return@supplyAsync executeWithResponse<ConversationsResponse>(ConversationsRequest(atlasUser).generateRequest()).get()
        }
    }
}