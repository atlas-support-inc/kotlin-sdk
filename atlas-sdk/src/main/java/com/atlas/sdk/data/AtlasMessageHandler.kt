package com.atlas.sdk.data

import androidx.annotation.Keep

interface InternalMessageHandler {
    fun onError(message: String?)

    fun onNewTicket(ticketId: String?)

    fun onChangeIdentity(atlasId: String?, userId: String?, userHash: String?)
}

@Keep
abstract class AtlasMessageHandler : InternalMessageHandler