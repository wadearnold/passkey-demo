#!/bin/bash

echo "🧹 Cleaning iOS AASA Cache for PasskeyDemo"

# 1. Kill the app if running
echo "📱 Stopping app if running..."
xcrun simctl terminate booted com.passkey.demo.ios 2>/dev/null || true

# 2. Uninstall the app
echo "🗑️  Uninstalling app from all simulators..."
xcrun simctl uninstall booted com.passkey.demo.ios 2>/dev/null || true

# 3. Clean Xcode
echo "🧽 Cleaning Xcode build..."
xcodebuild clean -project PasskeyDemo.xcodeproj -scheme PasskeyDemo -quiet

# 4. Remove derived data
echo "🗂️  Removing derived data..."
rm -rf ~/Library/Developer/Xcode/DerivedData/PasskeyDemo-*

# 5. Clear simulator caches
echo "💾 Clearing simulator caches..."
xcrun simctl shutdown all
rm -rf ~/Library/Developer/CoreSimulator/Caches/dyld/

# 6. Verify AASA is accessible
echo "🌐 Verifying AASA file..."
NGROK_URL=$(grep -o '"ngrok_url": "[^"]*"' PasskeyDemo/ngrok-config.json | cut -d'"' -f4)
echo "Checking: $NGROK_URL/.well-known/apple-app-site-association"
curl -s "$NGROK_URL/.well-known/apple-app-site-association" | jq .

echo ""
echo "✅ Cache cleared! Now:"
echo "1. Open $NGROK_URL in Safari on simulator/device first"
echo "2. Build and run fresh in Xcode"
echo "3. iOS will fetch AASA during app installation"