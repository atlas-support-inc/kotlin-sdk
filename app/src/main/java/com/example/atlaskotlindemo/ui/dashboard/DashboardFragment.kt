package com.example.atlaskotlindemo.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.atlaskotlindemo.AtlasDemoApplication
import com.example.atlaskotlindemo.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.counterMessage
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val atlasSdk = (requireActivity().application as AtlasDemoApplication).atlasSdk
        atlasSdk.atlasStatsLive.observe(viewLifecycleOwner, { stats ->
            val count = stats?.conversations?.map { it.unread }?.sum() ?: 0
            binding.counterMessage.text = if (count == 0) {
                "You have no messages"
            } else {
                "You have $count unread messages"
            }
        })

        val user = atlasSdk.getUser()
        if (user != null && user.id.isNotEmpty()) {
            binding.loginField.setText(user.id)
        }

        binding.loginButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val id = binding.loginField.text.toString()
                if (id.isEmpty()) {
                    return@launch
                }

                atlasSdk.identify(userId = id)
                Log.d("Atlas", "Logged in as $id")
            }
        }

        binding.logoutButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                atlasSdk.identify()
                binding.loginField.text.clear()
            }
        }
        

//        val saveButton: Button = binding.saveButton
//        saveButton.setOnClickListener { view ->
//            viewLifecycleOwner.lifecycleScope.launch {
//                val customFieldNameEditText: EditText = binding.customFieldName
//                val customFieldValueEditText: EditText = binding.customFieldValue
//
//                val customFieldName = customFieldNameEditText.text.toString()
//                val customFieldValue = customFieldValueEditText.text.toString()
//
//                println("Custom Field Name: $customFieldName")
//                println("Custom Field Value: $customFieldValue")
//                //(requireActivity().application as AtlasDemoApplication).atlasSdk.updateCustomFields("", mapOf(customFieldName to customFieldValue))
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}