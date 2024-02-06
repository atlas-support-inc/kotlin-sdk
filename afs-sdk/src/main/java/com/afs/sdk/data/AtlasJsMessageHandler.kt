package com.afs.sdk.data

import androidx.lifecycle.Lifecycle

interface InternalJsMessageHandler {
    fun onError(message: String?)

    fun onNewTicket(ticketId: String?)

    fun onChangeIdentity(atlasId: String?, userId: String?, userHash: String?)
}

abstract class AtlasJsMessageHandler(val lifecycle: Lifecycle) : InternalJsMessageHandler