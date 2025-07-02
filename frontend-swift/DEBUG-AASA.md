# Debugging AASA (Apple App Site Association) Issues

## The Problem
iOS is not recognizing the domain association even though:
- Team ID is correct (927BS6LD3W)
- Entitlements file has the ngrok domain
- AASA file is accessible and correct

## iOS AASA Caching Behavior

iOS caches AASA files aggressively. When an app is installed:
1. iOS fetches the AASA file from the associated domains
2. This fetch happens ONCE during app installation
3. The association is cached until the app is reinstalled

## Force AASA Refresh - Method 1: Complete Reinstall

1. **Delete the app completely** from device/simulator
2. **In Xcode**: Product → Clean Build Folder (Cmd+Shift+K)
3. **Reset simulator** (if using simulator): Device → Erase All Content and Settings
4. **Wait 30 seconds** (let iOS clear caches)
5. **Build and Run** fresh install

## Force AASA Refresh - Method 2: Developer Mode

For iOS 16+, you can use developer mode to force refresh:

1. On device: Settings → Developer → Universal Links → Diagnostics
2. Enter your domain: `67e9-76-154-22-254.ngrok-free.app`
3. Tap "Test" to force fetch AASA

## Verify AASA Format

The AASA file must be served with correct headers:

```bash
curl -I https://67e9-76-154-22-254.ngrok-free.app/.well-known/apple-app-site-association
```

Should show:
- `Content-Type: application/json`
- Status: 200 OK

## Alternative Testing Approach

If AASA issues persist, test with a direct domain:

1. **Use CDN mode in Console** (if available)
   - This provides a stable domain that doesn't change
   - Update entitlements with the CDN domain

2. **Or use a custom domain**
   - Point a domain you control to ngrok
   - This avoids issues with dynamic ngrok subdomains

## Check Device Logs

On macOS with device connected:
1. Open Console.app
2. Select your device
3. Filter for "swcd" (Shared Web Credentials Daemon)
4. Look for AASA fetch attempts and errors

## Common AASA Issues

1. **SSL Certificate**: ngrok free tier shows warning page first time
   - Visit the ngrok URL in Safari first
   - Accept the warning to establish trust

2. **Subdomain Changes**: Each ngrok restart = new subdomain
   - iOS still has old subdomain cached
   - Requires full reinstall each time

3. **Timing**: AASA fetch happens at install time
   - Not when app launches
   - Not when you update entitlements

## Quick Test

Test if the issue is AASA or WebAuthn:

1. Try **username-based login** instead of discoverable
   - This still requires AASA but different flow
   - If this also fails, it's definitely AASA

2. Try **localhost mode**
   - Change APIService baseURL to localhost
   - This uses developer mode entitlements
   - If this works, confirms ngrok AASA issue