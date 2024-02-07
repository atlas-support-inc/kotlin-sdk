package com.afs.sdk.api

import com.afs.sdk.core.Config
import com.afs.sdk.data.AtlasUser
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
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