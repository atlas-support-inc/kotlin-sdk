package com.atlas.sdk.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.atlas.sdk.data.AtlasMessageHandler
import com.atlas.sdk.databinding.FragmentAtlasViewBinding

class AtlasViewFragment : Fragment() {

    private var _binding: FragmentAtlasViewBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentAtlasViewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.atlasView.bindToLifeCycle(lifecycle)
        binding.atlasView.openPage()

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
                        // (requireActivity().application as AtlasDemoApplication).atlasSdk.updateCustomFields(ticketId, mapOf("customField" to "customValue")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}