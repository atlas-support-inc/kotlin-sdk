package com.afs.sdk.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.afs.sdk.AtlasSdk
import com.afs.sdk.data.AtlasUser
import com.google.gson.Gson
import kotlinx.coroutines.flow.first

class UserLocalRepository(private val dataStore: DataStore<Preferences>, gson: Gson) : AbstractLocalRepository(gson) {

    suspend fun storeIdentity(atlasUser: AtlasUser) {
        val prefField = stringPreferencesKey(AtlasSdk.PREF_DATA_NAME)
        dataStore.edit { prefs ->
            prefs[prefField] = gson.toJson(atlasUser)
        }
    }

    suspend fun loadStoredIdentity(): AtlasUser? {
        val prefField = stringPreferencesKey(AtlasSdk.PREF_DATA_NAME)
        return gson.fromJson(this.dataStore.data.first()[prefField], AtlasUser::class.java)
    }

}