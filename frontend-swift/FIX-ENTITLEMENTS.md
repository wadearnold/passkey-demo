# Fix: Entitlements File Not Linked to Xcode Project

## The Problem
The `PasskeyDemo.entitlements` file exists but isn't linked to the Xcode project. This means iOS doesn't know about the associated domains and won't fetch the AASA file.

## Solution: Link Entitlements in Xcode

1. **Open PasskeyDemo.xcodeproj in Xcode**

2. **Add Entitlements to Project**:
   - Select the PasskeyDemo target
   - Go to "Signing & Capabilities" tab
   - Click the "+" button to add capability
   - Choose "Associated Domains"
   - This will create or link the entitlements file

3. **Configure Associated Domains**:
   - You should now see "Associated Domains" section
   - Add domains:
     - `webcredentials:67e9-76-154-22-254.ngrok-free.app`
     - `webcredentials:localhost?mode=developer`
     - `applinks:67e9-76-154-22-254.ngrok-free.app`

4. **Verify Entitlements Path**:
   - Still in "Signing & Capabilities"
   - Scroll down to find "Code Signing Entitlements"
   - Should show: `PasskeyDemo/PasskeyDemo.entitlements`

## Alternative: Manual Project File Edit

If the GUI method doesn't work, manually add to `project.pbxproj`:

```
CODE_SIGN_ENTITLEMENTS = PasskeyDemo/PasskeyDemo.entitlements;
```

## Expected Result

After linking entitlements:
- Clean build and reinstall app
- iOS will fetch AASA during app installation
- Backend logs should show: `🍎 AASA file requested from: ...`
- Console.app should show swcd fetching your domain

## Test Command

To verify entitlements are active:
```bash
codesign -d --entitlements - PasskeyDemo.app
```

Should show your associated domains in the output.