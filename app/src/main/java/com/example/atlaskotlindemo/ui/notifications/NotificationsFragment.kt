package com.example.atlaskotlindemo.ui.notifications

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.atlas.sdk.data.AtlasMessageHandler
import com.example.atlaskotlindemo.AtlasDemoApplication
import com.example.atlaskotlindemo.databinding.FragmentNotificationsBinding
import androidx.lifecycle.Lifecycle
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var uploadFileCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity().application as AtlasDemoApplication).atlasSdk.bindAtlasView(binding.atlasView)
        binding.atlasView.openPage()

        binding.atlasView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                // Create an Intent for choosing files from the filesystem, including photos and videos.
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

                // Create an Intent for capturing images.
                val capturePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                // Create an Intent for capturing video.
                val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

                val intentList: MutableList<Intent> = ArrayList()
                intentList.add(capturePhotoIntent)
                intentList.add(captureVideoIntent)

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an action")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray())

                // Start the activity to select a file, capture a photo, or record a video.
                startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST_CODE)

                // Keep a reference to the ValueCallback which will receive the result.
                uploadFileCallback = filePathCallback

                return true
            }
        }

        binding.atlasView.setAtlasMessageHandler(
            object : AtlasMessageHandler() {
                override fun onError(message: String?) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d("AtlasView", "onError: $message")
                    }
                }

                override fun onNewTicket(ticketId: String?) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d("AtlasView", "onNewTicket: $ticketId")
                    }
                }

                override fun onChangeIdentity(
                    atlasId: String?,
                    userId: String?,
                    userHash: String?
                ) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d(
                            "AtlasView",
                            "onChangeIdentity: $atlasId $userId $userHash"
                        )
                    }
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (null == uploadFileCallback || resultCode != RESULT_OK || data == null) {
                uploadFileCallback?.onReceiveValue(null)
                uploadFileCallback = null
                return
            }

            // Handle the case where the user captures a photo or video or selects one from the gallery.
            val result = if (data.data != null) arrayOf(data.data!!) else WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            uploadFileCallback?.onReceiveValue(result)
            uploadFileCallback = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}