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
import com.atlas.sdk.data.AtlasJsMessageHandler
import com.atlas.sdk.data.AtlasUser
import com.atlas.sdk.data.InternalJsMessageHandler
import com.atlas.sdk.data.WebViewJsMessage
import com.google.gson.Gson

@Keep
@SuppressLint("SetJavaScriptEnabled")
class AtlasWebView : WebView {

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

    private var sdkAtlasJsMessageHandler: InternalJsMessageHandler? = null
    fun setSdkAtlasJsMessageHandler(atlasJsMessageHandler: InternalJsMessageHandler?) {
        this.sdkAtlasJsMessageHandler = atlasJsMessageHandler
    }

    private var atlasJsMessageHandler: AtlasJsMessageHandler? = null
    fun setAtlasJsMessageHandler(atlasJsMessageHandler: AtlasJsMessageHandler?) {
        this.atlasJsMessageHandler = atlasJsMessageHandler
    }

    fun removeAtlasJsMessageHandler() {
        this.atlasJsMessageHandler = null
    }

    private val gson = Gson()
    private val atlasWebViewAppInterface = object : AtlasWebViewAppInterface {

        @JavascriptInterface
        override fun postMessage(message: String) {
            try {
                val webViewJsMessage = gson.fromJson(message, WebViewJsMessage::class.java)
                when (webViewJsMessage.type) {
                    Config.MESSAGE_TYPE_ERROR -> {
                        sdkAtlasJsMessageHandler?.onError(webViewJsMessage.errorMessage)
                        atlasJsMessageHandler?.onError(webViewJsMessage.errorMessage)
                    }

                    Config.MESSAGE_TYPE_NEW_TICKET -> {
                        sdkAtlasJsMessageHandler?.onNewTicket(webViewJsMessage.ticketId)
                        atlasJsMessageHandler?.onNewTicket(webViewJsMessage.ticketId)
                    }

                    Config.MESSAGE_TYPE_CHANGE_IDENTITY -> {
                        sdkAtlasJsMessageHandler?.onChangeIdentity(
                            webViewJsMessage.atlasId,
                            webViewJsMessage.userId,
                            webViewJsMessage.userHash
                        )
                        atlasJsMessageHandler?.onChangeIdentity(
                            webViewJsMessage.atlasId,
                            webViewJsMessage.userId,
                            webViewJsMessage.userHash
                        )
                    }

                    else -> sdkAtlasJsMessageHandler?.onError(message)
                }
            } catch (e: Exception) {
                sdkAtlasJsMessageHandler?.onError("js message: $message, exception: ${e.message}")
                atlasJsMessageHandler?.onError("js message: $message, exception: ${e.message}")
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
        addJavascriptInterface(atlasWebViewAppInterface, "FlutterWebView")
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

        removeAtlasJsMessageHandler()
        loadUrl("file://")
        removeJavascriptInterface("FlutterWebView")
    }

    @Keep
    interface AtlasWebViewAppInterface {

        @JavascriptInterface
        fun postMessage(message: String)

    }
}
