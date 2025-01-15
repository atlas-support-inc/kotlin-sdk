package com.atlas.sdk.core

class Config {
    companion object {
        const val ATLAS_WEB_SOCKET_BASE_URL = "wss://app.atlas.so"
        const val ATLAS_WIDGET_BASE_URL = "https://embed.atlas.so"
        const val ATLAS_API_BASE_URL = "https://app.atlas.so/api"
        const val LOGIN_URL = "$ATLAS_API_BASE_URL/client-app/company/identify"
        const val CONVERSATIONS_URL =  "$ATLAS_API_BASE_URL/client-app/conversations/"
        const val UPDATE_CUSTOM_FIELDS_URL =  "$ATLAS_API_BASE_URL/client-app/ticket/"

        const val PARAM_APP_ID = "appId"
        const val PARAM_ATLAS_ID = "atlasId"
        const val PARAM_QUERY = "query"

        const val MESSAGE_TYPE_ERROR = "atlas:error"
        const val MESSAGE_TYPE_NEW_TICKET = "atlas:newTicket"
        const val MESSAGE_TYPE_CHANGE_IDENTITY = "atlas:changeIdentity"

        const val PARAM_ES5 = "es5"
    }
}