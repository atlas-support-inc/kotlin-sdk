package com.example.atlaskotlindemo

import android.app.Application
import com.atlas.sdk.AtlasSdk

class AtlasDemoApplication : Application() {

    val atlasSdk : AtlasSdk = AtlasSdk

    override fun onCreate() {
        super.onCreate()

        atlasSdk.init(this@AtlasDemoApplication, "kxjfzvo5pp")
    }
}
