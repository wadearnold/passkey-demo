#!/bin/bash

# Script to configure ngrok URL for Android app

echo "Android ngrok Configuration"
echo "=========================="
echo ""

# First, try to read from .env file in parent directory
ENV_FILE="../.env"
if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
    if [ ! -z "$NGROK_URL" ]; then
        echo "Found NGROK_URL in .env file: $NGROK_URL"
        NGROK_URL_TO_USE=$NGROK_URL
    fi
fi

# If URL provided as argument, use it instead
if [ ! -z "$1" ]; then
    NGROK_URL_TO_USE=$1
    echo "Using provided URL: $NGROK_URL_TO_USE"
fi

# Check if we have a URL to use
if [ -z "$NGROK_URL_TO_USE" ]; then
    echo "Usage: ./setup-ngrok.sh [ngrok-url]"
    echo ""
    echo "You can either:"
    echo "1. Provide the URL as an argument: ./setup-ngrok.sh https://abc123.ngrok-free.app"
    echo "2. Or let the script read from ../.env file (NGROK_URL variable)"
    echo ""
    echo "To get your ngrok URL:"
    echo "1. Run ./scripts/start-ngrok.sh from the project root"
    echo "2. The URL will be saved to .env file automatically"
    exit 1
fi

NGROK_URL=$NGROK_URL_TO_USE

# Validate URL format (accept both .ngrok.io and .ngrok-free.app)
if [[ ! "$NGROK_URL" =~ ^https://.*\.ngrok(-free)?\.app$ ]] && [[ ! "$NGROK_URL" =~ ^https://.*\.ngrok\.io$ ]]; then
    echo "Error: Invalid ngrok URL format"
    echo "Expected format: https://xxxxx.ngrok-free.app or https://xxxxx.ngrok.io"
    exit 1
fi

echo "Configuring Android app to use: $NGROK_URL"

# Create ngrok-config.json file
cat > ngrok-config.json << EOF
{
    "ngrok_url": "$NGROK_URL"
}
EOF

echo ""
echo "✅ Configuration saved to ngrok-config.json"
echo ""
echo "⚠️  IMPORTANT: The config file approach doesn't work well with Android's sandboxing."
echo ""
echo "RECOMMENDED: Use the in-app Settings instead:"
echo "1. Open the app"
echo "2. Tap the Settings icon (gear) on the home screen"
echo "3. Enter: $NGROK_URL"
echo "4. Tap the check mark to save"
echo "5. Restart the app"
echo ""
echo "The app will now use your ngrok URL for all API calls."