# WebAuthn Passkey Demo

Cross-platform passwordless authentication demonstration using WebAuthn passkeys.

## Purpose & Features

This project demonstrates WebAuthn passkey authentication with:
- **Passwordless login** using Face ID, Touch ID, or Windows Hello
- **Cross-platform passkeys** that work seamlessly between iOS app and web browsers
- **Three auth flows**: Registration, discoverable login, and username-based login
- **Complete implementation** with iOS (Swift), web (React), and backend (Go)

## Quick Start

### Prerequisites
- ngrok account (free tier works)
- iOS: Xcode 15+, Apple Developer account
- Backend: Go 1.21+
- Frontend: Node.js 18+

### Setup with ngrok (Required for iOS)

```bash
# 1. Start ngrok tunnel
./scripts/start-ngrok.sh

# 2. Configure iOS app (note your Team ID)
cd frontend-swift
./setup-domain.sh

# 3. Setup backend with your Team ID
cd ../backend
./setup-aasa.sh YOUR_TEAM_ID
source ../.env && ./passkey-backend

# 4. Start React frontend
cd ../frontend-react
npm install && npm run dev

# 5. Build iOS app in Xcode
```

### Local Development (Web Only)

```bash
# Backend with localhost flag
cd backend
go run . -localhost

# React frontend
cd frontend-react
npm install && npm run dev

# Access at http://localhost:3000
```

## Project Structure

```
passkey-demo/
├── backend/          # Go WebAuthn server
├── frontend-react/   # React web app
├── frontend-swift/   # iOS native app
├── scripts/          # Setup automation
└── docs/            # Documentation
```

## Platform Guides

- [Backend Configuration & Debugging](backend/README.md)
- [React Frontend Setup](frontend-react/README.md)
- [iOS App Development](frontend-swift/README.md)

## Key Implementation Notes

### WebAuthn Security
- **RPID must match origin**: localhost for local dev, ngrok domain for iOS testing
- **HTTPS required**: ngrok provides this automatically
- **Domain verification**: iOS requires Associated Domains configuration

### Cross-Platform Requirements
- Same RPID across all platforms
- Proper AASA file for iOS
- WebAuthn-capable browser/device

## Troubleshooting

Common issues:
- **"RPID mismatch"**: Ensure you're accessing via the correct domain
- **iOS not recognizing passkeys**: Check AASA file and Associated Domains
- **Data loss on restart**: Backend uses in-memory storage (demo only)

See [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for detailed solutions.

## License

BSD 3-Clause License (same as go-webauthn library)