package com.passkeydemo.android.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Registration Models
@Serializable
data class RegistrationRequest(
    val username: String,
    val displayName: String
)

@Serializable
data class RegistrationOptions(
    val publicKey: PublicKeyCredentialCreationOptions
)

@Serializable
data class PublicKeyCredentialCreationOptions(
    val challenge: String,
    val rp: RelyingParty,
    val user: User,
    val pubKeyCredParams: List<PubKeyCredParam>,
    val timeout: Long? = null,
    val attestation: String? = null,
    val excludeCredentials: List<PublicKeyCredentialDescriptor>? = null,
    val authenticatorSelection: AuthenticatorSelectionCriteria? = null
)

@Serializable
data class RelyingParty(
    val id: String,
    val name: String
)

@Serializable
data class User(
    val id: String,
    val name: String,
    val displayName: String
)

@Serializable
data class PubKeyCredParam(
    val type: String,
    val alg: Int
)

@Serializable
data class PublicKeyCredentialDescriptor(
    val type: String,
    val id: String,
    val transports: List<String>? = null
)

@Serializable
data class AuthenticatorSelectionCriteria(
    val authenticatorAttachment: String? = null,
    val residentKey: String? = null,
    val requireResidentKey: Boolean? = null,
    val userVerification: String? = null
)

// Authentication Models
@Serializable
data class AuthenticationRequest(
    val username: String? = null
)

@Serializable
data class AuthenticationOptions(
    val publicKey: PublicKeyCredentialRequestOptions
)

@Serializable
data class PublicKeyCredentialRequestOptions(
    val challenge: String,
    val timeout: Long? = null,
    val rpId: String,
    val userVerification: String? = null,
    val allowCredentials: List<PublicKeyCredentialDescriptor>? = null
)

// Response Models
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class UserProfile(
    val username: String,
    val displayName: String,
    val createdAt: String
)

@Serializable
data class Passkey(
    val id: String,
    val credentialId: String,
    val publicKey: String,
    val signCount: Int,
    val createdAt: String,
    val lastUsedAt: String? = null,
    val credentialDeviceType: String,
    val credentialBackedUp: Boolean,
    val transports: List<String>? = null
)

@Serializable
data class PasskeysResponse(
    val passkeys: List<Passkey>
)