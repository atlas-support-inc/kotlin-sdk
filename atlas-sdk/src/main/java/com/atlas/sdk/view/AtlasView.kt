package com.atlas.sdk.view

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.atlas.sdk.AtlasSdk
import com.atlas.sdk.core.Config
import com.atlas.sdk.data.AtlasMessageHandler
import com.atlas.sdk.data.AtlasUser
import com.atlas.sdk.data.InternalMessageHandler
import com.google.gson.Gson
import java.io.File

@Keep
@SuppressLint("SetJavaScriptEnabled")
class AtlasView : WebView {

    private lateinit var filePickerLifeCycleObserver: FilePickerLifeCycleObserver
    private fun bindToLifeCycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(filePickerLifeCycleObserver)
    }

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
        webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                if (::filePickerLifeCycleObserver.isInitialized) {
                    filePickerLifeCycleObserver.pickFile(context.applicationContext, filePathCallback)
                }

                return true
            }
        }
        with(settings) {
            javaScriptEnabled = true
            databaseEnabled = true
            domStorageEnabled = true
        }
        addJavascriptInterface(atlasAppInterface, "FlutterWebView")

        (context as? FragmentActivity)?.activityResultRegistry?.let { registry ->
            filePickerLifeCycleObserver = FilePickerLifeCycleObserver(registry)
        }
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

    private class FilePickerLifeCycleObserver(private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {
        private var photoCaptureUri: Uri? = null
        lateinit var getContent: ActivityResultLauncher<Intent>
        private var uploadFileCallback: ValueCallback<Array<Uri>>? = null

        override fun onCreate(owner: LifecycleOwner) {
            getContent = registry.register(
                "AtlasView",
                owner,
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                if (null == uploadFileCallback || result.resultCode != RESULT_OK || result.data == null) {
                    uploadFileCallback?.onReceiveValue(null)
                    uploadFileCallback = null
                    return@register
                }

                // Handle the case where the user captures a photo or video or selects one from the gallery.
                var result =
                    if (result.data?.data != null)
                        arrayOf(result.data?.data!!)
                    else
                        WebChromeClient.FileChooserParams.parseResult(
                            result.resultCode,
                            result.data
                        )
                result = if (result.isNullOrEmpty()) arrayOf(photoCaptureUri) else result
                uploadFileCallback?.onReceiveValue(result)
                uploadFileCallback = null
            }
        }

        fun pickFile(context: Context, filePathCallback: ValueCallback<Array<Uri>>?) {
            this.uploadFileCallback = filePathCallback
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "*/*"
            contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            // Create an Intent for capturing images.
            val storageDir: File? = context.externalCacheDir?.absoluteFile
            val file = File.createTempFile(
                PHOTO_FILE_NAME_PREFIX,
                ".jpg",
                storageDir
            )

            photoCaptureUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val capturePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            capturePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            capturePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoCaptureUri)

            // Create an Intent for capturing video.
            val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            captureVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            val intentList: MutableList<Intent> = ArrayList()
            intentList.add(capturePhotoIntent)
            intentList.add(captureVideoIntent)

            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an action")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray())
            getContent.launch(chooserIntent)
        }

        companion object {
            const val PHOTO_FILE_NAME_PREFIX = "atlas_photo_capture_"
        }
    }
}
