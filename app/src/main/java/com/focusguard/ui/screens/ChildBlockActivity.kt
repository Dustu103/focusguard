package com.focusguard.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguard.admin.DeviceAdminReceiver
import com.focusguard.data.PinManager
import com.focusguard.ui.theme.FocusGuardTheme

class ChildBlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val blockedPackage = intent.getStringExtra("blocked_package") ?: "This app"
        val isDeviceAdminDisable = intent.getBooleanExtra("is_device_admin_disable", false)
        val isDailyLimit = intent.getStringExtra("block_reason") == "DAILY_LIMIT"

        val appName = try {
            val pm = packageManager
            val ai = pm.getApplicationInfo(blockedPackage, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: Exception) {
            blockedPackage
        }

        setContent {
            FocusGuardTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isDeviceAdminDisable) {
                        // --- PIN gate for uninstall/disable admin attempt ---
                        UninstallPinGate(
                            onCorrectPin = {
                                // Parent verified — allow deactivation by finishing
                                // The system dialog will continue after this activity closes
                                finish()
                            },
                            onWrongPin = {
                                // Send child back to home
                                goHome()
                            }
                        )
                    } else {
                        // --- App blocked overlay ---
                        BlockedAppOverlay(appName = appName, isDailyLimit = isDailyLimit)
                    }
                }
            }
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        goHome()
    }
}

@Composable
private fun UninstallPinGate(onCorrectPin: () -> Unit, onWrongPin: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val pinManager = remember { PinManager(context) }

    var pin by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(0) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Parent Verification Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Enter your parent PIN to allow this change. FocusGuard protection will be removed if verified.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it; errorMsg = null },
                    label = { Text("Parent PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorMsg != null,
                    supportingText = errorMsg?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onWrongPin,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }

                    Button(
                        onClick = {
                            if (pinManager.verifyPin(pin)) {
                                onCorrectPin()
                            } else {
                                attempts++
                                pin = ""
                                errorMsg = if (attempts >= 3)
                                    "Too many wrong attempts. Access denied."
                                else
                                    "Wrong PIN. ${3 - attempts} attempts left."
                                if (attempts >= 3) onWrongPin()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Verify") }
                }
            }
        }
    }
}

@Composable
private fun BlockedAppOverlay(appName: String, isDailyLimit: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(if (isDailyLimit) "⏰" else "🔒", fontSize = 56.sp)
                Text(
                    if (isDailyLimit) "Time's Up" else "App Blocked",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (isDailyLimit)
                        "You've reached today's time limit for $appName."
                    else
                        "$appName has been blocked by FocusGuard Parental Controls.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
