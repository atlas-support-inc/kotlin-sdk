package com.atlas.sdk

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.atlas.sdk.core.WebSocketConnectionListener
import com.atlas.sdk.core.WebSocketMessageParser
import com.atlas.sdk.data.AtlasMessageHandler
import com.atlas.sdk.data.AtlasStats
import com.atlas.sdk.data.AtlasUser
import com.atlas.sdk.data.Conversation
import com.atlas.sdk.data.ConversationStats
import com.atlas.sdk.data.InternalMessageHandler
import com.atlas.sdk.repository.ConversationsRemoteRepository
import com.atlas.sdk.repository.UserLocalRepository
import com.atlas.sdk.repository.UserRemoteRepository
import com.atlas.sdk.view.AtlasFragment
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.os.Build

@Keep
object AtlasSdk {

    private val gson = Gson()

    private lateinit var userLocalRepository: UserLocalRepository
    private var localBroadcastManager: LocalBroadcastManager? = null
    private val userRemoteRepository = UserRemoteRepository(gson)
    private val conversationsRemoteRepository = ConversationsRemoteRepository(gson)

    internal var appId: String = ""
    internal var chatbotKey: String = ""
    internal var query: String = ""
    internal var atlasUser: AtlasUser? = null
    internal var legacy: Boolean = false
    private val atlasViewFragment: AtlasFragment? = null
    private val executorService: ExecutorService = Executors.newFixedThreadPool(2) // Adjust as needed

    val atlasUserLive: LiveData<AtlasUser?> = MutableLiveData()

    internal val internalAtlasMessageHandler = object : InternalMessageHandler {
        override fun onError(message: String?) {
            // Log.d(TAG, "onError:$message")
            atlasMessageHandlers.forEach {
                it.onError(message)
            }
        }

        override fun onNewTicket(ticketId: String?) {
            // Log.d(TAG, "onNewTicket:$ticketId")
            // ticketId?.let {
            //     GlobalScope.launch {
            //         updateCustomFields(ticketId, mapOf("newTicketIsHere" to ticketId))
            //     }
            // }
            atlasMessageHandlers.forEach {
                it.onNewTicket(ticketId)
            }
        }

        override fun onChangeIdentity(atlasId: String?, userId: String?, userHash: String?) {
            // Log.d(TAG, "onChangeIdentity:$atlasId $userId $userHash")
            val user = if (atlasUser == null)
                AtlasUser(userId ?: "", userHash, atlasId)
            else
                atlasUser!!.apply { this.atlasId = atlasId }

            GlobalScope.launch {
                restore(user)
            }
        }
    }

    private val atlasMessageHandlers = arrayListOf<AtlasMessageHandler>()
    fun addAtlasMessageHandler(atlasMessageHandler: AtlasMessageHandler) {
        atlasMessageHandlers.add(atlasMessageHandler)
    }

    fun removeAtlasMessageHandler(atlasMessageHandler: AtlasMessageHandler) {
        atlasMessageHandlers.remove(atlasMessageHandler)
    }

    fun clearAtlasMessageHandlers() {
        atlasMessageHandlers.clear()
    }

    private var webSocketConnectionListener: WebSocketConnectionListener? = null
    private var atlasStats = AtlasStats()
    val atlasStatsLive: LiveData<AtlasStats?> = MutableLiveData()

    private var atlasStatsUpdateWatcher: AtlasStatsUpdateWatcher? = null
    fun registerAtlasStatsUpdateWatcher(atlasStatsUpdateWatcher: AtlasStatsUpdateWatcher?) {
        this.atlasStatsUpdateWatcher = atlasStatsUpdateWatcher
    }

    fun unregisterAtlasStatsUpdateWatcher() {
        this.atlasStatsUpdateWatcher = null
    }
    
    fun init(context: Application, appId: String) {
        this.appId = appId
        this.legacy = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) true /* SDK version is below 24 */ else false /* Code for SDK version 24 and above */

        this.localBroadcastManager = LocalBroadcastManager.getInstance(context)

        userLocalRepository = UserLocalRepository(getSharedPreferences(context), gson)

        GlobalScope.launch {
            userLocalRepository.loadStoredIdentity()?.let {
                restore(it)
            }
        }
    }

    fun getUser(): AtlasUser? = atlasUser

    private suspend fun restore(user: AtlasUser? = null) {
        coroutineScope {
            if (user == null || user.isEmpty) {
                atlasUser = null
                localBroadcastManager?.sendBroadcast(Intent().apply {
                    action = ON_CHANGE_IDENTITY_ACTION
                    putExtra(AtlasUser::class.java.simpleName, atlasUser)
                })
                withContext(Dispatchers.IO) {
                    userLocalRepository.storeIdentity(AtlasUser.EMPTY_USER)
                }
                unWatchStats()
                return@coroutineScope
            }

            val loggedInUser = login(user)
            val isItNewUser =
                atlasUser?.atlasId.isNullOrEmpty() || loggedInUser?.atlasId != atlasUser?.atlasId
            if (isItNewUser && loggedInUser?.atlasId.isNullOrEmpty().not()) {
                atlasUser = loggedInUser
                (atlasUserLive as MutableLiveData).postValue(atlasUser)

                localBroadcastManager?.sendBroadcast(Intent().apply {
                    action = ON_CHANGE_IDENTITY_ACTION
                    putExtra(AtlasUser::class.java.simpleName, atlasUser)
                })

                atlasMessageHandlers.forEach {
                    it.onChangeIdentity(atlasUser?.atlasId, atlasUser?.id, atlasUser?.hash)
                }

                withContext(Dispatchers.IO) {
                    userLocalRepository.storeIdentity(atlasUser!!)
                }
                watchStats()
            }
        }
    }

    // Visible to Java
    @JvmOverloads
    fun identifyAsync(
        userId: String,
        userHash: String? = null,
        userDetails: Map<String, String?> = emptyMap() // Dictionary for name, email, phoneNumber
    ) {
        val name = userDetails["name"]
        val email = userDetails["email"]
        val phoneNumber = userDetails["phoneNumber"]

        executorService.execute {
            runBlocking {
                identify(userId, userHash, name, email, phoneNumber)
            }
        }
    }

    @JvmSynthetic
    suspend fun identify(userId: String, userHash: String? = null, name: String? = null, email: String? = null, phoneNumber: String? = null) {
        coroutineScope {
            val user = AtlasUser(userId, userHash, null, name, email, phoneNumber)
            restore(user)
        }
    }

    @JvmOverloads
    suspend fun logoutAsync() {
        executorService.execute {
            runBlocking {
                logout()
            }
        }
    }

    @JvmSynthetic
    suspend fun logout() {
        coroutineScope {
            val user = AtlasUser("", null, null, null, null, null)
            restore(user)
        }
    }

    fun getAtlasFragment(query: String = ""): AtlasFragment {
        this.query = query
        if (appId.isEmpty()) {
            println("AtlasSDK Error: App ID cannot be empty.")
        }
        val atlasViewFragment = AtlasFragment()

        return atlasViewFragment
    }

    suspend fun watchStats() {
        coroutineScope {
            atlasUser?.takeIf { it.atlasId.isNullOrEmpty().not() }?.let { user ->
                fetchConversations(user)?.let { conversationStats ->
                    atlasStats.conversations.clear()
                    atlasStats.conversations.addAll(conversationStats)
                    (atlasStatsLive as MutableLiveData).postValue(atlasStats)

                    atlasStatsUpdateWatcher?.onStatsUpdate(atlasStats)
                }

                // only in case this is new user we want to reastablish connection
                if (webSocketConnectionListener?.atlasId != user.atlasId) {
                    webSocketConnectionListener?.close()

                    webSocketConnectionListener =
                        WebSocketConnectionListener(user.atlasId!!, gson).apply {
                            webSocketMessageHandler =
                                object : WebSocketConnectionListener.WebSocketMessageHandler {
                                    override fun onNewMessage(webSocketMessage: WebSocketMessageParser.WebSocketMessage?) {
                                        // Log.d(TAG, "$webSocketMessage")

                                        // we send updates only if there have been changes
                                        if (processWebSocketMessage(webSocketMessage)) {

                                            (atlasStatsLive as MutableLiveData).postValue(
                                                atlasStats
                                            )

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

    fun unWatchStats() {
        atlasStats.conversations.clear()
        (atlasStatsLive as MutableLiveData).postValue(atlasStats)

        atlasStatsUpdateWatcher?.onStatsUpdate(atlasStats)

        webSocketConnectionListener?.close()
    }

    private fun updateConversationStats(conversation: Conversation?) {
        conversation?.let {
            val conversationStats = ConversationStats.fromConversation(conversation)
            val conversationIndex =
                atlasStats.conversations.indexOfFirst { it.id == conversation.id }
            if (conversationIndex == -1) {
                atlasStats.conversations.add(conversationStats)
            } else {
                atlasStats.conversations[conversationIndex] = conversationStats
            }
        }
    }

    private fun processWebSocketMessage(webSocketMessage: WebSocketMessageParser.WebSocketMessage?): Boolean {
        var requireStatsUpdate = true
        when (webSocketMessage?.packetType) {
            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_CONVERSATION_UPDATED -> {
                (webSocketMessage.payload as? Map<String, String>)?.let { payload ->
                    (payload["conversation"] as? Map<String, Any?>)?.let { conversation ->
                        if (conversation["id"] != null) {
                            updateConversationStats(
                                Conversation(
                                    (conversation["id"] as? String) ?: "",
                                    (conversation["messages"] as? ArrayList<LinkedTreeMap<String, Any?>>)?.map {
                                        Conversation.Message(
                                            it["read"] as? Boolean,
                                            (it["read"] as? Double)?.toInt()
                                                ?: Conversation.Message.MessageSide.AGENT.value,
                                            (it["closed"] as? Boolean) ?: false
                                        )
                                    } ?: emptyList<Conversation.Message>(),
                                    conversation["closedAt"] as? String
                                )
                            )
                        }
                    }
                }
            }

            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_BOT_MESSAGE,
            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_AGENT_MESSAGE -> {
                (webSocketMessage.payload as? Map<String, String>)?.let { payload ->
                    gson.fromJson(
                        payload["conversation"],
                        Conversation::class.java
                    )?.let { conversation ->
                        updateConversationStats(conversation)
                    }
                }
            }

            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_MESSAGE_READ -> {
                (webSocketMessage.payload as? Map<String, String>)?.let { payload ->
                    payload["conversationId"]?.takeIf { it.isNotEmpty() }
                        ?.let { conversationId ->
                            val conversationIndex =
                                atlasStats.conversations.indexOfFirst { it.id == conversationId }
                            if (conversationIndex >= 0) {
                                atlasStats.conversations[conversationIndex].unread = 0
                            }
                        }
                }
            }

            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_CHATBOT_WIDGET_RESPONSE -> {
                (webSocketMessage.payload as? Map<String, String>)?.let { payload ->
                    (payload["message"] as? Map<String, String>)?.let { msg ->
                        msg["conversationId"]?.takeIf { it.isNotEmpty() }
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
                    }
                }
            }

            WebSocketMessageParser.WebSocketMessage.PACKET_TYPE_CONVERSATION_HIDDEN -> {
                (webSocketMessage.payload as? Map<String, String>)?.let { payload ->
                    payload["conversationId"]?.takeIf { it.isNotEmpty() }
                        ?.let { conversationId ->
                            atlasStats.conversations.removeAll { it.id == conversationId }
                        }
                }
            }

            else -> requireStatsUpdate = false
        }

        return requireStatsUpdate
    }

    private suspend fun login(user: AtlasUser?): AtlasUser? {
        user?.let { _atlasUser ->
            if (_atlasUser.id.isNotEmpty() && _atlasUser.atlasId.isNullOrEmpty()) {
                val response = userRemoteRepository.login(appId, _atlasUser)
                if (response != null && response.isSuccessful) {
                    // we may have OR may not have atlasId in the beginning
                    // but now we should have it in response
                    return _atlasUser.copy(atlasId = response.id)
                }
            } else {
                return _atlasUser
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

    // Visible to Java
    @JvmOverloads
    fun updateCustomFieldsAsync(
        ticketId: String,
        data:  Map<String, Any>
    ) {
        executorService.submit {
            runBlocking {
                updateCustomFields(ticketId, data)
            }
        }
    }

    @JvmSynthetic
    suspend fun updateCustomFields(ticketId: String, data: Map<String, Any>) {
        atlasUser?.let {
            userRemoteRepository.updateCustomFields(it, ticketId, data)
        }
    }

    private fun getSharedPreferences(context: Context): DataStore<Preferences> {
        return preferencesDataStore(
            name = PREF_FILE,
            corruptionHandler = null
        ).getValue(context, this::atlasUser)
    }

    const val PREF_FILE = "atlassdk"
    const val PREF_DATA_NAME = "atlassdk"

    const val ON_ERROR_ACTION = "ATLAS_ON_ERROR_ACTION"
    const val ON_NEW_TICKET_ACTION = "ATLAS_ON_NEW_TICKET_ACTION"
    const val ON_CHANGE_IDENTITY_ACTION = "ATLAS_ON_CHANGE_IDENTITY_ACTION"

    const val TAG = "AtlasSdk"

    @Keep
    interface AtlasStatsUpdateWatcher {
        fun onStatsUpdate(atlasStats: AtlasStats)
    }

}