package com.atlas.sdk.data

import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle

interface InternalJsMessageHandler {
    fun onError(message: String?)

    fun onNewTicket(ticketId: String?)

    fun onChangeIdentity(atlasId: String?, userId: String?, userHash: String?)
}

@Keep
abstract class AtlasJsMessageHandler(val lifecycle: Lifecycle) : InternalJsMessageHandler