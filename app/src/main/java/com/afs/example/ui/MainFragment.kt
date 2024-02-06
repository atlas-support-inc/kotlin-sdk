package com.afs.example.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.afs.example.databinding.FragmentMainBinding
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
        binding.btnConnect.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                atlasSdk.watchStats(lifecycle)
            }
        }
        binding.btnDisconnect.setOnClickListener {
            atlasSdk.unWatchStats()
        }
    }

    override fun onPause() {
        super.onPause()

        (requireActivity().application as ExampleApplication).atlasSDK.unWatchStats()
    }
}