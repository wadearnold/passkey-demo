# Passkey Demo Backend

Go WebAuthn server implementing passwordless authentication with cross-platform passkey support.

## Configuration

### Environment Variables
- `NGROK_URL`: Full ngrok URL (e.g., `https://abc123.ngrok.io`)
- `PORT`: Server port (default: 8080)

The backend automatically detects ngrok configuration:
1. Checks for `NGROK_URL` environment variable
2. If found, uses ngrok domain for RPID and origins
3. If not found or `-localhost` flag used, defaults to localhost

### Command Line Flags
- `-localhost`: Force localhost mode, ignoring NGROK_URL
- `-h`: Show help

### Running the Backend

**Local Development (Web Only)**
```bash
go run . -localhost
# RPID: localhost
# Origin: http://localhost:*
```

**Cross-Platform Testing (iOS/Android)**
```bash
# 1. Start ngrok first (creates .env file)
cd ..
./scripts/start-ngrok.sh

# 2. Run backend with ngrok configuration
cd backend
source ../.env && go run .
# RPID: your-tunnel.ngrok.io
# Origin: https://your-tunnel.ngrok.io
```

**Alternative: Manual ngrok configuration**
```bash
# If running ngrok manually
NGROK_URL=https://abc123.ngrok.io go run .
```

**Production Build**
```bash
go build -o passkey-backend .

# Run with ngrok
source ../.env && ./passkey-backend

# Or force localhost
./passkey-backend -localhost
```

## API Endpoints

### Registration
- `POST /api/register/begin` - Start passkey registration
- `POST /api/register/finish` - Complete registration with credential

### Authentication  
- `POST /api/login/begin` - Start authentication (with/without username)
- `POST /api/login/finish` - Complete authentication with assertion

### User Management
- `GET /api/user/profile` - Get current user info
- `GET /api/user/passkeys` - List user's passkeys
- `DELETE /api/user/passkeys/{id}` - Remove a passkey
- `POST /api/logout` - End session

### Utility
- `GET /api/health` - Health check
- `GET /.well-known/apple-app-site-association` - iOS app association

## iOS Configuration (AASA)

For iOS app integration, the backend serves an Apple App Site Association file:

```bash
# Generate AASA file with your Team ID
./setup-aasa.sh YOUR_TEAM_ID

# Creates aasa.json with:
{
  "webcredentials": {
    "apps": ["YOUR_TEAM_ID.com.passkey.demo.ios"]
  }
}
```

The backend automatically serves this file at:
- `/.well-known/apple-app-site-association`

This enables iOS apps to use passkeys created by the backend.

## Debugging

### Common Issues

**RPID Mismatch Error**
- Ensure you're accessing via the correct domain
- Check RPID matches your access URL exactly
- For ngrok: use the full HTTPS URL

**CORS Errors**
- Backend automatically configures CORS for known origins
- Check browser console for specific CORS errors
- Verify frontend is using correct backend URL

**Session Issues**
- Sessions expire after 24 hours
- Check cookies are being set (HttpOnly, Secure)
- Ensure HTTPS for production domains

### Debug Logging

The backend uses structured logging with correlation IDs:

```bash
# View all logs
go run . 2>&1 | grep -E "Starting|Error|Session"

# Filter by operation
go run . 2>&1 | grep "Registration"

# Watch for errors
go run . 2>&1 | grep -E "Error|Failed"
```

### Testing Commands

```bash
# Health check
curl http://localhost:8080/api/health

# Test CORS
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS http://localhost:8080/api/register/begin

# Test registration (use browser or Postman for full flow)
curl -X POST http://localhost:8080/api/register/begin \
     -H "Content-Type: application/json" \
     -d '{"username":"test","displayName":"Test User"}'
```

## Code Organization

```
backend/
├── main.go          # Server setup and configuration
├── handlers.go      # HTTP request handlers
├── models.go        # Data models and storage
├── middleware.go    # CORS, logging, sessions
└── TUTORIAL.md      # WebAuthn implementation guide
```

## Security Notes

- **In-memory storage**: Data lost on restart (demo only)
- **RPID validation**: Strict origin checking for WebAuthn security
- **Session management**: HTTP-only cookies with 24-hour expiration
- **Input validation**: Username format restrictions
- **No attestation**: Uses "none" for demo simplicity

## Production Considerations

For production deployment:
1. Replace in-memory storage with database
2. Add rate limiting on auth endpoints
3. Implement proper logging/monitoring
4. Use environment-specific configuration
5. Add backup credential recovery flow