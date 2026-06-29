package com.focusguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import com.focusguard.data.PinManager
import kotlinx.coroutines.launch

@Composable
fun SetupPinScreen(onPinSet: () -> Unit) {
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) } // 1 = enter, 2 = confirm, 3 = recovery
    var error by remember { mutableStateOf("") }
    var recoveryKey by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    if (step == 3) {
        RecoveryKeyScreen(
            recoveryKey = recoveryKey,
            onFinish = onPinSet
        )
        return
    }


    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null,
                 modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = if (step == 1) "Set your Parent PIN" else "Confirm your PIN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your child will not be able to change this",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        // Premium PIN dots display
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(6) { index ->
                val currentPin = if (step == 1) pin else confirmPin
                val isFilled = index < currentPin.length
                val dotSize = if (isFilled) 20.dp else 16.dp
                Box(
                    modifier = Modifier
                        .size(24.dp), // Fixed container size
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .background(
                                color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            androidx.compose.material3.Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = error, 
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        
        if (isVerifying) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        }

        Spacer(Modifier.height(32.dp))

        NumberPad(
            enabled = !isVerifying,
            onNumberClick = { digit ->
                error = "" // clear error on typing
                if (step == 1 && pin.length < 6) {
                    pin += digit
                    if (pin.length == 6) step = 2
                } else if (step == 2 && confirmPin.length < 6) {
                    confirmPin += digit
                    if (confirmPin.length == 6) {
                        isVerifying = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(250) // Allow 6th dot to render visually
                            if (pin == confirmPin) {
                                // Generate random recovery key
                                val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
                                val randomKey = (1..8).map { chars.random() }.joinToString("")
                                recoveryKey = "${randomKey.substring(0,4)}-${randomKey.substring(4,8)}"
                                
                                pinManager.setRecoveryKey(recoveryKey)
                                pinManager.setPin(pin)
                                step = 3
                            } else {
                                error = "PINs don't match. Try again."
                                pin = ""; confirmPin = ""
                                step = 1
                            }
                            isVerifying = false
                        }
                    }
                }
            },
            onDelete = {
                error = ""
                if (step == 1 && pin.isNotEmpty()) pin = pin.dropLast(1)
                else if (step == 2 && confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
            }
        )
    }
}

@Composable
fun RecoveryKeyScreen(recoveryKey: String, onFinish: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null,
                modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.error)
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text("Recovery Key", style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "If you forget your PIN, this key is the ONLY way to unlock FocusGuard. Keep it safe.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(Modifier.height(40.dp))
        
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ) {
            Text(
                text = recoveryKey,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(32.dp).align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = {
                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                    data = android.net.Uri.parse("mailto:")
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "FocusGuard Recovery Key")
                    putExtra(android.content.Intent.EXTRA_TEXT, "My FocusGuard Recovery Key is: $recoveryKey\n\nKeep this email safe. You will need it if you ever forget your parent PIN.")
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Email Backup"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Email Backup to Myself")
        }
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I have saved it. Continue.")
        }
    }
}

@Composable
fun NumberPad(enabled: Boolean = true, onNumberClick: (String) -> Unit, onDelete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )
        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                for (key in row) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                color = if (key.isNotEmpty()) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable(enabled = enabled && key.isNotEmpty(), onClick = {
                                if (key == "DEL") onDelete() else onNumberClick(key)
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "DEL") {
                            Text(
                                text = "DEL", 
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (key.isNotEmpty()) {
                            Text(
                                text = key, 
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}



