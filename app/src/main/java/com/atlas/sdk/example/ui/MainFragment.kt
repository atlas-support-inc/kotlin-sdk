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
import com.atlas.sdk.example.databinding.FragmentMainBinding
import com.atlas.sdk.AtlasSdk
import com.atlas.sdk.data.AtlasStats
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

        // you can use lifecycle observer to make sure app does not receive anything from the Atlas SDK
        viewLifecycleOwner.lifecycle.addObserver(atlasSdk)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                atlasSdk.atlasStatsUpdateWatcher = object : AtlasSdk.AtlasStatsUpdateWatcher {
                    override fun onStatsUpdate(atlasStats: AtlasStats) {

                        val count = atlasStats.conversations.map { it.unread }.sum()
                        binding.messagesCount.setText("Messages unread: $count")
                    }
                }
            }
        }

        binding.btnConnect.setOnClickListener {
            lifecycleScope.launch {
                atlasSdk.watchStats(lifecycle)
            }
        }
        binding.btnDisconnect.setOnClickListener {
            atlasSdk.unWatchStats()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        // don't forget to remove the observer
        viewLifecycleOwner.lifecycle.removeObserver((requireActivity().application as ExampleApplication).atlasSDK)
    }

    override fun onPause() {
        super.onPause()

        // OR you can manually stop watching for the stats
//        (requireActivity().application as ExampleApplication).atlasSDK.unWatchStats()
    }
}