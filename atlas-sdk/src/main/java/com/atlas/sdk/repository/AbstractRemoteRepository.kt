package com.atlas.sdk.repository

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.CompletableFuture

abstract class AbstractRemoteRepository(val gson: Gson) {

    inline fun <reified A : Any> executeWithResponse(request: Request): CompletableFuture<A?> {
        return CompletableFuture.supplyAsync {
                try {
                    val response = OkHttpClient().newCall(request).execute()
                    return@supplyAsync if (response.isSuccessful) {
                        gson.fromJson(
                            response.body?.string(),
                            A::class.java
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    return@supplyAsync null
                }
        }
    }

    suspend fun execute(request: Request): Response? {
        return coroutineScope {
            withContext(Dispatchers.IO) {
                try {
                    return@withContext OkHttpClient().newCall(request).execute()
                } catch (e: Exception) {
                    return@withContext null
                }
            }
        }
    }
}