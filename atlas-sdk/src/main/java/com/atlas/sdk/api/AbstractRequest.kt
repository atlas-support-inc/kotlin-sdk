package com.atlas.sdk.api

import okhttp3.Request

interface AbstractRequest {

    fun generateRequest(): Request
}