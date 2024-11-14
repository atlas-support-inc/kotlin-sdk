package com.atlas.sdk.repository

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

abstract class AbstractRemoteRepository(val gson: Gson) {

    suspend inline fun <reified A : Any> executeWithResponse(request: Request): A? {
        return coroutineScope {
            withContext(Dispatchers.IO) {
                try {
                    val response = OkHttpClient().newCall(request).execute()
                    return@withContext if (response.isSuccessful) {
                        gson.fromJson(
                            response.body?.string(),
                            A::class.java
                        )
                    } else {
                        null
                    }

                } catch (e: Exception) {
                    return@withContext null
                }
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