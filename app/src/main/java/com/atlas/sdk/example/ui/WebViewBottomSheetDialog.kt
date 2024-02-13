package com.atlas.sdk.example.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.atlas.sdk.data.AtlasJsMessageHandler
import com.atlas.sdk.example.databinding.DialogAtlasWebviewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WebViewBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogAtlasWebviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAtlasWebviewBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity().application as ExampleApplication).atlasSDK.bindAtlasWebView(binding.webview)

        binding.webview.openPage()
    }

    override fun onResume() {
        super.onResume()

        binding.webview.setAtlasJsMessageHandler(
            object : AtlasJsMessageHandler(lifecycle) {
                override fun onError(message: String?) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d("WebViewBottomSheetDialog", "onError: $message")
                    }
                }

                override fun onNewTicket(ticketId: String?) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d("WebViewBottomSheetDialog", "onNewTicket: $ticketId")
                    }
                }

                override fun onChangeIdentity(
                    atlasId: String?,
                    userId: String?,
                    userHash: String?
                ) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d(
                            "WebViewBottomSheetDialog",
                            "onChangeIdentity: $atlasId $userId $userHash"
                        )
                    }
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()

        binding.webview.removeAtlasJsMessageHandler()
    }

}