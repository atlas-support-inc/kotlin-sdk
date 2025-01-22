package com.atlas.sdk.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.atlas.sdk.AtlasSdk
import com.atlas.sdk.data.AtlasMessageHandler
import com.atlas.sdk.data.AtlasUser
import com.atlas.sdk.databinding.FragmentAtlasViewBinding
import com.atlas.sdk.BuildConfig

class AtlasFragment : Fragment() {

    private var _binding: FragmentAtlasViewBinding? = null
    private val binding get() = _binding!!

    private var receiver: BroadcastReceiver? = null
    private var intentFilter = IntentFilter().apply {
        addAction(AtlasSdk.ON_CHANGE_IDENTITY_ACTION)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAtlasViewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appId: String = AtlasSdk.appId
        val user: AtlasUser? = AtlasSdk.atlasUser
        val query = AtlasSdk.query
        val sdkVersionName = "kotlin@" + BuildConfig.VERSION_NAME

        binding.atlasView.applyConfig(appId, user)
        binding.atlasView.setQuery(query)
        binding.atlasView.setSdkVersionName(sdkVersionName)
        binding.atlasView.setSdkAtlasMessageHandler(AtlasSdk.internalAtlasMessageHandler)
        binding.atlasView.bindToLifeCycle(lifecycle)
        binding.atlasView.openPage()

        configureBroadcastReceiver()
    }

    private fun configureBroadcastReceiver() {
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
                            if (AtlasSdk.atlasUser?.atlasId != intentUser.atlasId) {
                                binding.atlasView.setUser(intentUser)
                                binding.atlasView.openPage()
                            } else {
                                binding.atlasView.setUser(intentUser)
                            }
                        }

                    }
                }
            }
        }
        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(receiver!!, intentFilter) }
    }

    override fun onDetach() {
        super.onDetach()

        receiver?.let {
            context?.let { it1 -> LocalBroadcastManager.getInstance(it1).unregisterReceiver(it) }
        }
        receiver = null

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}