package com.afs.sdk.core

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
        const val PARAM_USER_ID = "userId"
        const val PARAM_USER_HASH = "userHash"
        const val PARAM_USER_NAME = "userName"
        const val PARAM_USER_EMAIL = "userEmail"

        const val MESSAGE_TYPE_ERROR = "atlas:error"
        const val MESSAGE_TYPE_NEW_TICKET = "atlas:newTicket"
        const val MESSAGE_TYPE_CHANGE_IDENTITY = "atlas:changeIdentity"
    }
}