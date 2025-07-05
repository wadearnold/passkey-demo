package com.passkeydemo.android.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.*
import com.passkeydemo.android.data.api.PasskeyApi
import com.passkeydemo.android.data.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasskeyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: PasskeyApi
) {
    private val tag = "PasskeyRepository"
    private val credentialManager = CredentialManager.create(context)
    
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Registration flow
    suspend fun register(username: String, displayName: String): Result<UserProfile> {
        return try {
            _error.value = null
            
            // Step 1: Get registration options from server
            Log.d(tag, "Requesting registration options for username: $username, displayName: $displayName")
            val options = api.beginRegistration(RegistrationRequest(username, displayName))
            Log.d(tag, "Registration options received: $options")
            Log.d(tag, "PublicKey options: ${options.publicKey}")
            
            // Step 2: Create passkey using Credential Manager
            val request = createRegistrationRequest(options.publicKey)
            val result = credentialManager.createCredential(context, request)
            
            // Step 3: Send credential to server
            val credential = result as? CreatePublicKeyCredentialResponse
                ?: throw Exception("Invalid credential response type")
            val response = api.finishRegistration(parseRegistrationResponse(credential))
            
            if (response.success && response.data != null) {
                _currentUser.value = response.data
                Result.success(response.data)
            } else {
                throw Exception(response.error ?: "Registration failed")
            }
        } catch (e: CreateCredentialCancellationException) {
            Log.e(tag, "User cancelled registration", e)
            _error.value = "Registration cancelled"
            Result.failure(Exception("Registration cancelled"))
        } catch (e: CreateCredentialNoCreateOptionException) {
            Log.e(tag, "No create options available - likely Google Play Services issue", e)
            _error.value = "Passkeys not available. Please check:\n• Google Play Services is updated\n• Screen lock is enabled\n• Not using an old emulator"
            Result.failure(e)
        } catch (e: CreateCredentialException) {
            Log.e(tag, "Failed to create credential: ${e.message}", e)
            _error.value = "Failed to create passkey: ${e.message}"
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(tag, "Registration error: ${e.message}", e)
            Log.e(tag, "Stack trace:", e)
            _error.value = e.message ?: "Unknown error occurred"
            Result.failure(e)
        }
    }
    
    // Authentication with username
    suspend fun authenticate(username: String): Result<UserProfile> {
        return try {
            _error.value = null
            
            // Step 1: Get authentication options from server
            val options = api.beginAuthentication(AuthenticationRequest(username))
            Log.d(tag, "Authentication options received for $username")
            
            // Step 2: Get credential using Credential Manager
            val request = createAuthenticationRequest(options.publicKey)
            val result = credentialManager.getCredential(context, request)
            
            // Step 3: Send credential to server
            val credential = result.credential as? PublicKeyCredential
                ?: throw Exception("Invalid credential response type")
            val response = api.finishAuthentication(parseAuthenticationResponse(credential))
            
            if (response.success && response.data != null) {
                _currentUser.value = response.data
                Result.success(response.data)
            } else {
                throw Exception(response.error ?: "Authentication failed")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.e(tag, "User cancelled authentication", e)
            _error.value = "Authentication cancelled"
            Result.failure(Exception("Authentication cancelled"))
        } catch (e: NoCredentialException) {
            Log.e(tag, "No credentials found", e)
            _error.value = "No passkeys found for this user"
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e(tag, "Failed to get credential", e)
            _error.value = "Failed to authenticate: ${e.message}"
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(tag, "Authentication error", e)
            _error.value = e.message
            Result.failure(e)
        }
    }
    
    // Discoverable authentication (no username)
    suspend fun authenticateDiscoverable(): Result<UserProfile> {
        return try {
            _error.value = null
            
            // Step 1: Get authentication options from server (no username)
            val options = api.beginAuthentication(AuthenticationRequest())
            Log.d(tag, "Discoverable authentication options received")
            
            // Step 2: Get credential using Credential Manager
            val request = createAuthenticationRequest(options.publicKey)
            val result = credentialManager.getCredential(context, request)
            
            // Step 3: Send credential to server
            val credential = result.credential as? PublicKeyCredential
                ?: throw Exception("Invalid credential response type")
            val response = api.finishAuthentication(parseAuthenticationResponse(credential))
            
            if (response.success && response.data != null) {
                _currentUser.value = response.data
                Result.success(response.data)
            } else {
                throw Exception(response.error ?: "Authentication failed")
            }
        } catch (e: Exception) {
            Log.e(tag, "Discoverable authentication error", e)
            _error.value = e.message
            Result.failure(e)
        }
    }
    
    // Get user passkeys
    suspend fun getUserPasskeys(): Result<List<Passkey>> {
        return try {
            val response = api.getUserPasskeys()
            Result.success(response.passkeys)
        } catch (e: Exception) {
            Log.e(tag, "Failed to get passkeys", e)
            Result.failure(e)
        }
    }
    
    // Delete passkey
    suspend fun deletePasskey(id: String): Result<Unit> {
        return try {
            val response = api.deletePasskey(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                throw Exception(response.error ?: "Failed to delete passkey")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete passkey", e)
            Result.failure(e)
        }
    }
    
    // Logout
    suspend fun logout(): Result<Unit> {
        return try {
            api.logout()
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Logout error", e)
            Result.failure(e)
        }
    }
    
    // Helper functions
    private fun createRegistrationRequest(options: PublicKeyCredentialCreationOptions): CreatePublicKeyCredentialRequest {
        val requestJson = JSONObject().apply {
            put("challenge", options.challenge)
            put("rp", JSONObject().apply {
                put("id", options.rp.id)
                put("name", options.rp.name)
            })
            put("user", JSONObject().apply {
                put("id", options.user.id)
                put("name", options.user.name)
                put("displayName", options.user.displayName)
            })
            put("pubKeyCredParams", org.json.JSONArray(
                options.pubKeyCredParams.map { param ->
                    JSONObject().apply {
                        put("type", param.type)
                        put("alg", param.alg)
                    }
                }
            ))
            options.authenticatorSelection?.let { selection ->
                put("authenticatorSelection", JSONObject().apply {
                    selection.authenticatorAttachment?.let { put("authenticatorAttachment", it) }
                    selection.residentKey?.let { put("residentKey", it) }
                    selection.userVerification?.let { put("userVerification", it) }
                })
            }
            options.timeout?.let { put("timeout", it) }
            options.attestation?.let { put("attestation", it) }
        }
        
        Log.d(tag, "Creating credential request with JSON: ${requestJson.toString(2)}")
        return CreatePublicKeyCredentialRequest(requestJson.toString())
    }
    
    private fun createAuthenticationRequest(options: PublicKeyCredentialRequestOptions): GetCredentialRequest {
        val requestJson = JSONObject().apply {
            put("challenge", options.challenge)
            put("rpId", options.rpId)
            options.timeout?.let { put("timeout", it) }
            options.userVerification?.let { put("userVerification", it) }
            options.allowCredentials?.let { credentials ->
                put("allowCredentials", credentials.map { cred ->
                    JSONObject().apply {
                        put("type", cred.type)
                        put("id", cred.id)
                        cred.transports?.let { put("transports", it) }
                    }
                })
            }
        }
        
        return GetCredentialRequest.Builder()
            .addCredentialOption(
                GetPublicKeyCredentialOption(requestJson.toString())
            )
            .build()
    }
    
    private fun parseRegistrationResponse(credential: CreatePublicKeyCredentialResponse): Map<String, Any> {
        return try {
            val json = JSONObject(credential.registrationResponseJson)
            json.keys().asSequence().associateWith { key ->
                json.get(key)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse registration response", e)
            emptyMap()
        }
    }
    
    private fun parseAuthenticationResponse(credential: PublicKeyCredential): Map<String, Any> {
        return try {
            val json = JSONObject(credential.authenticationResponseJson)
            json.keys().asSequence().associateWith { key ->
                json.get(key)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse authentication response", e)
            emptyMap()
        }
    }
    
}