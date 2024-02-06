package com.afs.sdk.repository

import com.afs.sdk.api.LoginRequest
import com.afs.sdk.api.UpdateCustomFieldsRequest
import com.afs.sdk.data.AtlasUser
import com.afs.sdk.data.LoginResponse
import com.google.gson.Gson

class UserRemoteRepository(gson: Gson) : AbstractRemoteRepository(gson) {

    suspend fun login(
        appId: String,
        atlasUser: AtlasUser
    ): LoginResponse? {
        return executeWithResponse<LoginResponse>(LoginRequest(appId, atlasUser).generateRequest())
    }

    suspend fun updateCustomFields(
        atlasUser: AtlasUser,
        ticketId: String,
        customData: Map<String, Any>
    ) {
        execute(UpdateCustomFieldsRequest(atlasUser, ticketId, customData, gson).generateRequest())
    }
}