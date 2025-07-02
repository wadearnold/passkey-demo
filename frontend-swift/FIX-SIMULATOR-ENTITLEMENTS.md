# Fix: Entitlements Not Working in iOS Simulator

## The Problem
When building for iOS Simulator, Xcode doesn't always embed entitlements properly, especially for associated domains. This prevents AASA fetching.

## Solution 1: Build for Real Device (Recommended)

The most reliable way to test associated domains:
1. Connect a physical iOS device
2. Select your device as the build target (not simulator)
3. Build and run
4. Entitlements will be properly embedded with code signing

## Solution 2: Force Code Signing for Simulator

In Xcode:
1. Select PasskeyDemo target → Build Settings
2. Search for "CODE_SIGN_IDENTITY"
3. Expand it and set for "Any iOS Simulator SDK" to: "Apple Development"
4. Search for "CODE_SIGNING_REQUIRED"
5. Set to "YES" for all configurations

## Solution 3: Manual Entitlements for Debug

1. Select PasskeyDemo target → Build Settings
2. Search for "CODE_SIGN_ENTITLEMENTS"
3. Verify it shows: `PasskeyDemo/PasskeyDemo.entitlements`
4. Make sure this is set for both Debug and Release

## Alternative Testing Method

If simulator issues persist, test the flow manually:

1. **Test on localhost first**:
   - Edit `ngrok-config.json` to use empty string
   - This forces localhost mode
   - Associated domains work with developer mode

2. **Use Console logging**:
   - Add logs to verify RPID matches
   - Confirm the authentication flow works
   - Then deploy to real device for ngrok testing

## Verification Steps

After making changes:
1. Clean Build Folder (Cmd+Shift+K)
2. Delete app from device/simulator
3. Build and run
4. Check Console.app for AASA fetch attempts
5. Look for lines like:
   ```
   swcd: Fetching AASA for 67e9-76-154-22-254.ngrok-free.app
   ```

## Why This Happens

- iOS Simulator doesn't enforce code signing by default
- Associated domains require proper app signing
- The `?mode=developer` suffix only works for localhost
- Production domains (including ngrok) need full code signing