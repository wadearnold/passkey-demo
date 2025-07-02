# iOS Swift Frontend Setup Guide

## Prerequisites

1. **Apple Developer Account**: Required for code signing
2. **Xcode 15+**: For iOS 17+ SDK support
3. **Physical device or iOS Simulator**: iOS 16+ for WebAuthn support

## Initial Setup

### 1. Configure Code Signing (REQUIRED)

The app must be properly signed to use Associated Domains for passkey authentication:

1. Open `PasskeyDemo.xcodeproj` in Xcode
2. Select the "PasskeyDemo" target
3. Go to "Signing & Capabilities" tab
4. Enable "Automatically manage signing"
5. Select your Development Team from the dropdown
   - This sets your Team ID (e.g., `927BS6LD3W`)
   - Without this, you'll get "FAKETEAMID" errors

### 2. Update Associated Domains

After setting your team, update the domains for your deployment mode:

#### For Local Development (localhost)
The default entitlements work as-is for `localhost` development.

#### For Cross-Platform Testing (ngrok)
1. Get your ngrok domain from `.env` file or ngrok dashboard
2. Edit `PasskeyDemo/PasskeyDemo.entitlements`:
   ```xml
   <key>com.apple.developer.associated-domains</key>
   <array>
       <string>webcredentials:YOUR-NGROK-DOMAIN.ngrok-free.app</string>
       <string>webcredentials:localhost?mode=developer</string>
   </array>
   ```

### 3. Configure ngrok (for cross-platform mode)

1. Copy the template:
   ```bash
   cp ngrok-config.template.json ngrok-config.json
   ```

2. Edit `ngrok-config.json` with your ngrok URL:
   ```json
   {
     "baseURL": "https://YOUR-NGROK-DOMAIN.ngrok-free.app"
   }
   ```

### 4. Update Backend AASA File

The backend must serve an Apple App Site Association file:

1. Edit `backend/static/.well-known/apple-app-site-association`
2. Replace `TEAMID` with your actual Team ID from Xcode:
   ```json
   {
     "webcredentials": {
       "apps": ["YOUR-TEAM-ID.com.passkey.demo.ios"]
     }
   }
   ```

## Building and Running

### Clean Build (Recommended after configuration changes)
1. In Xcode: Product → Clean Build Folder (Cmd+Shift+K)
2. Delete app from device/simulator
3. Build and Run (Cmd+R)

### Verify Setup
1. Check AASA is accessible:
   ```bash
   curl https://YOUR-NGROK-DOMAIN.ngrok-free.app/.well-known/apple-app-site-association
   ```

2. Console should show your Team ID, not "FAKETEAMID"

## Troubleshooting

### "Application not associated with domain" Error
- Verify Team ID is set in Xcode (not empty)
- Check entitlements file has correct domain
- Ensure AASA file has your Team ID
- Clean build and reinstall app

### "FAKETEAMID" in Error Messages
- You haven't set a Development Team in Xcode
- Go to Signing & Capabilities and select a team

### Passkeys Not Syncing Across Devices
- Ensure iCloud Keychain is enabled on all devices
- Use the same Apple ID on all devices
- Check that devices are on iOS 16+

## Development Modes

### Local Mode (localhost)
- Quick development without ngrok
- Passkeys only work on single device
- No cross-platform sharing

### Cross-Platform Mode (ngrok)
- Requires ngrok setup and domain configuration
- Enables passkey sharing between web/iOS/Android
- Production-like testing environment

## Security Notes

- The `?mode=developer` suffix in entitlements allows localhost testing
- Production apps should not include developer mode
- Always use HTTPS in production (ngrok provides this automatically)