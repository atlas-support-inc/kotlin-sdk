package com.example.atlaskotlindemo

import android.app.Application
import com.atlas.sdk.AtlasSdk

class AtlasDemoApplication : Application() {

    val atlasSdk : AtlasSdk = AtlasSdk

    override fun onCreate() {
        super.onCreate()

        atlasSdk.setAppId("kxjfzvo5pp")
        atlasSdk.init(this@AtlasDemoApplication)
    }
}
