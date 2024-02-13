package com.atlas.sdk.example.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.atlas.sdk.AtlasSdk
import com.atlas.sdk.data.AtlasStats
import com.atlas.sdk.example.databinding.FragmentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val atlasSdk = (requireActivity().application as ExampleApplication).atlasSDK
        atlasSdk.atlasStatsLive.observe(viewLifecycleOwner) { stats ->
            val count = stats?.conversations?.map { it.unread }?.sum() ?: 0
            binding.txtMsgFromObserver.setText("Observer: messages unread: $count, timestamp: ${System.currentTimeMillis()}")
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                atlasSdk.registerAtlasStatsUpdateWatcher(object : AtlasSdk.AtlasStatsUpdateWatcher {
                    override fun onStatsUpdate(atlasStats: AtlasStats) {

                        lifecycleScope.launch(Dispatchers.Main) {
                            val count = atlasStats.conversations.map { it.unread }.sum()
                            binding.txtMsgFromWatcher.setText("Watcher: messages unread: $count, timestamp: ${System.currentTimeMillis()}")
                        }
                    }
                })
            }
        }

        binding.btnConnect.setOnClickListener {
            viewModel.startWatchingStats()
        }
        binding.btnDisconnect.setOnClickListener {
            atlasSdk.unWatchStats()
        }

        binding.btnUser1.setOnClickListener {
            lifecycleScope.launch {
                (requireActivity().application as ExampleApplication).atlasSDK.identify(
                    ExampleApplication.SAMPLE_ATLAS_USER
                )
            }
        }

        binding.btnUserReset.setOnClickListener {
            lifecycleScope.launch {
                (requireActivity().application as ExampleApplication).atlasSDK.identify(null)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // stopping watching the stats globally
        (requireActivity().application as ExampleApplication).atlasSDK.unWatchStats()
    }

    override fun onPause() {
        super.onPause()

        (requireActivity().application as ExampleApplication).atlasSDK.unregisterAtlasStatsUpdateWatcher()

    }
}