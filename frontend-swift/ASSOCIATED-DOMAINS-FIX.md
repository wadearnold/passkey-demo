# Fix Associated Domains for Passkey Authentication

## The Problem

The error message indicates that `FAKETEAMID.com.passkey.demo.ios` is not associated with your ngrok domain. This happens because:

1. The app bundle identifier is `com.passkey.demo.ios`
2. No Development Team is set in Xcode
3. The entitlements file doesn't include your ngrok domain

## Solution Steps

### 1. Set Your Development Team in Xcode

1. Open `PasskeyDemo.xcodeproj` in Xcode
2. Select the PasskeyDemo target
3. Go to "Signing & Capabilities" tab
4. Enable "Automatically manage signing"
5. Select your Development Team from the dropdown
6. This will set your real Team ID (replacing FAKETEAMID)

### 2. Update the Entitlements File

Edit `PasskeyDemo/PasskeyDemo.entitlements` to include your ngrok domain:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.developer.associated-domains</key>
    <array>
        <!-- For ngrok cross-platform mode -->
        <string>webcredentials:67e9-76-154-22-254.ngrok-free.app</string>
        
        <!-- Keep localhost for local development -->
        <string>webcredentials:localhost?mode=developer</string>
    </array>
    <key>com.apple.developer.authentication-services.autofill-credential-provider</key>
    <true/>
</dict>
</plist>
```

### 3. Create Apple App Site Association File

The backend needs to serve an Apple App Site Association (AASA) file. Create this file at `backend/static/.well-known/apple-app-site-association`:

```json
{
  "webcredentials": {
    "apps": ["TEAMID.com.passkey.demo.ios"]
  }
}
```

Replace `TEAMID` with your actual Apple Developer Team ID (you can find this in Xcode after setting your team).

### 4. Ensure Backend Serves AASA File

The Go backend needs to serve this file. Add to your backend main.go:

```go
// Serve Apple App Site Association for iOS
http.HandleFunc("/.well-known/apple-app-site-association", func(w http.ResponseWriter, r *http.Request) {
    w.Header().Set("Content-Type", "application/json")
    http.ServeFile(w, r, "static/.well-known/apple-app-site-association")
})
```

### 5. Verify Setup

After making these changes:

1. Clean build folder in Xcode (Cmd+Shift+K)
2. Delete app from device/simulator
3. Rebuild and run
4. Verify AASA is accessible: `https://67e9-76-154-22-254.ngrok-free.app/.well-known/apple-app-site-association`

## Important Notes

- The TEAMID in AASA must match your Xcode signing team
- The domain in entitlements must match your ngrok domain exactly
- No `https://` prefix in the entitlements domain
- The bundle ID must remain consistent

## Quick Test

Once configured, iOS will verify the association by:
1. Checking the entitlements for allowed domains
2. Fetching the AASA file from your server
3. Verifying the Team ID + Bundle ID match

This enables passkey authentication to work with your ngrok domain.