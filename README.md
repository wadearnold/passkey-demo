# WebAuthn Passkey Demo

A WebAuthn passkey authentication demonstration showcasing passwordless authentication flows across iOS, web, and backend platforms.

![Passkey Demo](https://img.shields.io/badge/WebAuthn-Passkey%20Demo-blue?style=for-the-badge)
![iOS](https://img.shields.io/badge/iOS-16%2B-black?style=flat-square&logo=apple)
![React](https://img.shields.io/badge/React-18-blue?style=flat-square&logo=react)
![Go](https://img.shields.io/badge/Go-1.21-00ADD8?style=flat-square&logo=go)

## 🎯 What This Demonstrates

This project demonstrates **WebAuthn passkey authentication** with:

- **🔐 True passwordless authentication** using biometrics (Face ID, Touch ID, Windows Hello)
- **📱 Cross-platform passkey sharing** between iOS app and web browsers
- **🌐 Go backend** implementing W3C WebAuthn specification
- **⚡ Three authentication flows** demonstrating WebAuthn capabilities
- **🔒 Domain verification** and core WebAuthn security

### ✨ Key Features

- **Passkey Registration**: Create passkeys with username/display name + biometric auth
- **Discoverable Login**: Sign in without entering username (shows all passkeys)
- **Username-based Login**: Enter username, then authenticate with passkey  
- **Cross-platform Sync**: Same passkeys work on iOS, web, and other devices
- **Device Management**: View and manage registered passkeys
- **Real-time Debugging**: Comprehensive logging for development

## 📸 Demo

### iOS App Authentication
```
[🎥 GIF: iOS Face ID authentication flow]
```

### Cross-Platform Login
```
[🎥 GIF: Register on iOS → Login on web with same passkey]
```

### Passkey Management
```
[📷 Screenshot: Dashboard showing passkey details and cross-platform info]
```

## ⚡ Quick Start

Get the demo running in **3 steps**:

### 1. Start ngrok tunnel
```bash
./scripts/start-ngrok.sh
```

### 2. Configure iOS app
```bash
cd frontend-swift
./setup-domain.sh
```

### 3. Setup backend and run
```bash
cd ../backend
./setup-aasa.sh YOUR_TEAM_ID  # From step 2
source ../.env && ./passkey-backend
```

**That's it!** Build the iOS app in Xcode and test cross-platform passkeys.

📖 **Detailed setup**: [Setup Guide](docs/SETUP.md)

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│                 │    │                  │    │                 │
│   iOS App       │    │   Web Frontend   │    │   Backend API   │
│   (Swift)       │    │   (React)        │    │   (Go)          │
│                 │    │                  │    │                 │
│ Face ID/Touch ID│◄──►│ Browser WebAuthn │◄──►│ WebAuthn Server │
│ Secure Enclave  │    │ Credential API   │    │ Session Mgmt    │
│ iCloud Keychain │    │ Local Storage    │    │ User Storage    │
│                 │    │                  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │                         │
                    │    Shared Passkeys      │
                    │  (Cross-Platform Sync)  │
                    │                         │
                    └─────────────────────────┘
```

### 🔧 Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **iOS Frontend** | Swift + SwiftUI + AuthenticationServices | Native iOS passkey experience |
| **Web Frontend** | React + TypeScript + WebAuthn API | Cross-platform web authentication |
| **Backend** | Go + [go-webauthn](https://github.com/go-webauthn/webauthn) | WebAuthn server implementation |
| **Development** | ngrok + automated scripts | Local development with proper domains |

## 📱 Platform Support

### iOS App
- **Requirements**: iOS 16+, Xcode 15+, Apple Developer account
- **Features**: Face ID/Touch ID, iCloud Keychain sync, native UX
- **Security**: Secure Enclave storage, biometric verification

### Web Frontend  
- **Requirements**: Modern browser with WebAuthn support
- **Features**: Platform authenticators, discoverable credentials
- **Compatibility**: Chrome, Safari, Firefox, Edge

### Backend
- **Requirements**: Go 1.21+
- **Standards**: W3C WebAuthn Level 3 compliant
- **Storage**: In-memory storage (demo only - not production)
- **Security**: WebAuthn attestation verification, session management

## 🚀 Use Cases

This demo is great for:

- **🎓 Learning WebAuthn**: Complete implementation with educational comments
- **🏗️ Learning WebAuthn**: Complete implementation with three key flows
- **🔬 Testing passkey flows**: Cross-platform testing environment
- **📚 Documentation**: Reference implementation for WebAuthn integration
- **🎯 Demonstrations**: Showcase WebAuthn authentication flows

## 📚 Documentation

| Guide | Purpose |
|-------|---------|
| [📋 Setup Guide](docs/SETUP.md) | Complete installation and configuration |
| [🏗️ Architecture](ARCHITECTURE.md) | Technical deep-dive and design decisions |
| [💻 Development](docs/DEVELOPMENT.md) | Development workflows and debugging |
| [🔧 Troubleshooting](docs/TROUBLESHOOTING.md) | Common issues and solutions |

### Individual Platform Guides
- [iOS Frontend Setup](frontend-swift/README.md)
- [Web Frontend Setup](frontend-react/README.md)  
- [Backend Setup](backend/README.md)

## 🔒 Security Features

### WebAuthn Compliance
- ✅ **W3C WebAuthn Level 3** specification compliance
- ✅ **FIDO2** certified authenticator support
- ✅ **Attestation verification** for device authenticity
- ✅ **User verification** with biometric confirmation

### Production Security
- ✅ **Associated Domains** for iOS app security
- ✅ **Origin validation** preventing credential theft
- ✅ **Challenge-response** cryptographic verification
- ✅ **Session management** with secure cookies

### Privacy Protection
- ✅ **No passwords stored** anywhere in the system
- ✅ **Biometric data** never leaves the device
- ✅ **Private keys** stored in Secure Enclave/TPM
- ✅ **User consent** required for all operations

## 🤝 Contributing

We welcome contributions! This project serves as both a working demo and educational resource.

### Ways to Contribute
- 🐛 **Bug reports**: Issues with authentication flows
- 💡 **Feature requests**: Additional WebAuthn features to demo
- 📖 **Documentation**: Improve guides and explanations
- 🔧 **Code improvements**: Performance, security, UX enhancements
- 🎥 **Demo content**: Screenshots, videos, tutorials

### Development Setup
1. Follow the [Development Guide](docs/DEVELOPMENT.md)
2. Make your changes with comprehensive comments
3. Test across all platforms (iOS, web, backend)
4. Submit PR with detailed description

## 📊 Project Status

| Component | Status | Notes |
|-----------|--------|-------|
| iOS Frontend | ✅ Complete | All passkey flows working |
| Web Frontend | ✅ Complete | Cross-platform sync verified |
| Backend | ✅ Complete | Production-ready implementation |
| Documentation | ✅ Complete | Comprehensive guides and troubleshooting |
| CI/CD | 🚧 In Progress | Automated testing setup |
| Demo Videos | 📋 Planned | Recording cross-platform flows |

## 🏆 Acknowledgments

- **[go-webauthn](https://github.com/go-webauthn/webauthn)**: Excellent Go WebAuthn library
- **[WebAuthn.io](https://webauthn.io/)**: Great testing and learning resource
- **[Passkeys.dev](https://passkeys.dev/)**: Comprehensive passkey documentation
- **Apple & FIDO Alliance**: For advancing passwordless authentication

## 📄 License

This project is licensed under the [BSD 3-Clause License](LICENSE) - the same as the upstream WebAuthn library.

## 🔗 Links

- **[WebAuthn Specification](https://w3c.github.io/webauthn/)**: Official W3C standard
- **[FIDO Alliance](https://fidoalliance.org/)**: Industry standards organization  
- **[Passkeys](https://developer.apple.com/passkeys/)**: Apple's passkey documentation
- **[WebAuthn Guide](https://webauthn.guide/)**: Excellent learning resource

---

**🎉 Ready to try passwordless authentication?** [Get started with the setup guide!](docs/SETUP.md)