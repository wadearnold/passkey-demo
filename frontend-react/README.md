# Passkey Demo - React Frontend

React web application demonstrating WebAuthn passkey authentication with cross-platform support.

## Configuration

### Backend URL
The frontend connects to the backend API, which can be configured in different ways:
- **Localhost mode**: `http://localhost:8080` (default)
- **ngrok mode**: Automatically uses `NGROK_URL` from `../.env`

### Environment Setup

**For localhost development:**
```bash
npm run dev:localhost
# Uses http://localhost:8080 for API
```

**For ngrok (cross-platform testing):**
```bash
# 1. Start ngrok first (from root directory)
../scripts/start-ngrok.sh

# 2. Run with ngrok configuration
npm run dev
# Automatically uses NGROK_URL from ../.env
```

## Running the Frontend

```bash
# Install dependencies
npm install

# Development modes
npm run dev           # Auto-detects ngrok from ../.env, falls back to localhost
npm run dev:localhost # Force localhost mode
npm run dev:direct    # Run Vite directly without configuration scripts

# Production builds
npm run build        # Standard build (localhost)
npm run build:ngrok  # Build with ngrok URL from ../.env

# Preview production build
npm run preview
```

## Features

- **Passkey Registration**: Create passkeys with biometric authentication
- **Discoverable Login**: Sign in without entering username
- **Username-based Login**: Traditional flow with passkey authentication
- **Deep Link Support**: Protected routes with authentication redirect
- **Passkey Management**: View and delete registered passkeys

## Debugging

### Browser Console

The app logs detailed WebAuthn information:

```javascript
// Check during registration
console.log('WebAuthn options:', publicKeyCredentialCreationOptions);
console.log('Platform authenticator available:', 
  await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable());

// Check during login
console.log('Assertion options:', publicKeyCredentialRequestOptions);
console.log('Credential response:', credential);
```

### Common Issues

**"WebAuthn not supported" error**
- Check browser compatibility (Chrome 67+, Safari 14+, Firefox 60+)
- Ensure HTTPS or localhost connection
- Verify browser security settings

**CORS errors**
- Ensure backend is running
- Check that frontend and backend are using matching configurations (both localhost or both ngrok)
- Verify backend CORS configuration includes frontend origin

**"RPID mismatch" error**
- Access frontend via same domain as backend RPID
- For localhost: use `http://localhost:5173`
- For ngrok: ensure both frontend and backend use ngrok URLs

**Biometrics not working**
- Check system biometric settings
- Try different browser (Safari on macOS often works better)
- Clear browser data and retry
- See troubleshooting section below

### Biometric Authentication Troubleshooting

If passkeys request system password instead of biometrics:

**1. Verify WebAuthn Configuration**
```javascript
// Should see in console:
Platform authenticator available: true
authenticatorAttachment: 'platform'
userVerification: 'required'
```

**2. System Settings**
- **macOS**: System Settings → Touch ID → Enable for browser
- **Windows**: Settings → Sign-in options → Set up Windows Hello
- **iOS**: Settings → Face ID → Enable for Safari
- **Android**: Settings → Passwords → Enable passkeys

**3. Browser Settings**
- Chrome: `chrome://settings/securityKeys`
- Safari: Preferences → AutoFill → Passkeys
- Edge: Settings → Profiles → Passwords

**4. Test with Other Sites**
- Try [webauthn.io](https://webauthn.io)
- If those also use password, it's a system configuration issue

## Project Structure

```
src/
├── components/
│   ├── Dashboard.jsx      # Main UI with all auth flows
│   ├── LoginForm.jsx      # Login UI component
│   ├── RegisterForm.jsx   # Registration UI
│   └── Profile.jsx        # Protected route example
├── hooks/
│   └── useWebAuthn.js     # WebAuthn API wrapper
├── services/
│   └── api.js            # Backend API client
└── App.jsx               # Routes and layout
```

## Development Tips

### Testing Different Flows
1. **Registration**: Create multiple test users
2. **Discoverable Login**: Test without username
3. **Protected Routes**: Try accessing `/profile` directly
4. **Cross-Platform**: Register on one device, login on another

### API Integration
All backend calls are in `src/services/api.js`:
- Registration: `/api/register/begin` and `/api/register/finish`
- Authentication: `/api/login/begin` and `/api/login/finish`
- User management: `/api/user/profile` and `/api/user/passkeys`

The API base URL is determined by:
1. Checking for `VITE_API_URL` environment variable
2. Checking for ngrok configuration in build scripts
3. Falling back to `http://localhost:8080`

### Error Handling
The app displays user-friendly error messages while logging details to console. Check console for:
- WebAuthn API errors
- Network request failures
- Validation errors

## Production Build

```bash
# Build for production with localhost
npm run build

# Build for production with ngrok
npm run build:ngrok

# Test production build locally
npm run preview

# Deploy dist/ folder to your hosting service
```

### Build Scripts
- `start-dev.sh`: Configures development server with ngrok if available
- `start-localhost.sh`: Forces localhost configuration
- `build-with-ngrok.sh`: Production build with ngrok URL injection