# Finding Code Signing Entitlements in Xcode

## Method 1: Build Settings Tab

1. **Select PasskeyDemo target** (not the project)
2. **Click "Build Settings" tab** (next to "Signing & Capabilities")
3. **Search for "entitlements"** in the search bar
4. Look for **"Code Signing Entitlements"** setting
5. Should show: `PasskeyDemo/PasskeyDemo.entitlements`

## Method 2: All vs Combined View

If you don't see it:
1. In Build Settings, click **"All"** (not "Basic" or "Customized")
2. Make sure **"Combined"** is selected (not "Levels")
3. Search for "CODE_SIGN_ENTITLEMENTS"

## Method 3: Command Line Verification

To verify entitlements are linked, run from the project directory:

```bash
# Check if entitlements are in the build settings
grep -r "CODE_SIGN_ENTITLEMENTS" PasskeyDemo.xcodeproj/

# Should output something like:
# CODE_SIGN_ENTITLEMENTS = PasskeyDemo/PasskeyDemo.entitlements;
```

## Method 4: Check Built App

After building, verify the entitlements are embedded:

```bash
# Find the built app (usually in DerivedData)
find ~/Library/Developer/Xcode/DerivedData -name "PasskeyDemo.app" -type d | head -1

# Check entitlements in built app
codesign -d --entitlements - "$(find ~/Library/Developer/Xcode/DerivedData -name "PasskeyDemo.app" -type d | head -1)"
```

Should show XML output with your associated domains.

## What You Should See

The entitlements should contain:
```xml
<key>com.apple.developer.associated-domains</key>
<array>
    <string>webcredentials:67e9-76-154-22-254.ngrok-free.app</string>
    <string>webcredentials:localhost?mode=developer</string>
    <string>applinks:67e9-76-154-22-254.ngrok-free.app</string>
</array>
```

## If Missing

If CODE_SIGN_ENTITLEMENTS is not set:
1. In Build Settings, find "Code Signing Entitlements"
2. Double-click the empty field
3. Type: `PasskeyDemo/PasskeyDemo.entitlements`