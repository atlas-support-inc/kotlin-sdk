package com.atlas.sdk.example.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.atlas.sdk.data.AtlasMessageHandler
import com.atlas.sdk.example.databinding.DialogAtlasViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AtlasViewBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogAtlasViewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAtlasViewBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity().application as ExampleApplication).atlasSDK.bindAtlasView(binding.atlasView)

        binding.atlasView.openPage()
    }

    override fun onResume() {
        super.onResume()

        binding.atlasView.setAtlasMessageHandler(
            object : AtlasMessageHandler() {
                override fun onError(message: String?) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d("AtlasViewBottomSheetDialog", "onError: $message")
                    }
                }

                override fun onNewTicket(ticketId: String?) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d("AtlasViewBottomSheetDialog", "onNewTicket: $ticketId")
                    }
                }

                override fun onChangeIdentity(
                    atlasId: String?,
                    userId: String?,
                    userHash: String?
                ) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d(
                            "AtlasViewBottomSheetDialog",
                            "onChangeIdentity: $atlasId $userId $userHash"
                        )
                    }
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()

        binding.atlasView.removeAtlasMessageHandler()
    }

}