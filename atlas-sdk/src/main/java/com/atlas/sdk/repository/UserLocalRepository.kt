package com.atlas.sdk.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.atlas.sdk.AtlasSdk
import com.atlas.sdk.data.AtlasUser
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

class UserLocalRepository(private val dataStore: DataStore<Preferences>, gson: Gson) : AbstractLocalRepository(gson) {

    fun storeIdentity(atlasUser: AtlasUser): CompletableFuture<Void> {
        val completableFuture = CompletableFuture<Void>()
            GlobalScope.launch(Dispatchers.IO) {
                val prefField = stringPreferencesKey(AtlasSdk.PREF_DATA_NAME)
                dataStore.edit { prefs ->
                    prefs[prefField] = gson.toJson(atlasUser)
                    completableFuture.complete(null)
                }
            }
        return completableFuture
    }

    suspend fun loadStoredIdentity(): AtlasUser? {
        val prefField = stringPreferencesKey(AtlasSdk.PREF_DATA_NAME)
        return gson.fromJson(this.dataStore.data.first()[prefField], AtlasUser::class.java)
    }

}