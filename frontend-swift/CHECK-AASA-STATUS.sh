#!/bin/bash

echo "🔍 Checking AASA Status for PasskeyDemo"
echo "========================================"

# Get ngrok URL from config
NGROK_URL=$(grep -o '"ngrok_url": "[^"]*"' PasskeyDemo/ngrok-config.json | cut -d'"' -f4)
echo "📡 Ngrok URL: $NGROK_URL"

# Check if AASA is accessible
echo ""
echo "🌐 Testing AASA accessibility..."
RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" "$NGROK_URL/.well-known/apple-app-site-association")
HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS:" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | grep -v "HTTP_STATUS:")

echo "HTTP Status: $HTTP_STATUS"
echo "Response:"
echo "$BODY" | jq . 2>/dev/null || echo "$BODY"

# Check if app is built with entitlements
echo ""
echo "📱 Checking built app entitlements..."
APP_PATH=$(find ~/Library/Developer/Xcode/DerivedData -name "PasskeyDemo.app" -type d 2>/dev/null | head -1)

if [ -z "$APP_PATH" ]; then
    echo "❌ No built app found in DerivedData"
    echo "   Build the app first in Xcode"
else
    echo "Found app at: $APP_PATH"
    echo ""
    echo "Entitlements in built app:"
    codesign -d --entitlements - "$APP_PATH" 2>/dev/null | grep -A 10 "associated-domains" || echo "❌ No associated domains found in built app"
fi

# Developer mode test URL
echo ""
echo "🔗 To force AASA fetch on iOS 16+:"
echo "   1. On device: Settings → Developer → Universal Links → Diagnostics"
echo "   2. Enter: $NGROK_URL"
echo "   3. Tap 'Test' to see fetch result"

echo ""
echo "💡 If AASA isn't being fetched:"
echo "   - Ensure you visited $NGROK_URL in Safari first"
echo "   - Try on a real device instead of simulator"
echo "   - Check Console.app for 'swcd' errors with your domain"