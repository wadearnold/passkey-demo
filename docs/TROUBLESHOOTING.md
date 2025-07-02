# Troubleshooting Guide

## Common Issues

### iOS App Issues

#### "Application not associated with domain" Error
**Symptoms:** iOS registration/login fails with domain association error

**Solution:**
1. Run `./setup-domain.sh` to update entitlements
2. Ensure backend AASA file is configured: `./setup-aasa.sh YOUR_TEAM_ID`
3. Clean build folder in Xcode (Cmd+Shift+K)
4. Delete app and reinstall on device

#### Passkey Registration Fails
**Check:**
- Using real device (not simulator)
- ngrok tunnel is active
- Backend is running and accessible
- Entitlements file is linked in Xcode project

#### Discoverable Login Shows No Passkeys
**Possible causes:**
- No passkeys registered for this domain
- Domain mismatch between app and backend
- iOS Keychain sync issues

**Solution:**
- Verify domain configuration matches between iOS and backend
- Check Console.app logs for domain verification errors

### Backend Issues

#### AASA File Not Found (404)
**Check backend logs for:**
```
GET /.well-known/apple-app-site-association
```

**Solution:**
- Run `./setup-aasa.sh YOUR_TEAM_ID` 
- Verify Team ID matches iOS app configuration
- Restart backend after updating AASA

#### Registration/Login API Errors
**Common issues:**
- CORS errors (check browser console)
- Invalid request format
- Session data corruption (restart backend to clear)

### ngrok Issues

#### ngrok URL Changes
**When ngrok restarts with new domain:**
1. Update iOS: `./setup-domain.sh`
2. Update backend: `./setup-aasa.sh YOUR_TEAM_ID`
3. Clean build iOS app
4. Test flows again

#### ngrok Rate Limits
**Free ngrok limitations:**
- Connection limits
- Request rate limits
- Domain changes on restart

**Solutions:**
- Use ngrok paid plan for stable domains
- Restart ngrok session if hitting limits

### Web Frontend Issues

#### WebAuthn Not Supported
**Browser requirements:**
- Chrome 67+, Safari 14+, Firefox 60+, Edge 18+
- HTTPS required (localhost exception)

#### Passkey Registration/Login Fails
**Check:**
- Browser console for errors
- Network tab for API call failures
- Ensure accessing via correct domain (ngrok URL)

### Cross-Platform Issues

#### Passkeys Don't Sync Between Devices
**Expected behavior:**
- Same Apple ID: Passkeys sync via iCloud Keychain
- Different Apple IDs: Separate passkeys per account
- iOS to Web: Works when using same domain/RPID

#### Different Behavior Between Platforms
**Verify:**
- Both use same backend domain
- RPID matches exactly
- Both platforms use same user account

## Debugging Tools

### iOS Debugging
**Xcode Console:**
```
🚀 Starting registration for username: alice
📋 Registration options received
✅ Registration successful
```

**macOS Console.app:**
- Filter: `swcd` for system-level WebAuthn logs
- Look for domain verification messages

### Backend Debugging
**Server logs show:**
```
2024/07/02 17:30:15    POST /begin-registration
2024/07/02 17:30:15    Registration successful for user: alice
```

### Network Debugging
**Test AASA file:**
```bash
curl https://your-ngrok-domain.ngrok-free.app/.well-known/apple-app-site-association
```

**Expected response:**
```json
{
  "webcredentials": {
    "apps": ["YOUR_TEAM_ID.com.passkey.demo.ios"]
  }
}
```

## Reset Procedures

### Complete Reset
1. Delete iOS app from device
2. Clear backend (restart server)
3. Get fresh ngrok URL
4. Reconfigure everything from scratch

### iOS-only Reset
1. Delete app from device
2. Clean Xcode build folder
3. Rebuild and reinstall

### Backend-only Reset
1. Restart backend server (clears in-memory data)
2. Verify AASA file is accessible

## Getting Help

1. **Check logs first**: Xcode console, backend logs, Console.app
2. **Verify configuration**: Domain matching, Team ID, entitlements
3. **Test systematically**: One platform at a time
4. **Use real devices**: iOS simulator has limitations
5. **Fresh start**: When in doubt, reset everything

## Known Limitations

- **iOS Simulator**: Associated domains don't work reliably
- **In-memory storage**: Backend loses data on restart
- **ngrok free**: Domain changes on restart
- **Development mode**: Not optimized for production use