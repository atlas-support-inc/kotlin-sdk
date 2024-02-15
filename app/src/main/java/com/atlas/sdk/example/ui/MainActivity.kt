package com.atlas.sdk.example.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.atlas.sdk.example.R
import com.atlas.sdk.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }

        binding.btnFab.setOnClickListener {
            val dialog = AtlasViewBottomSheetDialog()
            dialog.show(supportFragmentManager, AtlasViewBottomSheetDialog::class.simpleName)
        }
    }


}