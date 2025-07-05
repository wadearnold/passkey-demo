package com.passkeydemo.android.util

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object DeviceCompatibility {
    private const val TAG = "DeviceCompatibility"
    
    fun checkPasskeySupport(context: Context): CompatibilityResult {
        Log.d(TAG, "Checking device compatibility...")
        
        // Check Android version (need API 28+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Log.e(TAG, "Android version too old: ${Build.VERSION.SDK_INT}")
            return CompatibilityResult(
                isSupported = false,
                reason = "Android 9 (API 28) or higher required. Current: API ${Build.VERSION.SDK_INT}"
            )
        }
        
        // Check Google Play Services
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play Services issue: $resultCode")
            return CompatibilityResult(
                isSupported = false,
                reason = when (resultCode) {
                    ConnectionResult.SERVICE_MISSING -> "Google Play Services not installed"
                    ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "Google Play Services update required"
                    ConnectionResult.SERVICE_DISABLED -> "Google Play Services disabled"
                    else -> "Google Play Services not available (code: $resultCode)"
                }
            )
        }
        
        // Check screen lock
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isDeviceSecure) {
            Log.e(TAG, "No screen lock set")
            return CompatibilityResult(
                isSupported = false,
                reason = "Screen lock required. Please set up PIN, pattern, or biometric lock"
            )
        }
        
        // Check if running on emulator
        val isEmulator = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"))
        
        if (isEmulator) {
            Log.w(TAG, "Running on emulator - passkeys may not work properly")
            return CompatibilityResult(
                isSupported = true,
                warning = "Running on emulator. Passkeys may not work properly. Use a physical device for best results."
            )
        }
        
        Log.d(TAG, "Device is compatible with passkeys")
        return CompatibilityResult(isSupported = true)
    }
    
    data class CompatibilityResult(
        val isSupported: Boolean,
        val reason: String? = null,
        val warning: String? = null
    )
}