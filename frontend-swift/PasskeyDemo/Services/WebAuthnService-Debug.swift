import Foundation
import AuthenticationServices
import SwiftUI
import LocalAuthentication

// DEBUG VERSION - Enhanced with detailed logging
// Copy this over WebAuthnService.swift when debugging

@MainActor
class WebAuthnService: NSObject, ObservableObject {
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var currentUser: String?
    @Published var userPasskeys: [UserPasskey] = []
    
    private let apiService = APIService.shared
    
    // MARK: - Registration
    
    func register(username: String, displayName: String) async throws -> RegistrationResult {
        isLoading = true
        errorMessage = nil
        
        defer { isLoading = false }
        
        do {
            // Step 1: Get registration options from server
            print("🚀 Starting registration for username: \(username)")
            let options = try await apiService.beginRegistration(
                username: username, 
                displayName: displayName
            )
            
            print("=== REGISTRATION DEBUG ===")
            print("Username: \(username)")
            print("Display Name: \(displayName)")
            print("Server RPID: \(options.rp.id)")
            print("User ID: \(options.user.id)")
            print("User Name: \(options.user.name)")
            print("Challenge: \(options.challenge)")
            print("Attestation: \(options.attestation)")
            print("Pub Key Cred Params: \(options.pubKeyCredParams.map { "[\($0.alg), \($0.type)]" }.joined(separator: ", "))")
            
            // Step 2: Create platform authenticator request
            // Use the RPID from server response for proper WebAuthn compliance
            // IMPORTANT: Must match exact RPID from server (ngrok domain for cross-platform)
            let rpid = options.rp.id
            let platformProvider = ASAuthorizationPlatformPublicKeyCredentialProvider(
                relyingPartyIdentifier: rpid
            )
            
            guard let challengeData = options.challenge.base64URLDecode() else {
                print("❌ Failed to decode challenge: \(options.challenge)")
                throw WebAuthnError.invalidChallenge
            }
            
            guard let userIdData = options.user.id.base64URLDecode() else {
                print("❌ Failed to decode user ID: \(options.user.id)")
                throw WebAuthnError.invalidChallenge
            }
            
            print("Challenge Data Length: \(challengeData.count)")
            print("User ID Data Length: \(userIdData.count)")
            
            let registrationRequest = platformProvider.createCredentialRegistrationRequest(
                challenge: challengeData,
                name: options.user.name,
                userID: userIdData
            )
            
            // Configure authenticator selection
            registrationRequest.displayName = options.user.displayName
            registrationRequest.userVerificationPreference = .preferred
            
            print("🔐 Created platform authenticator request")
            print("Request Name: \(options.user.name)")
            print("Request Display Name: \(options.user.displayName)")
            
            // Step 3: Prompt user for biometric authentication
            let authController = ASAuthorizationController(authorizationRequests: [registrationRequest])
            authController.delegate = self
            authController.presentationContextProvider = self
            
            let credential = try await withCheckedThrowingContinuation { continuation in
                self.registrationContinuation = continuation
                authController.performRequests()
            }
            
            print("✅ User completed biometric authentication")
            print("Created Credential ID: \(credential.id)")
            print("Attestation Object Length: \((credential.response as? RegistrationCredential.AuthenticatorAttestationResponse)?.attestationObject.count ?? 0)")
            
            // Step 4: Send credential to server
            let result = try await apiService.finishRegistration(credential: credential)
            
            if result.success {
                currentUser = result.username
                print("🎉 Registration successful for user: \(result.username ?? "unknown")")
                await loadUserPasskeys()
            } else {
                print("❌ Registration failed with success=false")
            }
            
            return result
            
        } catch let error as WebAuthnError {
            errorMessage = error.localizedDescription
            print("❌ Registration failed with WebAuthnError: \(error)")
            throw error
        } catch {
            let webAuthnError = WebAuthnError.unknown(error.localizedDescription)
            errorMessage = webAuthnError.localizedDescription
            print("❌ Registration failed with error: \(error)")
            print("Error Type: \(type(of: error))")
            throw webAuthnError
        }
    }
    
    // MARK: - Authentication
    
    func authenticate(username: String? = nil) async throws -> AuthenticationResult {
        isLoading = true
        errorMessage = nil
        
        defer { isLoading = false }
        
        do {
            // Step 1: Get authentication options from server
            print("🚀 Starting authentication for username: \(username ?? "discoverable")")
            let options = try await apiService.beginAuthentication(username: username)
            
            if username == nil {
                print("=== DISCOVERABLE LOGIN DEBUG ===")
            } else {
                print("=== USERNAME LOGIN DEBUG ===")
            }
            print("Username: \(username ?? "none")")
            print("Server RPID: \(options.rpId ?? "nil")")
            print("Challenge: \(options.challenge)")
            print("Timeout: \(options.timeout ?? 0)")
            print("User Verification: \(options.userVerification ?? "nil")")
            
            if let allowedCreds = options.allowCredentials {
                print("Allowed Credentials Count: \(allowedCreds.count)")
                for (index, cred) in allowedCreds.enumerated() {
                    print("  Credential \(index + 1):")
                    print("    ID: \(cred.id)")
                    print("    Type: \(cred.type)")
                    print("    Transports: \(cred.transports?.joined(separator: ", ") ?? "none")")
                }
            } else {
                print("Allow Credentials: nil (discoverable mode)")
            }
            
            // Step 2: Create platform authenticator request
            // Use the RPID from server response for proper WebAuthn compliance
            // IMPORTANT: Must match the exact RPID from server (ngrok domain for cross-platform)
            guard let rpid = options.rpId else {
                print("❌ Server did not provide RPID")
                throw WebAuthnError.apiError("Server did not provide RPID")
            }
            let platformProvider = ASAuthorizationPlatformPublicKeyCredentialProvider(
                relyingPartyIdentifier: rpid
            )
            
            guard let challengeData = options.challenge.base64URLDecode() else {
                print("❌ Failed to decode challenge: \(options.challenge)")
                throw WebAuthnError.invalidChallenge
            }
            
            print("Challenge Data Length: \(challengeData.count)")
            
            let assertionRequest = platformProvider.createCredentialAssertionRequest(challenge: challengeData)
            
            // Configure user verification
            assertionRequest.userVerificationPreference = ASAuthorizationPublicKeyCredentialUserVerificationPreference.preferred
            
            // Set allowed credentials if provided (username-based auth)
            if let allowedCredentials = options.allowCredentials {
                assertionRequest.allowedCredentials = allowedCredentials.compactMap { cred -> ASAuthorizationPlatformPublicKeyCredentialDescriptor? in
                    guard let credentialIdData = cred.id.base64URLDecode() else {
                        print("⚠️ Failed to decode credential ID: \(cred.id)")
                        return nil
                    }
                    print("Adding allowed credential with ID length: \(credentialIdData.count)")
                    return ASAuthorizationPlatformPublicKeyCredentialDescriptor(credentialID: credentialIdData)
                }
                print("🔑 Username-based auth with \(assertionRequest.allowedCredentials.count) allowed credentials")
            } else {
                print("🔍 Discoverable credential authentication (no allowed credentials)")
            }
            
            // Step 3: Prompt user for biometric authentication
            let authController = ASAuthorizationController(authorizationRequests: [assertionRequest])
            authController.delegate = self
            authController.presentationContextProvider = self
            
            print("📱 Presenting authentication UI...")
            
            let credential = try await withCheckedThrowingContinuation { continuation in
                self.authenticationContinuation = continuation
                authController.performRequests()
            }
            
            print("✅ User completed biometric authentication")
            print("Used Credential ID: \(credential.id)")
            print("User Handle: \(credential.response.userHandle ?? "nil")")
            
            // Step 4: Send credential to server
            let result = try await apiService.finishAuthentication(credential: credential)
            
            if result.success {
                currentUser = result.username
                print("🎉 Authentication successful for user: \(result.username ?? "unknown")")
                await loadUserPasskeys()
            } else {
                print("❌ Authentication failed with success=false")
            }
            
            return result
            
        } catch let error as WebAuthnError {
            errorMessage = error.localizedDescription
            print("❌ Authentication failed with WebAuthnError: \(error)")
            throw error
        } catch {
            let webAuthnError = WebAuthnError.unknown(error.localizedDescription)
            errorMessage = webAuthnError.localizedDescription
            print("❌ Authentication failed with error: \(error)")
            print("Error Type: \(type(of: error))")
            throw webAuthnError
        }
    }
    
    // MARK: - User Management
    
    func loadUserPasskeys() async {
        do {
            userPasskeys = try await apiService.getUserPasskeys()
            print("📊 Loaded \(userPasskeys.count) passkeys for user")
            for (index, passkey) in userPasskeys.enumerated() {
                print("  Passkey \(index + 1):")
                print("    ID: \(passkey.id)")
                print("    Created: \(passkey.createdAt)")
                print("    Last Used: \(passkey.lastUsedAt ?? "never")")
                print("    Backed Up: \(passkey.isBackedUp ? "yes" : "no")")
            }
        } catch {
            print("❌ Failed to load user passkeys: \(error)")
            print("Error Type: \(type(of: error))")
            errorMessage = "Failed to load passkeys: \(error.localizedDescription)"
        }
    }
    
    func deletePasskey(_ passkey: UserPasskey) async throws {
        do {
            print("🗑️ Attempting to delete passkey: \(passkey.id)")
            try await apiService.deletePasskey(credentialId: passkey.id)
            await loadUserPasskeys() // Refresh the list
            print("✅ Successfully deleted passkey")
        } catch {
            print("❌ Failed to delete passkey: \(error)")
            print("Error Type: \(type(of: error))")
            throw error
        }
    }
    
    func logout() async {
        do {
            print("👋 Logging out user: \(currentUser ?? "unknown")")
            try await apiService.logout()
            currentUser = nil
            userPasskeys = []
            print("✅ User logged out successfully")
        } catch {
            print("❌ Logout failed: \(error)")
            print("Error Type: \(type(of: error))")
            // Even if logout fails on server, clear local state
            currentUser = nil
            userPasskeys = []
        }
    }
    
    // MARK: - Capability Check
    
    var isWebAuthnSupported: Bool {
        // WebAuthn with platform authenticators requires iOS 16.0+
        guard #available(iOS 16.0, *) else {
            print("⚠️ WebAuthn not supported: iOS 16.0+ required")
            return false
        }
        
        // Check if the device has biometric authentication or device passcode
        let context = LAContext()
        var error: NSError?
        
        // This checks if the device can use biometrics (Face ID/Touch ID) or device passcode
        let canEvaluatePolicy = context.canEvaluatePolicy(.deviceOwnerAuthentication, error: &error)
        
        if canEvaluatePolicy {
            print("✅ WebAuthn supported: biometric/passcode available")
        } else {
            print("⚠️ WebAuthn not supported: \(error?.localizedDescription ?? "no biometric/passcode")")
        }
        
        return canEvaluatePolicy
    }
    
    // MARK: - Private Properties for Async/Await Bridge
    
    private var registrationContinuation: CheckedContinuation<RegistrationCredential, Error>?
    private var authenticationContinuation: CheckedContinuation<AuthenticationCredential, Error>?
}

// MARK: - ASAuthorizationControllerDelegate

extension WebAuthnService: ASAuthorizationControllerDelegate {
    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        print("=== AUTHORIZATION COMPLETED ===")
        print("Credential Type: \(type(of: authorization.credential))")
        
        if let platformCredential = authorization.credential as? ASAuthorizationPlatformPublicKeyCredentialRegistration {
            print("Registration credential received")
            print("Credential ID Length: \(platformCredential.credentialID.count)")
            print("Raw Attestation Object Length: \(platformCredential.rawAttestationObject?.count ?? 0)")
            print("Raw Client Data JSON Length: \(platformCredential.rawClientDataJSON.count)")
            // Handle registration
            handleRegistrationCredential(platformCredential)
        } else if let platformCredential = authorization.credential as? ASAuthorizationPlatformPublicKeyCredentialAssertion {
            print("Authentication credential received")
            print("Credential ID Length: \(platformCredential.credentialID.count)")
            print("User ID Length: \(platformCredential.userID?.count ?? 0)")
            print("Raw Authenticator Data Length: \(platformCredential.rawAuthenticatorData.count)")
            print("Signature Length: \(platformCredential.signature.count)")
            // Handle authentication
            handleAuthenticationCredential(platformCredential)
        } else {
            print("⚠️ Unknown credential type: \(type(of: authorization.credential))")
        }
    }
    
    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        print("=== AUTHORIZATION ERROR ===")
        print("Error: \(error)")
        print("Error Type: \(type(of: error))")
        print("Error Description: \(error.localizedDescription)")
        
        let webAuthnError: WebAuthnError
        
        if let authError = error as? ASAuthorizationError {
            print("ASAuthorizationError Code: \(authError.code.rawValue)")
            switch authError.code {
            case .canceled:
                print("User canceled authentication")
                webAuthnError = .userCancel
            case .unknown:
                print("Unknown authorization error")
                webAuthnError = .unknown(authError.localizedDescription)
            case .invalidResponse:
                print("Invalid response from authenticator")
                webAuthnError = .apiError("Invalid response from authenticator")
            case .notHandled:
                print("Request not handled")
                webAuthnError = .notSupported
            case .failed:
                print("Authentication failed")
                webAuthnError = .unknown("Authentication failed")
            case .notInteractive:
                print("Authentication requires user interaction")
                webAuthnError = .unknown("Authentication requires user interaction")
            @unknown default:
                print("Unknown error code: \(authError.code.rawValue)")
                webAuthnError = .unknown(authError.localizedDescription)
            }
        } else {
            webAuthnError = .unknown(error.localizedDescription)
        }
        
        registrationContinuation?.resume(throwing: webAuthnError)
        authenticationContinuation?.resume(throwing: webAuthnError)
        
        registrationContinuation = nil
        authenticationContinuation = nil
    }
    
    private func handleRegistrationCredential(_ credential: ASAuthorizationPlatformPublicKeyCredentialRegistration) {
        print("=== HANDLING REGISTRATION CREDENTIAL ===")
        let registrationCredential = RegistrationCredential(
            id: credential.credentialID.base64URLEncode(),
            rawId: credential.credentialID.base64URLEncode(),
            type: "public-key",
            response: RegistrationCredential.AuthenticatorAttestationResponse(
                attestationObject: credential.rawAttestationObject?.base64URLEncode() ?? "",
                clientDataJSON: credential.rawClientDataJSON.base64URLEncode(),
                transports: ["internal", "hybrid"] // iOS typically supports internal and hybrid
            )
        )
        
        print("Created registration credential with ID: \(registrationCredential.id)")
        
        registrationContinuation?.resume(returning: registrationCredential)
        registrationContinuation = nil
    }
    
    private func handleAuthenticationCredential(_ credential: ASAuthorizationPlatformPublicKeyCredentialAssertion) {
        print("=== HANDLING AUTHENTICATION CREDENTIAL ===")
        let authenticationCredential = AuthenticationCredential(
            id: credential.credentialID.base64URLEncode(),
            rawId: credential.credentialID.base64URLEncode(),
            type: "public-key",
            response: AuthenticationCredential.AuthenticatorAssertionResponse(
                authenticatorData: credential.rawAuthenticatorData.base64URLEncode(),
                clientDataJSON: credential.rawClientDataJSON.base64URLEncode(),
                signature: credential.signature.base64URLEncode(),
                userHandle: credential.userID?.base64URLEncode()
            )
        )
        
        print("Created authentication credential with ID: \(authenticationCredential.id)")
        print("User Handle: \(authenticationCredential.response.userHandle ?? "nil")")
        
        authenticationContinuation?.resume(returning: authenticationCredential)
        authenticationContinuation = nil
    }
}

// MARK: - ASAuthorizationControllerPresentationContextProviding

extension WebAuthnService: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        print("=== GETTING PRESENTATION ANCHOR ===")
        // Return the key window for presenting the authentication UI
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first else {
            print("⚠️ No window found, returning empty anchor")
            return ASPresentationAnchor()
        }
        print("✅ Found window for presentation")
        return window
    }
}