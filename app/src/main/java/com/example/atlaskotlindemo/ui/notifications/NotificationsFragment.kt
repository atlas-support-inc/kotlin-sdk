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
import com.atlas.sdk.AtlasSdk

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    private val binding get() = _binding!!

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

        // Step 1: Create AtlasFragment
        val atlasFragment = AtlasSdk.getAtlasFragment(query = "chatbotKey: n_other_topics; prefer: last", legacy = true)

        // Step 2: Replace the current fragment with AtlasFragment in full screen
        childFragmentManager.beginTransaction()
            .replace(binding.fragmentContainerView.id, atlasFragment)
            .commitNow()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}