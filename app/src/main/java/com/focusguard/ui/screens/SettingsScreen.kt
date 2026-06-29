package com.focusguard.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focusguard.admin.DeviceAdminReceiver
import com.focusguard.data.PinManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }

    // Dialog state
    var showRemoveDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var removalDone by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Security Group
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "SECURITY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    SettingRow(
                        title = "Change PIN",
                        subtitle = "Update your parent lock PIN",
                        icon = Icons.Default.Lock,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { showChangePinDialog = true }
                    )
                }
            }

            // Danger Zone
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "DANGER ZONE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    SettingRow(
                        title = "Remove Parental Controls",
                        subtitle = "Deactivate Device Admin so you can uninstall",
                        icon = Icons.Default.DeleteForever,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = { showRemoveDialog = true }
                    )
                }
            }

            // Support Group
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "SUPPORT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    SettingRow(
                        title = "Buy me a coffee",
                        subtitle = "Support FocusGuard development",
                        icon = Icons.Default.Favorite,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/yourname"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                "FocusGuard v1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
    }

    // ── Remove Protection Dialog ───────────────────────────────────────────────
    if (showRemoveDialog) {
        PinConfirmDialog(
            title = "Remove Parental Controls",
            message = if (removalDone)
                "✅ Device Admin removed. You can now go to Settings → Apps → FocusGuard → Uninstall."
            else
                "Enter your Parent PIN to deactivate Device Admin protection. After this, FocusGuard can be uninstalled.",
            confirmLabel = if (removalDone) "Close" else "Remove Protection",
            confirmIsDestructive = true,
            pinManager = pinManager,
            skipPinIfDone = removalDone,
            onConfirm = {
                if (!removalDone) {
                    // Programmatically remove Device Admin — only parent can do this
                    val dpm = context.getSystemService(android.content.Context.DEVICE_POLICY_SERVICE)
                            as DevicePolicyManager
                    val comp = ComponentName(context, DeviceAdminReceiver::class.java)
                    if (dpm.isAdminActive(comp)) {
                        dpm.removeActiveAdmin(comp)
                    }
                    removalDone = true
                } else {
                    showRemoveDialog = false
                    removalDone = false
                }
            },
            onDismiss = {
                showRemoveDialog = false
                removalDone = false
            }
        )
    }

    // ── Change PIN Dialog ──────────────────────────────────────────────────────
    if (showChangePinDialog) {
        ChangePinDialog(
            pinManager = pinManager,
            onDismiss = { showChangePinDialog = false }
        )
    }
}

// ── Pin Confirm Dialog ─────────────────────────────────────────────────────────
@Composable
private fun PinConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    confirmIsDestructive: Boolean,
    pinManager: PinManager,
    skipPinIfDone: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (confirmIsDestructive) Icons.Default.Warning else Icons.Default.Lock,
                contentDescription = null,
                tint = if (confirmIsDestructive) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (!skipPinIfDone) {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6) { pin = it; error = null } },
                        label = { Text("Parent PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = error != null,
                        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (skipPinIfDone) {
                        onConfirm()
                    } else if (pinManager.verifyPin(pin)) {
                        error = null
                        onConfirm()
                    } else {
                        pin = ""
                        error = "Wrong PIN. Try again."
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (confirmIsDestructive) MaterialTheme.colorScheme.error
                                     else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(confirmLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!skipPinIfDone) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

// ── Change PIN Dialog ──────────────────────────────────────────────────────────
@Composable
private fun ChangePinDialog(pinManager: PinManager, onDismiss: () -> Unit) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(if (success) "PIN Changed!" else "Change PIN", fontWeight = FontWeight.Bold) },
        text = {
            if (success) {
                Text(
                    "Your PIN has been updated successfully.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { if (it.length <= 6) { currentPin = it; error = null } },
                        label = { Text("Current PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6) { newPin = it; error = null } },
                        label = { Text("New PIN (4–6 digits)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 6) { confirmPin = it; error = null } },
                        label = { Text("Confirm New PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = error != null,
                        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (success) { onDismiss(); return@Button }
                when {
                    newPin.length < 4 -> error = "New PIN must be at least 4 digits."
                    newPin != confirmPin -> error = "PINs do not match."
                    !pinManager.verifyPin(currentPin) -> { currentPin = ""; error = "Current PIN is wrong." }
                    else -> { pinManager.setPin(newPin); success = true }
                }
            }) {
                Text(if (success) "Done" else "Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!success) TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconTint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
