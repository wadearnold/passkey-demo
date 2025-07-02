#!/bin/bash

echo "🔍 Verifying Entitlements in Built App"
echo "======================================"

# Find the most recent built app
APP_PATH=$(find ~/Library/Developer/Xcode/DerivedData -name "PasskeyDemo.app" -type d 2>/dev/null | head -1)

if [ -z "$APP_PATH" ]; then
    echo "❌ No built app found in DerivedData"
    echo "   Build the app first in Xcode"
    exit 1
fi

echo "📱 Found app at:"
echo "   $APP_PATH"
echo ""

# Check if entitlements are embedded
echo "🔐 Checking embedded entitlements:"
codesign -d --entitlements - "$APP_PATH" 2>/dev/null > /tmp/entitlements.plist

if [ $? -ne 0 ]; then
    echo "❌ Failed to extract entitlements"
    exit 1
fi

# Pretty print the entitlements
echo "📄 Entitlements content:"
plutil -p /tmp/entitlements.plist

# Check specifically for associated domains
echo ""
echo "🌐 Associated domains found:"
grep -A 10 "associated-domains" /tmp/entitlements.plist | grep "string" | sed 's/.*<string>//;s/<\/string>//' | while read domain; do
    echo "   ✓ $domain"
done

# Check bundle identifier
echo ""
echo "📦 Bundle identifier:"
APP_BUNDLE_ID=$(plutil -extract CFBundleIdentifier raw "$APP_PATH/Info.plist" 2>/dev/null)
echo "   $APP_BUNDLE_ID"

# Get team ID from provisioning
echo ""
echo "👥 Team ID from code signature:"
codesign -dvvv "$APP_PATH" 2>&1 | grep "TeamIdentifier" | sed 's/TeamIdentifier=/   /'

# Expected AASA app identifier
TEAM_ID=$(codesign -dvvv "$APP_PATH" 2>&1 | grep "TeamIdentifier" | sed 's/TeamIdentifier=//')
echo ""
echo "🔗 Expected AASA app identifier:"
echo "   ${TEAM_ID}.${APP_BUNDLE_ID}"

# Cleanup
rm -f /tmp/entitlements.plist

echo ""
echo "💡 Next steps:"
echo "1. Verify the associated domains include your ngrok URL"
echo "2. Ensure AASA file has the correct Team ID + Bundle ID"
echo "3. If missing, check Xcode project settings"