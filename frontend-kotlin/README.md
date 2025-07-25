# Passkey Demo - Android App

Native Android app demonstrating WebAuthn passkey authentication with cross-platform support via Google Password Manager.

## Configuration

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 28+ (Android 9.0)
- Android device or emulator with Google Play Services
- ngrok tunnel running (for cross-platform testing)

### Quick Setup

```bash
# 1. Start ngrok (from root directory)
../scripts/start-ngrok.sh

# 2. Build React app for ngrok
cd ../frontend-react
npm install && npm run build:ngrok

# 3. Configure Android app with ngrok URL
cd ../frontend-kotlin
./setup-ngrok.sh https://your-subdomain.ngrok.io

# 4. Setup and run backend
cd ../backend
./setup-aasa.sh YOUR_TEAM_ID  # Use any team ID for Android
source ../.env && ./passkey-backend

# 5. Open in Android Studio and run
```

### Configuration Options

**Option 1: In-App Settings (Easiest)**
- Tap Settings icon on home screen
- Enter your ngrok URL
- Restart the app

**Option 2: Setup Script**
```bash
./setup-ngrok.sh https://abc123.ngrok.io
```

**Option 3: Manual Config File**
Create `ngrok-config.json`:
```json
{
  "ngrok_url": "https://abc123.ngrok-free.app"
}
```

The app checks multiple locations for configuration:
1. SharedPreferences (from Settings screen)
2. ngrok-config.json file
3. Default to localhost/10.0.2.2 for emulator

## Running the App

### Development
1. Open this directory in Android Studio
2. Sync project with Gradle files
3. Select your device/emulator
4. Run the app

### Important Notes
- **Physical device recommended** - Better biometric support
- **Google Play Services required** - For Credential Manager API
- **Same network required** - Device and dev machine for local testing
- **API 28+ required** - Minimum for WebAuthn support
- **Java/JDK 17+ required** - For Gradle builds

## Features

- **Passkey Registration** - Create passkeys with biometric authentication
- **Username Login** - Sign in with username and passkey
- **Discoverable Login** - Passwordless login without username
- **Cross-Platform Sync** - Works with passkeys from iOS/web via Google Password Manager
- **Passkey Management** - View, rename, and delete passkeys

## Debugging

### Common Issues

**"No credentials available" error**
- Ensure Google Play Services is updated
- Check that screen lock is enabled
- Verify Google account is signed in

**Gradle/Java errors**
- Ensure Java/JDK 17+ is installed
- Copy `local.properties.template` to `local.properties` 
- Set `org.gradle.java.home` in `local.properties` if needed
- In Android Studio: File → Project Structure → SDK Location → JDK Location

**Network connection errors**
- For emulator: Use `10.0.2.2` instead of `localhost`
- For device: Ensure same network as dev machine
- Check ngrok URL is correct in `ngrok-config.json`

**Biometric prompt not showing**
- Enable screen lock in device settings
- Add fingerprint/face in security settings
- Check app has USE_BIOMETRIC permission

### Logging

Monitor Android Studio Logcat:
```
Filter: "PasskeyDemo"

Example logs:
D/PasskeyDemo: Starting registration for username: alice
D/PasskeyDemo: Create credential request prepared
D/PasskeyDemo: Credential created successfully
D/PasskeyDemo: Registration completed
```

### Testing Cross-Platform

**⚠️ IMPORTANT: Use a Physical Device**

Passkey support on Android emulators is unreliable. The Android Credential Manager often returns "No create options available" on simulators, even when properly configured. At this point, we are not sure how to get this working reliably with simulators.

**Requirements for physical device:**
- Android 9+ (API 28+)
- Google Play Services updated
- Screen lock enabled (PIN, pattern, or biometric)
- Google account signed in

**Cross-platform testing:**
1. **Register on Android** → Sign in on iOS/web
2. **Register on web** → Sign in on Android
3. **Google Password Manager sync** → Use across devices

## Project Structure

```
frontend-kotlin/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/passkeydemo/android/
│   │   │   ├── MainActivity.kt
│   │   │   ├── PasskeyDemoApp.kt
│   │   │   ├── data/
│   │   │   ├── di/
│   │   │   ├── ui/
│   │   │   └── util/
│   │   └── res/
├── build.gradle.kts
└── settings.gradle.kts
```

## Architecture

- **MVVM Pattern** - ViewModels with Compose UI
- **Credential Manager API** - Android's WebAuthn implementation
- **Hilt** - Dependency injection
- **Retrofit** - Network calls to backend
- **Jetpack Compose** - Modern declarative UI

## Security Notes

- **Biometric required** - Fingerprint, face, or screen lock
- **Hardware-backed keys** - Stored in Android Keystore
- **Domain verification** - Prevents credential phishing
- **Google sync** - Optional passkey backup

## Production Deployment

For production:
1. Update `BASE_URL` in NetworkModule
2. Configure ProGuard rules
3. Enable certificate pinning
4. Add crash reporting
5. Test on multiple Android versions