package com.passkeydemo.android.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.passkeydemo.android.data.api.PasskeyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()
        
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }
        
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        // Check for ngrok configuration
        val ngrokConfigFile = File(context.filesDir.parentFile?.parentFile, "ngrok-config.json")
        val baseUrl = if (ngrokConfigFile.exists()) {
            try {
                val config = ngrokConfigFile.readText()
                val ngrokUrl = Json.parseToJsonElement(config)
                    .jsonObject["ngrok_url"]
                    ?.toString()
                    ?.replace("\"", "")
                    ?: getDefaultBaseUrl()
                "$ngrokUrl/"
            } catch (e: Exception) {
                getDefaultBaseUrl()
            }
        } else {
            getDefaultBaseUrl()
        }
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    @Provides
    @Singleton
    fun providePasskeyApi(retrofit: Retrofit): PasskeyApi {
        return retrofit.create(PasskeyApi::class.java)
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