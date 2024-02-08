package com.afs.example.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.afs.example.databinding.FragmentMainBinding
import com.afs.sdk.AtlasSdk
import com.afs.sdk.data.AtlasStats
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
        viewLifecycleOwner.lifecycle.addObserver(atlasSdk)

        binding.btnConnect.setOnClickListener {
            lifecycleScope.launch {
                atlasSdk.watchStats(lifecycle)
            }
        }
        binding.btnDisconnect.setOnClickListener {
            atlasSdk.unWatchStats()
        }
        atlasSdk.atlasStatsUpdateWatcher = object : AtlasSdk.AtlasStatsUpdateWatcher {
            override fun onStatsUpdate(atlasStats: AtlasStats) {
                val count = atlasStats.conversations.map { it.unread }.sum()
                binding.messagesCount.setText("Messages unread: $count")
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewLifecycleOwner.lifecycle.addObserver((requireActivity().application as ExampleApplication).atlasSDK)
    }

    override fun onPause() {
        super.onPause()

//        (requireActivity().application as ExampleApplication).atlasSDK.unWatchStats()
    }
}