package com.focusguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusguard.data.PinManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPinScreen(
    onNavigateBack: () -> Unit,
    onPinResetSuccess: () -> Unit
) {
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }
    var unlockCode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recover PIN") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Developer Support Recovery",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "WARNING: This is for extreme cases only. It may take more than 24 hours to verify and receive your unlock code. Please try to remember your PIN.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "To request an unlock code, email support@focusguard.com with your exact Support ID below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your Support ID", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = pinManager.getSupportId(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = unlockCode,
                onValueChange = { if (it.length <= 6) unlockCode = it },
                label = { Text("Enter 6-Digit Unlock Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error.isNotEmpty()
            )
            
            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (pinManager.verifySupportUnlockCode(unlockCode)) {
                        pinManager.clearPin() // Clear the old PIN
                        onPinResetSuccess()   // Navigate back to Setup Pin Screen
                    } else {
                        error = "Invalid Unlock Code"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = unlockCode.length == 6
            ) {
                Text("Verify & Reset PIN")
            }
        }
    }
}
