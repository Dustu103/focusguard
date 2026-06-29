package com.focusguard.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.filled.Apps
import com.focusguard.data.BlockedApp
import com.focusguard.data.AppDatabase
import com.focusguard.data.PinManager
import com.focusguard.services.AppBlockerService
import kotlinx.coroutines.launch

import com.focusguard.utils.PermissionUtils.isAccessibilityEnabled
import com.focusguard.utils.PermissionUtils.isDeviceAdminActive
import com.focusguard.utils.PermissionUtils.hasUsageStatsPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSelectApps: () -> Unit,
    onViewSchedules: () -> Unit,
    onSettingsClick: () -> Unit,
    onCustomDomains: () -> Unit,
    onDailyLimits: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val pinManager = remember { PinManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val allBlockedApps by db.blockedAppDao().getBlockedApps().collectAsState(initial = emptyList())
    val now = System.currentTimeMillis()
    val blockedApps = allBlockedApps.filter {
        it.isBlocked && (it.blockedUntil == 0L || it.blockedUntil > now)
    }
    val isBlockActive = blockedApps.isNotEmpty()

    // Permission states — re-checked every time screen is shown
    var accessibilityEnabled by remember { mutableStateOf(false) }
    var deviceAdminActive    by remember { mutableStateOf(false) }
    var usageStatsGranted    by remember { mutableStateOf(false) }

    // PIN unblock dialog state
    var appPendingUnblock by remember { mutableStateOf<BlockedApp?>(null) }

    // VPN intent state
    var vpnIntent by remember { mutableStateOf<Intent?>(null) }
    val vpnLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            com.focusguard.services.FocusVpnService.start(context)
            vpnIntent = null
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            accessibilityEnabled = isAccessibilityEnabled(context)
            deviceAdminActive    = isDeviceAdminActive(context)
            usageStatsGranted    = hasUsageStatsPermission(context)
            if (usageStatsGranted && accessibilityEnabled) {
                AppBlockerService.start(context)
            }
            val currentVpnIntent = android.net.VpnService.prepare(context)
            vpnIntent = currentVpnIntent
            if (currentVpnIntent == null) {
                com.focusguard.services.FocusVpnService.start(context)
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    // PIN dialog for unblocking
    appPendingUnblock?.let { appToUnblock ->
        UnblockPinDialog(
            appName = appToUnblock.appName,
            pinManager = pinManager,
            onConfirmed = {
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    db.blockedAppDao().insertOrUpdate(appToUnblock.copy(isBlocked = false, blockedUntil = 0L))
                }
                appPendingUnblock = null
            },
            onDismiss = { appPendingUnblock = null }
        )
    }

    Scaffold(
        topBar = {
            // Replaced default top app bar with a custom premium header inside the LazyColumn
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "AdMob Banner Placeholder",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Premium Hero Header ───────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .size(40.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "FocusGuard",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(
                                onClick = onSettingsClick, 
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha=0.2f), RoundedCornerShape(12.dp))
                                    .size(40.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isBlockActive) Icons.Default.Lock else Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (isBlockActive) "Protection Active" else "Ready to Focus",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (isBlockActive) "${blockedApps.size} apps are currently restricted" else "No active restrictions",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Permission Banners (Wrapped in padding) ───────────────────────
            if (!accessibilityEnabled || !deviceAdminActive || !usageStatsGranted || vpnIntent != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (!accessibilityEnabled) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Accessibility Service Disabled",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "App blocking won't work without it. Tap to enable.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                            Button(
                                onClick = {
                                    context.startActivity(
                                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) { Text("Enable", style = MaterialTheme.typography.labelMedium) }
                        }
                    }
                }
            }

            // ── Device Admin warning banner ────────────────────────────────────
            if (!deviceAdminActive) {
                item {
                    PermissionBanner(
                        title = "Uninstall Protection Disabled",
                        subtitle = "Without Device Admin, anyone can uninstall this app.",
                        buttonLabel = "Activate",
                        onClick = {
                            val comp = android.content.ComponentName(
                                context, com.focusguard.admin.DeviceAdminReceiver::class.java
                            )
                            val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp)
                                putExtra(android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    "Required to prevent unauthorized uninstallation of FocusGuard.")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // ── Usage Stats warning banner ─────────────────────────────────────
            if (!usageStatsGranted) {
                item {
                    PermissionBanner(
                        title = "Usage Access Not Granted",
                        subtitle = "App blocking won't detect foreground apps without it.",
                        buttonLabel = "Grant",
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        }
                    )
                }
            }

            // ── VPN warning banner ─────────────────────────────────────────────
            if (vpnIntent != null) {
                item {
                    PermissionBanner(
                        title = "Website Filtering Disabled",
                        subtitle = "Required to automatically block websites for blocked apps.",
                        buttonLabel = "Enable",
                        onClick = {
                            vpnLauncher.launch(vpnIntent!!)
                        }
                    )
                }
            }

            // ── Action Grid ───────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Block Apps",
                            icon = Icons.Default.Lock,
                            onClick = onSelectApps,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                        ActionCard(
                            title = "Websites",
                            icon = Icons.Default.Info, // Placeholder for globe
                            onClick = onCustomDomains,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Schedules",
                            icon = Icons.Default.Add, // Placeholder for clock
                            onClick = onViewSchedules,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                        ActionCard(
                            title = "Daily Limits",
                            icon = Icons.Default.Warning, // Placeholder for timer
                            onClick = onDailyLimits,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // ── Blocked apps list ─────────────────────────────────────────────
            if (blockedApps.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Active Restrictions (${blockedApps.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                items(blockedApps) { app ->
                    BlockedAppRow(
                        app = app,
                        onRequestUnblock = {
                            // Show PIN gate — don't unblock directly
                            appPendingUnblock = app
                        }
                    )
                }
            }


        }
    }
}

// ─── Premium Action Card Component ────────────────────────────────────────────
@Composable
fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

// ─── PIN dialog shown before unblocking ───────────────────────────────────────
@Composable
fun UnblockPinDialog(
    appName: String,
    pinManager: PinManager,
    onConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Parent PIN Required") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Enter your PIN to unblock $appName.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) { pin = it; errorMsg = null } },
                    label = { Text("PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorMsg != null,
                    supportingText = errorMsg?.let { msg -> { Text(msg, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (pinManager.verifyPin(pin)) {
                    onConfirmed()
                } else {
                    attempts++
                    pin = ""
                    errorMsg = if (attempts >= 5) "Too many attempts." else "Wrong PIN. ${5 - attempts} left."
                    if (attempts >= 5) onDismiss()
                }
            }) { Text("Unblock") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ─── Blocked app row ──────────────────────────────────────────────────────────
@Composable
fun BlockedAppRow(app: BlockedApp, onRequestUnblock: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appIcon = remember<androidx.compose.ui.graphics.ImageBitmap?>(app.packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(app.packageName)
            val bitmap = if (drawable is android.graphics.drawable.BitmapDrawable) {
                drawable.bitmap
            } else {
                val b = android.graphics.Bitmap.createBitmap(
                    if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1,
                    if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(b)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                b
            }
            bitmap.asImageBitmap()
        } catch (e: Exception) { null }
    }
    val now = System.currentTimeMillis()
    val timeLeft = if (app.blockedUntil > 0L) {
        val ms = app.blockedUntil - now
        when {
            ms <= 0 -> null
            ms < 60_000 -> "${ms / 1000}s left"
            ms < 3_600_000 -> "${ms / 60_000}m left"
            else -> {
                val h = ms / 3_600_000
                val m = (ms % 3_600_000) / 60_000
                if (m > 0) "${h}h ${m}m left" else "${h}h left"
            }
        }
    } else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appIcon != null) {
                androidx.compose.foundation.Image(
                    bitmap = appIcon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (timeLeft != null) "🔒 Blocked · $timeLeft" else "🔒 Blocked forever",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ─── Reusable permission banner ───────────────────────────────────────────────
@Composable
fun PermissionBanner(
    title: String,
    subtitle: String,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) { Text(buttonLabel, style = MaterialTheme.typography.labelMedium) }
        }
    }
}
