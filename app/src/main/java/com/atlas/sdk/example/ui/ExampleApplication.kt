package com.atlas.sdk.example.ui

import android.app.Application
import com.atlas.sdk.AtlasSdk
import com.atlas.sdk.data.AtlasUser

class ExampleApplication : Application() {
    val atlasSDK: AtlasSdk = AtlasSdk

    override fun onCreate() {
        super.onCreate()

        atlasSDK.init(this@ExampleApplication, SAMPLE_APP_ID)
    }

    companion object {
        const val SAMPLE_APP_ID = "kxjfzvo5pp"//7wukb9ywp9__x33kupocfb"
        val SAMPLE_ATLAS_USER = AtlasUser(
            "1e9322f7-fa86-400d-bf84-4cb64a981910",
            "5d88e73eeba85abf97aec8d390e9ab0e467bd7b212a2bcca1c3fbcaa8972ad01",
            ""
        )

    }

}
