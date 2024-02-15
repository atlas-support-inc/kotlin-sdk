package com.atlas.sdk.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Keep
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.atlas.sdk.AtlasSdk
import com.atlas.sdk.core.Config
import com.atlas.sdk.data.AtlasMessageHandler
import com.atlas.sdk.data.AtlasUser
import com.atlas.sdk.data.InternalMessageHandler
import com.google.gson.Gson

@Keep
@SuppressLint("SetJavaScriptEnabled")
class AtlasView : WebView {

    private var intentFilter = IntentFilter().apply {
        addAction(AtlasSdk.ON_CHANGE_IDENTITY_ACTION)
    }
    private var receiver: BroadcastReceiver? = null

    private var appId: String = ""
    fun setAppId(appId: String) {
        this.appId = appId
    }

    private var atlasUser: AtlasUser? = null
    fun setUser(user: AtlasUser?) {
        this.atlasUser = user
    }

    private var sdkAtlasMessageHandler: InternalMessageHandler? = null
    @Keep
    private fun setSdkAtlasMessageHandler(atlasMessageHandler: InternalMessageHandler?) {
        this.sdkAtlasMessageHandler = atlasMessageHandler
    }

    private var atlasMessageHandler: AtlasMessageHandler? = null
    fun setAtlasMessageHandler(atlasMessageHandler: AtlasMessageHandler?) {
        this.atlasMessageHandler = atlasMessageHandler
    }

    fun removeAtlasMessageHandler() {
        this.atlasMessageHandler = null
    }

    private val gson = Gson()
    private val atlasAppInterface = object : AtlasAppInterface {

        @JavascriptInterface
        override fun postMessage(message: String) {
            try {
                val atlasEndpointCallbackMessage = gson.fromJson(message, AtlasEndpointCallbackMessage::class.java)
                when (atlasEndpointCallbackMessage.type) {
                    Config.MESSAGE_TYPE_ERROR -> {
                        sdkAtlasMessageHandler?.onError(atlasEndpointCallbackMessage.errorMessage)
                        atlasMessageHandler?.onError(atlasEndpointCallbackMessage.errorMessage)
                    }

                    Config.MESSAGE_TYPE_NEW_TICKET -> {
                        sdkAtlasMessageHandler?.onNewTicket(atlasEndpointCallbackMessage.ticketId)
                        atlasMessageHandler?.onNewTicket(atlasEndpointCallbackMessage.ticketId)
                    }

                    Config.MESSAGE_TYPE_CHANGE_IDENTITY -> {
                        sdkAtlasMessageHandler?.onChangeIdentity(
                            atlasEndpointCallbackMessage.atlasId,
                            atlasEndpointCallbackMessage.userId,
                            atlasEndpointCallbackMessage.userHash
                        )
                        atlasMessageHandler?.onChangeIdentity(
                            atlasEndpointCallbackMessage.atlasId,
                            atlasEndpointCallbackMessage.userId,
                            atlasEndpointCallbackMessage.userHash
                        )
                    }

                    else -> sdkAtlasMessageHandler?.onError(message)
                }
            } catch (e: Exception) {
                sdkAtlasMessageHandler?.onError("message: $message, exception: ${e.message}")
                atlasMessageHandler?.onError("message: $message, exception: ${e.message}")
            }
        }
    }

    init {
        webViewClient = WebViewClient()
        webChromeClient = WebChromeClient()
        with(settings) {
            javaScriptEnabled = true
            databaseEnabled = true
            domStorageEnabled = true
        }
        addJavascriptInterface(atlasAppInterface, "FlutterWebView")
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        privateBrowsing: Boolean
    ) : super(context, attrs, defStyleAttr, privateBrowsing)

    fun applyConfig(appId: String, user: AtlasUser?) {
        setAppId(appId)
        setUser(user)
    }

    fun openPage() {
        val uri = Uri.parse(Config.ATLAS_WIDGET_BASE_URL)
        loadUrl(
            Uri.Builder().scheme(uri.scheme).authority(uri.authority)
                .appendQueryParameter(Config.PARAM_APP_ID, appId)
                .appendQueryParameter(Config.PARAM_ATLAS_ID, atlasUser?.atlasId ?: "")
                .appendQueryParameter(Config.PARAM_USER_ID, atlasUser?.id ?: "")
                .appendQueryParameter(Config.PARAM_USER_HASH, atlasUser?.hash ?: "")
                .appendQueryParameter(Config.PARAM_USER_NAME, atlasUser?.name ?: "")
                .appendQueryParameter(Config.PARAM_USER_EMAIL, atlasUser?.email ?: "").build()
                .toString()
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    AtlasSdk.ON_CHANGE_IDENTITY_ACTION -> {
                        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.extras?.getParcelable(
                                AtlasUser::class.java.simpleName,
                                AtlasUser::class.java
                            )
                        } else {
                            intent.extras?.getParcelable<AtlasUser>(AtlasUser::class.java.simpleName)
                        })?.let { intentUser ->
                            if (atlasUser?.atlasId != intentUser.atlasId) {
                                setUser(intentUser)
                                openPage()
                            } else {
                                setUser(intentUser)
                            }
                        }

                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver!!, intentFilter)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        receiver?.let {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(it)
        }
        receiver = null

        removeAtlasMessageHandler()
        loadUrl("file://")
        removeJavascriptInterface("FlutterWebView")
    }

    @Keep
    interface AtlasAppInterface {

        @JavascriptInterface
        fun postMessage(message: String)

    }

    @Keep
    private data class AtlasEndpointCallbackMessage(
        val type: String,
        val errorMessage: String?,
        val ticketId: String?,
        val atlasId: String?,
        val userId: String?,
        val userHash: String?
    )
}
