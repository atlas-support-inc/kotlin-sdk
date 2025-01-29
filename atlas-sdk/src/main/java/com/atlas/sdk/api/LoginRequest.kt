package com.atlas.sdk.api

import com.atlas.sdk.core.Config
import com.atlas.sdk.data.AtlasUser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginRequest(
    private val appId: String,
    private val atlasUser: AtlasUser
): AbstractRequest {

    override fun generateRequest(): Request {
        val jsonObject = JSONObject()
        jsonObject.put("appId", appId)
        jsonObject.put("userId", atlasUser.id)
        jsonObject.put("userHash", atlasUser.hash)
        atlasUser.name?.let {
            jsonObject.put("name", it)
        }
        atlasUser.email?.let {
            jsonObject.put("email", it)
        }
        atlasUser.phoneNumber?.let {
            jsonObject.put("phoneNumber", it)
        }

        return Request.Builder()
            .url(Config.LOGIN_URL)
            .post(
                jsonObject.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            )
            .header("Content-Type", "application/json")
            .build()
    }

}