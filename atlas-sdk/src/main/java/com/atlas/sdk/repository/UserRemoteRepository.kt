package com.atlas.sdk.repository

import com.atlas.sdk.api.LoginRequest
import com.atlas.sdk.api.UpdateCustomFieldsRequest
import com.atlas.sdk.data.AtlasUser
import com.atlas.sdk.data.LoginResponse
import com.google.gson.Gson

class UserRemoteRepository(gson: Gson) : AbstractRemoteRepository(gson) {

    suspend fun login(
        appId: String,
        atlasUser: AtlasUser
    ): LoginResponse? {
        val loginReq = LoginRequest(appId, atlasUser).generateRequest()
        return executeWithResponse<LoginResponse>(loginReq)
    }

    suspend fun updateCustomFields(
        atlasUser: AtlasUser,
        ticketId: String,
        customData: Map<String, Any>
    ) {
        execute(UpdateCustomFieldsRequest(atlasUser, ticketId, customData, gson).generateRequest())
    }
}