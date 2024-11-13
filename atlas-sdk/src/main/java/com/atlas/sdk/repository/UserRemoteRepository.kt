package com.atlas.sdk.repository

import com.atlas.sdk.api.LoginRequest
import com.atlas.sdk.api.UpdateCustomFieldsRequest
import com.atlas.sdk.data.AtlasUser
import com.atlas.sdk.data.LoginResponse
import com.google.gson.Gson
import java.util.concurrent.CompletableFuture

class UserRemoteRepository(gson: Gson) : AbstractRemoteRepository(gson) {

    fun login(
        appId: String,
        atlasUser: AtlasUser
    ): CompletableFuture<LoginResponse?> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync executeWithResponse<LoginResponse>(LoginRequest(appId, atlasUser).generateRequest()).get()
        }
    }

    suspend fun updateCustomFields(
        atlasUser: AtlasUser,
        ticketId: String,
        customData: Map<String, Any>
    ) {
        execute(UpdateCustomFieldsRequest(atlasUser, ticketId, customData, gson).generateRequest())
    }
}