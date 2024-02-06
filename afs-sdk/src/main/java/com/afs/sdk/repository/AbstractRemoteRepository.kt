package com.afs.sdk.repository

import com.google.gson.Gson
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

abstract class AbstractRemoteRepository(val gson: Gson) {

    suspend inline fun <reified A : Any> executeWithResponse(request: Request): A? {
        return coroutineScope {
            try {
                val response = OkHttpClient().newCall(request).execute()
                return@coroutineScope if (response.isSuccessful) {
                    gson.fromJson(
                        response.body?.string(),
                        A::class.java
                    )
                } else {
                    null
                }

            } catch (e: Exception) {
                return@coroutineScope null
            }
        }
    }

    suspend fun execute(request: Request): Response? {
        return coroutineScope {
            try {
                return@coroutineScope OkHttpClient().newCall(request).execute()
            } catch (e: Exception) {
                return@coroutineScope null
            }
        }
    }
}