package com.atlas.sdk.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    fun startWatchingStats() {
        viewModelScope.launch {
            (getApplication<ExampleApplication>()).atlasSDK.watchStats()
        }
    }
}