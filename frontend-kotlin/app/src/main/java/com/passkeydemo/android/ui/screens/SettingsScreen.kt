package com.passkeydemo.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("passkey_demo", android.content.Context.MODE_PRIVATE) }
    var ngrokUrl by remember { mutableStateOf(prefs.getString("ngrok_url", "") ?: "") }
    var showSnackbar by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text("Settings saved. Restart the app to apply changes.")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Server Configuration",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "Enter your ngrok URL to connect to the backend server",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = ngrokUrl,
                        onValueChange = { ngrokUrl = it },
                        label = { Text("ngrok URL") },
                        placeholder = { Text("https://abc123.ngrok.io") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    prefs.edit().putString("ngrok_url", ngrokUrl).apply()
                                    showSnackbar = true
                                }
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Save")
                            }
                        }
                    )
                    
                    Text(
                        text = "Leave empty to use localhost (emulator only)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "How to get your ngrok URL",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Text(
                        text = """1. Run ./scripts/start-ngrok.sh from the project root
2. Copy the HTTPS URL shown (e.g., https://abc123.ngrok.io)
3. Paste it above and save
4. Restart the app""",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Current URL info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Debug Info",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "Current URL: ${ngrokUrl.ifEmpty { "Using default (localhost/10.0.2.2)" }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}