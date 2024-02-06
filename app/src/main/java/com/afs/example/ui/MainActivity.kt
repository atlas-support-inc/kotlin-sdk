package com.afs.example.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import androidx.databinding.DataBindingUtil
import com.afs.example.R
import com.afs.example.databinding.ActivityMainBinding
import com.afs.example.databinding.DialogAtlasWebviewBinding
import kotlinx.parcelize.Parcelize

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
            (application as ExampleApplication).atlasSDK.identify(ExampleApplication.SAMPLE_ATLAS_USER)

            val dialog = WebViewBottomSheetDialog()
            dialog.show(supportFragmentManager, WebViewBottomSheetDialog::class.simpleName)
        }
    }


}