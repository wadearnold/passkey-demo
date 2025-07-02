# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with the passkey-demo repository.

## Repository Context

This is a WebAuthn passkey authentication demonstration with cross-platform support across iOS, web, and backend platforms.

## Project Structure

```
passkey-demo/
├── backend/          # Go WebAuthn server (in-memory storage)
├── frontend-react/   # React web frontend
├── frontend-swift/   # iOS Swift app
├── scripts/          # Setup automation scripts
└── docs/            # Documentation
```

## Development Modes

### ngrok Mode (Required for iOS)
**Always use ngrok for iOS development.** iOS requires a proper domain for Associated Domains.

```bash
# 1. Start ngrok
./scripts/start-ngrok.sh

# 2. Configure iOS app
cd frontend-swift && ./setup-domain.sh

# 3. Configure backend
cd ../backend && ./setup-aasa.sh YOUR_TEAM_ID
```

### Local Mode (Web-only)
For rapid web development without iOS:

```bash
# Backend on localhost
cd backend && go run .

# React dev server
cd frontend-react && npm run dev
```

## Important Rules

### WebAuthn Security
- **RPID must match origin**: If RPID="localhost", access via http://localhost:*
- **RPID must match origin**: If RPID="abc.ngrok.io", access via https://abc.ngrok.io
- This is a WebAuthn security requirement, not a bug

### iOS Development
- **Never use localhost for iOS testing** - Associated Domains require real FQDN
- **Always test on real devices** - Simulator has entitlements limitations
- **Run setup scripts after ngrok restarts** - Domain changes require reconfiguration

## Common Commands

### Testing
```bash
# Backend tests
cd backend && go test ./...

# Frontend builds
cd frontend-react && npm run build
cd frontend-swift && xcodebuild
```

### Debugging
- **iOS logs**: Xcode console + Console.app (filter: `swcd`)
- **Backend logs**: Custom logger with timestamp correlation
- **AASA verification**: `curl https://domain/.well-known/apple-app-site-association`

## Architecture Overview

### Authentication Flows
1. **Passkey Registration**: Create passkey with username/display name + biometric auth
2. **Discoverable Login**: Sign in without username (shows all passkeys)
3. **Username-based Login**: Enter username, then authenticate with passkey

### Key Components
- **Backend**: Go + go-webauthn library, in-memory storage (demo only)
- **iOS**: Swift + AuthenticationServices framework, Secure Enclave storage
- **Web**: React + WebAuthn JavaScript API

### Cross-Platform Requirements
- Same RPID across all platforms
- Domain verification (iOS Associated Domains, AASA file)
- HTTPS for web (ngrok provides this)

## Current Limitations

- **In-memory storage**: Data lost on backend restart
- **No user management**: Simple demo without user profiles
- **ngrok free tier**: Domain changes on restart
- **Development focus**: Not optimized for production use

## Future Enhancements

Potential improvements for production use:
- Database storage (PostgreSQL, SQLite)
- User registration and profile management
- Stable domain configuration
- Rate limiting and security hardening
- Monitoring and analytics
- Profile deep linking in iOS app