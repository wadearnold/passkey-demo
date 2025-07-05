package com.passkeydemo.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun StartupScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("passkey_demo", android.content.Context.MODE_PRIVATE) }
    val hasNgrokUrl = remember { !prefs.getString("ngrok_url", null).isNullOrEmpty() }
    
    LaunchedEffect(hasNgrokUrl) {
        if (hasNgrokUrl) {
            // If we have a URL, go to home after a short delay
            delay(1000)
            onNavigateToHome()
        } else {
            // If no URL, go to settings
            delay(1500)
            onNavigateToSettings()
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (hasNgrokUrl) "Loading..." else "Configuration needed",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (!hasNgrokUrl) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Redirecting to settings...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}