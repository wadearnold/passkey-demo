package com.passkeydemo.android.data.api

import android.content.Context
import android.util.Log
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.File

class DynamicBaseUrlInterceptor(
    private val context: Context
) : Interceptor {
    companion object {
        private const val TAG = "DynamicUrlInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get the current base URL dynamically
        val currentBaseUrl = getBaseUrl()
        val newUrl = currentBaseUrl.toHttpUrlOrNull()
        
        if (newUrl != null) {
            // Replace the base URL with the current one
            val newRequest = originalRequest.newBuilder()
                .url(
                    originalRequest.url.newBuilder()
                        .scheme(newUrl.scheme)
                        .host(newUrl.host)
                        .port(newUrl.port)
                        .build()
                )
                .build()
            
            Log.d(TAG, "Request URL: ${newRequest.url}")
            return chain.proceed(newRequest)
        }
        
        return chain.proceed(originalRequest)
    }
    
    private fun getBaseUrl(): String {
        // First check SharedPreferences for ngrok URL
        val prefs = context.getSharedPreferences("passkey_demo", Context.MODE_PRIVATE)
        val ngrokUrl = prefs.getString("ngrok_url", null)
        
        if (!ngrokUrl.isNullOrEmpty()) {
            Log.d(TAG, "Using ngrok URL from preferences: $ngrokUrl")
            return if (ngrokUrl.endsWith("/")) ngrokUrl else "$ngrokUrl/"
        }
        
        // Fall back to default
        val defaultUrl = getDefaultBaseUrl()
        Log.d(TAG, "Using default URL: $defaultUrl")
        return defaultUrl
    }
    
    private fun getDefaultBaseUrl(): String {
        // For Android emulator, use 10.0.2.2 to access host machine's localhost
        return if (android.os.Build.FINGERPRINT.contains("generic")) {
            "http://10.0.2.2:8080/"
        } else {
            // For physical devices, you'll need to use your machine's IP or ngrok
            "http://localhost:8080/"
        }
    }
}