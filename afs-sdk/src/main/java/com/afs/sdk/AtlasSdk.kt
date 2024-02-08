package com.afs.sdk

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.afs.sdk.core.WebSocketConnectionListener
import com.afs.sdk.core.WebSocketMessageParser
import com.afs.sdk.data.AtlasJsMessageHandler
import com.afs.sdk.data.AtlasStats
import com.afs.sdk.data.AtlasUser
import com.afs.sdk.data.Conversation
import com.afs.sdk.data.ConversationStats
import com.afs.sdk.data.InternalJsMessageHandler
import com.afs.sdk.repository.ConversationsRemoteRepository
import com.afs.sdk.repository.UserLocalRepository
import com.afs.sdk.repository.UserRemoteRepository
import com.afs.sdk.view.AtlasWebView
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object AtlasSdk : DefaultLifecycleObserver {

    private val gson = Gson()

    private lateinit var userLocalRepository: UserLocalRepository
    private val userRemoteRepository = UserRemoteRepository(gson)
    private val conversationsRemoteRepository = ConversationsRemoteRepository(gson)

    private lateinit var appId: String
    private var atlasUser: AtlasUser? = null

    private val internalAtlasJsMessageHandler = object : InternalJsMessageHandler {
        override fun onError(message: String?) {
            Log.d("AtlasSdk", "onError:$message")
            atlasJsMessageHandlers.forEach {
                it.onError(message)
            }
        }

        override fun onNewTicket(ticketId: String?) {
            Log.d("AtlasSdk", "onNewTicket:$ticketId")
            ticketId?.let {
                GlobalScope.launch {
                    updateCustomFields(ticketId, mapOf("newTicketIsHere" to ticketId))
                }
            }
            atlasJsMessageHandlers.forEach {
                it.onNewTicket(ticketId)
            }
        }

        override fun onChangeIdentity(atlasId: String?, userId: String?, userHash: String?) {
            Log.d("AtlasSdk", "onChangeIdentity:$atlasId $userId $userHash")
            val user = if (atlasUser == null)
                AtlasUser(userId ?: "", userHash ?: "", atlasId)
            else
                atlasUser!!.apply { this.atlasId = atlasId }

            identify(user)

            GlobalScope.launch {
                userLocalRepository.storeIdentity(user)
            }

            atlasJsMessageHandlers.forEach {
                it.onChangeIdentity(atlasId, userId, userHash)
            }

        }
    }

    private val atlasJsMessageHandlers = arrayListOf<AtlasJsMessageHandler>()
    fun addAtlasJsMessageHandler(atlasJsMessageHandler: AtlasJsMessageHandler) {
        atlasJsMessageHandlers.add(atlasJsMessageHandler)
    }

    fun removeAtlasJsMessageHandler(atlasJsMessageHandler: AtlasJsMessageHandler) {
        atlasJsMessageHandlers.remove(atlasJsMessageHandler)
    }

    fun clearAtlasJsMessageHandlers() {
        atlasJsMessageHandlers.clear()
    }

    private var webSocketConnectionListener: WebSocketConnectionListener? = null
    private var atlasStats = AtlasStats()

    var atlasStatsUpdateWatcher: AtlasStatsUpdateWatcher? = null

    fun init(context: Context, appId: String) {
        this.appId = appId

        this.userLocalRepository = UserLocalRepository(getSharedPreferences(context), gson)

        GlobalScope.launch {
            userLocalRepository.loadStoredIdentity()?.let {
                identify(it)
            }
        }
    }

    fun getUser(): AtlasUser? = atlasUser

    fun identify(user: AtlasUser?) {
        this.atlasUser = user
    }

    fun bindAtlasWebView(atlasWebView: AtlasWebView) {
        atlasWebView.setSdkAtlasJsMessageHandler(internalAtlasJsMessageHandler)
        atlasWebView.applyConfig(appId, atlasUser)
    }

    suspend fun watchStats(lifecycle: Lifecycle) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            login()?.let { user ->
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    fetchConversations(user)?.let { conversationStats ->
                        atlasStats.conversations.clear()
                        atlasStats.conversations.addAll(conversationStats)

                        atlasStatsUpdateWatcher?.onStatsUpdate(atlasStats)
                    }

                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        webSocketConnectionListener?.shutdown()

                        webSocketConnectionListener =
                            WebSocketConnectionListener(user.atlasId!!, gson).apply {
                                webSocketMessageHandler =
                                    object : WebSocketConnectionListener.WebSocketMessageHandler {
                                        override fun onNewMessage(webSocketMessage: WebSocketMessageParser.WebSocketMessage?) {
                                            Log.d("ASDK", "$webSocketMessage")
                                            processWebSocketMessage(webSocketMessage)

                                            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                                atlasStatsUpdateWatcher?.onStatsUpdate(atlasStats)
                                            }
                                        }

                                    }
                                connect()
                            }
                    }
                }
            }
        }
    }

    private fun updateConversationStats(conversation: Conversation?) {
        conversation?.let {
            val conversationStats = ConversationStats.fromConversation(conversation)
            val conversationIndex =
                atlasStats.conversations.indexOfFirst { it.id == conversation.id }
            if (conversationIndex == -1) {
                atlasStats.conversations.add(conversationStats);
            } else {
                atlasStats.conversations[conversationIndex] = conversationStats;
            }
        }
    }

    private fun processWebSocketMessage(webSocketMessage: WebSocketMessageParser.WebSocketMessage?) {
        when (webSocketMessage?.packetType) {
            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_CONVERSATION_UPDATED,
            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_AGENT_MESSAGE,
            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_BOT_MESSAGE ->
                updateConversationStats(webSocketMessage.payload?.conversation);
            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_MESSAGE_READ -> {
                webSocketMessage.payload?.conversationId?.takeIf { !it.isNullOrEmpty() }
                    ?.let { conversationId ->
                        val conversationIndex =
                            atlasStats.conversations.indexOfFirst { it.id == conversationId }
                        if (conversationIndex >= 0) {
                            atlasStats.conversations[conversationIndex].unread = 0
                        }
                    }
            }

            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_CHATBOT_WIDGET_RESPONSE ->
                webSocketMessage.payload?.message?.conversationId?.takeIf { !it.isNullOrEmpty() }
                    ?.let { conversationId ->
                        val conversationIndex =
                            atlasStats.conversations.indexOfFirst { it.id == conversationId }
                        if (conversationIndex >= 0) {
                            atlasStats.conversations[conversationIndex].unread++
                        } else {
                            atlasStats.conversations.add(
                                ConversationStats(
                                    conversationId,
                                    1,
                                    false
                                )
                            )
                        }
                    }

            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_CONVERSATION_HIDDEN -> {
                atlasStats.conversations.removeIf { it.id == webSocketMessage.payload?.conversationId }
            }
        }
    }

    fun unWatchStats() {
        atlasStatsUpdateWatcher = null
        webSocketConnectionListener?.close()
    }

    private suspend fun login(): AtlasUser? {
        atlasUser?.let { _atlasUser ->
            if (_atlasUser.id.isNotEmpty()) {
                val response = userRemoteRepository.login(appId, _atlasUser)
                if (response != null && response.isSuccessful) {
                    // todo we may have OR Not have atlasId in the beginning
                    // but now we should have it in response
                    _atlasUser.copy(atlasId = response.id).run {
                        identify(this)

                        //storing identity
                        userLocalRepository.storeIdentity(this)

                        return this
                    }
                }
            }
        }
        return null

    }

    private suspend fun fetchConversations(atlasUser: AtlasUser): List<ConversationStats>? {
        val response = conversationsRemoteRepository.fetchConversations(atlasUser)
        if (response != null && response.isSuccessful) {
            return response.data?.map { ConversationStats.fromConversation(it) }
        }
        return null
    }

    private suspend fun updateCustomFields(ticketId: String, data: Map<String, Any>) {
        atlasUser?.let { _atlasUser ->
            userRemoteRepository.updateCustomFields(_atlasUser, ticketId, data)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        unWatchStats()
    }

    private fun getSharedPreferences(context: Context): DataStore<Preferences> {
        return preferencesDataStore(
            name = PREF_FILE,
            corruptionHandler = null
        ).getValue(context, this::atlasUser)
    }

    const val PREF_FILE = "atlassdk"
    const val PREF_DATA_NAME = "atlassdk"

    interface AtlasStatsUpdateWatcher {
        fun onStatsUpdate(atlasStats: AtlasStats)
    }
}