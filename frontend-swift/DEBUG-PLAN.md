# Swift Frontend Debug Plan

## Prerequisites
1. Ensure ngrok is running: `./scripts/start-ngrok.sh`
2. Build React frontend: `cd frontend-react && npm run build`
3. Start backend with ngrok RPID: `cd backend && source ../.env && go run .`
4. Configure Swift app with ngrok domain in `ngrok-config.json`

## Debug Steps for Each Flow

### 1. Discoverable Login (Autofill)
**Goal**: Login without entering username, using system passkey picker

**Debug Points**:
```swift
// WebAuthnService.swift:104-145
func authenticate(username: String? = nil)
```

**Expected Flow**:
1. Call `/api/login/begin` without username
2. Server returns `mediation: "conditional"` and empty `allowCredentials`
3. iOS shows all passkeys for the RPID
4. User selects and authenticates with biometrics
5. Send assertion to `/api/login/finish`

**Common Issues**:
- RPID mismatch between server and client
- Missing associated domains entitlement
- Passkey not synced to device via iCloud

**Debug Output to Collect**:
```swift
// Add to WebAuthnService.swift:115
print("=== DISCOVERABLE LOGIN DEBUG ===")
print("Server RPID: \(loginBeginResponse.publicKey.rpId)")
print("Challenge: \(loginBeginResponse.publicKey.challenge)")
print("Allow Credentials: \(loginBeginResponse.publicKey.allowCredentials)")
print("User Verification: \(loginBeginResponse.publicKey.userVerification)")
```

### 2. Username/Password Login
**Goal**: Login with specific username, showing only that user's passkeys

**Debug Points**:
```swift
// WebAuthnService.swift:104-145
func authenticate(username: String? = nil)
```

**Expected Flow**:
1. Call `/api/login/begin` with username
2. Server returns specific `allowCredentials` for that user
3. iOS filters passkeys to show only allowed ones
4. User authenticates with biometrics
5. Send assertion to `/api/login/finish`

**Common Issues**:
- Username not found on server
- Credential ID mismatch
- User has no passkeys registered

**Debug Output to Collect**:
```swift
// Add to WebAuthnService.swift:115
print("=== USERNAME LOGIN DEBUG ===")
print("Username: \(username ?? "none")")
print("Server RPID: \(loginBeginResponse.publicKey.rpId)")
print("Allowed Credentials Count: \(loginBeginResponse.publicKey.allowCredentials.count)")
for cred in loginBeginResponse.publicKey.allowCredentials {
    print("  Credential ID: \(cred.id)")
    print("  Type: \(cred.type)")
}
```

### 3. Passkey Registration
**Goal**: Create new passkey with username/displayName

**Debug Points**:
```swift
// WebAuthnService.swift:23-102
func register(username: String, displayName: String)
```

**Expected Flow**:
1. Call `/api/register/begin` with username/displayName
2. Server returns challenge, user info, and RPID
3. Create platform credential with user verification
4. iOS prompts for biometric authentication
5. Send attestation to `/api/register/finish`
6. Reload passkey list to show new credential

**Common Issues**:
- User already exists
- Biometric authentication not available
- Attestation format issues

**Debug Output to Collect**:
```swift
// Add to WebAuthnService.swift:39
print("=== REGISTRATION DEBUG ===")
print("Username: \(username)")
print("Display Name: \(displayName)")
print("Server RPID: \(registerBeginResponse.publicKey.rp.id)")
print("User ID: \(registerBeginResponse.publicKey.user.id)")
print("Challenge: \(registerBeginResponse.publicKey.challenge)")
print("Attestation: \(registerBeginResponse.publicKey.attestation)")

// Add after credential creation (line 80)
print("Created Credential ID: \(credential.credentialID.base64URLEncodedString())")
print("Attestation Object Length: \(credential.rawAttestationObject?.count ?? 0)")
```

### 4. Profile Deep Linking
**Goal**: Handle profile information from deep links

**Implementation Needed**:
```swift
// Add to PasskeyDemoApp.swift
.onOpenURL { url in
    handleDeepLink(url)
}

func handleDeepLink(_ url: URL) {
    // Parse URL for profile data
    // Format: passkeyapp://profile?user=username&action=view
}
```

**Debug Output**:
```swift
print("=== DEEP LINK DEBUG ===")
print("URL: \(url.absoluteString)")
print("Scheme: \(url.scheme ?? "none")")
print("Host: \(url.host ?? "none")")
print("Query: \(url.query ?? "none")")
```

## Error Response Debugging

Add comprehensive error logging:

```swift
// Add to APIService.swift error handling
if let httpResponse = response as? HTTPURLResponse {
    print("HTTP Status: \(httpResponse.statusCode)")
    print("Headers: \(httpResponse.allHeaderFields)")
}

if let errorData = data,
   let errorString = String(data: errorData, encoding: .utf8) {
    print("Error Response: \(errorString)")
}
```

## Testing Checklist

### For Each Flow:
- [ ] Enable verbose logging
- [ ] Test with fresh user (no existing passkeys)
- [ ] Test with existing user (has passkeys)
- [ ] Test network failures
- [ ] Test with/without biometrics enabled
- [ ] Verify RPID matches exactly between client/server
- [ ] Check console logs in Xcode
- [ ] Monitor backend logs simultaneously

### Cross-Platform Verification:
- [ ] Create passkey on iOS, login on web
- [ ] Create passkey on web, login on iOS
- [ ] Verify iCloud Keychain sync is enabled
- [ ] Test with multiple Apple IDs

## Next Steps

1. Share the error logs from your discoverable login attempt
2. We'll analyze the specific failure points
3. Implement targeted fixes based on the errors
4. Test each flow systematically with debug output
5. Remove debug logging once working