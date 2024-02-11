package com.atlas.sdk.api

import com.atlas.sdk.core.Config
import com.atlas.sdk.data.AtlasUser
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class UpdateCustomFieldsRequest(
    private val atlasUser: AtlasUser,
    private val ticketId: String,
    private val customData: Map<String, Any>,
    private val gson: Gson
) : AbstractRequest {

    override fun generateRequest(): Request {
        val jsonObject = JSONObject()
        jsonObject.put("conversationId", ticketId)
        jsonObject.put("customFields", gson.toJson(customData))

        return Request.Builder()
            .url(
                Config.UPDATE_CUSTOM_FIELDS_URL.plus(atlasUser.atlasId)
                    .plus("/update_custom_fields")
            )
            .header("x-atlas-user-hash", atlasUser.hash)
            .post(
                jsonObject.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            )
            .header("Content-Type", "application/json")
            .build()
    }

}