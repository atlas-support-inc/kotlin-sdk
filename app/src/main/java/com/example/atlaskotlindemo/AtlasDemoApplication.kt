package com.example.atlaskotlindemo

import android.app.Application
import com.atlas.sdk.AtlasSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AtlasDemoApplication : Application() {

    val atlasSdk : AtlasSdk = AtlasSdk

    override fun onCreate() {
        super.onCreate()

        atlasSdk.init(this@AtlasDemoApplication, appId = "kxjfzvo5pp")

        val user = "14f4771a-c43a-473c-ad22-7d3c5b8dd736"
        CoroutineScope(Dispatchers.IO).launch {
            AtlasSdk.identify(userId = user)
        }
    }
}

//https://embed.atlas.so/?appId=ev9731xvjb&chatbot=n_other_topics