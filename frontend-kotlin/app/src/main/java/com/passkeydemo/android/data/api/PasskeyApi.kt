package com.passkeydemo.android.data.api

import com.passkeydemo.android.data.models.*
import retrofit2.http.*

interface PasskeyApi {
    // Registration
    @POST("api/register/begin")
    suspend fun beginRegistration(@Body request: RegistrationRequest): RegistrationOptions
    
    @POST("api/register/finish")
    suspend fun finishRegistration(@Body credential: Map<String, Any>): ApiResponse<UserProfile>
    
    // Authentication
    @POST("api/login/begin")
    suspend fun beginAuthentication(@Body request: AuthenticationRequest): AuthenticationOptions
    
    @POST("api/login/finish")
    suspend fun finishAuthentication(@Body credential: Map<String, Any>): ApiResponse<UserProfile>
    
    // User Management
    @GET("api/user/profile")
    suspend fun getUserProfile(): UserProfile
    
    @GET("api/user/passkeys")
    suspend fun getUserPasskeys(): PasskeysResponse
    
    @DELETE("api/user/passkeys/{id}")
    suspend fun deletePasskey(@Path("id") id: String): ApiResponse<Unit>
    
    @POST("api/logout")
    suspend fun logout(): ApiResponse<Unit>
    
    // Health check
    @GET("api/health")
    suspend fun healthCheck(): Map<String, Any>
}