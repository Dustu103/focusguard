package com.focusguard.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.focusguard.data.BlockedApp
import com.focusguard.data.AppDatabase
import com.focusguard.data.PinManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isBlocked: Boolean,
    val blockedUntil: Long = 0L
)

// ─── Quick-select preset (duration in minutes, 0 = forever) ─────────────────
private data class DurationPreset(val label: String, val minutes: Long)

private val QUICK_PRESETS = listOf(
    DurationPreset("15 min", 15L),
    DurationPreset("30 min", 30L),
    DurationPreset("1 hr",   60L),
    DurationPreset("2 hr",  120L),
)

// ─── Screen ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var allApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
            val savedApps = db.blockedAppDao().getAllApps().associateBy { it.packageName }

            val apps = packages
                .filter {
                    it.packageName != context.packageName &&
                    pm.getLaunchIntentForPackage(it.packageName) != null
                }
                .map {
                    val pkg  = it.packageName
                    val name = try { it.loadLabel(pm).toString() } catch (e: Exception) { pkg }
                    val icon = try { it.loadIcon(pm) } catch (e: Exception) { null }
                    val saved = savedApps[pkg]
                    AppInfo(
                        packageName = pkg,
                        appName     = name,
                        icon        = icon,
                        isBlocked   = saved?.isBlocked ?: false,
                        blockedUntil = saved?.blockedUntil ?: 0L
                    )
                }
                .sortedWith(compareByDescending<AppInfo> { it.isBlocked }.thenBy { it.appName.lowercase() })

            allApps = apps
            isLoading = false
        }
    }

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }

    val blockedCount = allApps.count { it.isBlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Select Apps to Block", fontWeight = FontWeight.Bold)
                        if (!isLoading) {
                            Text(
                                "$blockedCount blocked · ${allApps.size} total",
                                style = MaterialTheme.typography.labelSmall,
                                color  = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            // Premium Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                placeholder = { Text("Search apps…", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f)) },
                leadingIcon  = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                singleLine   = true,
                shape        = CircleShape,
                colors       = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Loading your apps…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                filteredApps.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No apps found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        AppSelectionRow(
                            app      = app,
                            onToggle = { isBlocked, minutes ->
                                allApps = allApps.map { a ->
                                    if (a.packageName == app.packageName) a.copy(isBlocked = isBlocked) else a
                                }
                                coroutineScope.launch(Dispatchers.IO) {
                                    val blockedUntil = if (isBlocked && minutes > 0)
                                        System.currentTimeMillis() + minutes * 60_000L else 0L
                                    db.blockedAppDao().insertOrUpdate(
                                        BlockedApp(
                                            packageName  = app.packageName,
                                            appName      = app.appName,
                                            isBlocked    = isBlocked,
                                            blockedUntil = blockedUntil
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Row item ─────────────────────────────────────────────────────────────────
@Composable
fun AppSelectionRow(app: AppInfo, onToggle: (Boolean, Long) -> Unit) {
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }
    var isChecked  by remember(app.packageName, app.isBlocked) { mutableStateOf(app.isBlocked) }
    var showDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    if (showPinDialog) {
        UnblockPinDialog(
            appName = app.appName,
            pinManager = pinManager,
            onConfirmed = {
                showPinDialog = false
                isChecked = false
                onToggle(false, 0L)
            },
            onDismiss = { showPinDialog = false }
        )
    }

    val cardColor by animateColorAsState(
        targetValue    = if (isChecked)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label          = "cardColor"
    )

    if (showDialog) {
        BlockDurationDialog(
            appName   = app.appName,
            onDismiss = { showDialog = false; isChecked = false },
            onConfirm = { minutes ->
                showDialog = false
                onToggle(true, minutes)
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // App icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (app.icon != null) {
                    val bitmap = remember(app.packageName) { app.icon.toBitmap(96, 96).asImageBitmap() }
                    Image(bitmap = bitmap, contentDescription = app.appName, modifier = Modifier.size(40.dp))
                } else {
                    Text(
                        text       = app.appName.take(1).uppercase(),
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = app.appName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines   = 1
                )
                Text(
                    text     = if (isChecked) "Restricted" else "Allowed",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = if (isChecked) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f),
                    maxLines = 1
                )
            }

            val isStrictLocked = isChecked && app.blockedUntil > System.currentTimeMillis()
            
            // Premium Action Button
            Button(
                onClick = {
                    if (isStrictLocked && isChecked) {
                        android.widget.Toast.makeText(
                            context, 
                            "Strict Mode: Timer is still active!", 
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (!isChecked) {
                        isChecked = true
                        showDialog = true
                    } else {
                        showPinDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                    contentColor = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = if (isChecked) "Unblock" else "Block",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Duration picker dialog ───────────────────────────────────────────────────
@Composable
fun BlockDurationDialog(
    appName   : String,
    onDismiss : () -> Unit,
    onConfirm : (Long) -> Unit          // minutes, 0 = forever
) {
    // Selected quick preset (null = custom)
    var selectedPreset by remember { mutableStateOf<DurationPreset?>(QUICK_PRESETS[2]) } // default 1 hr
    // Custom picker state
    var customHours by remember { mutableStateOf(0) }
    var customMins  by remember { mutableStateOf(0)  }

    val effectiveMinutes: Long = when {
        selectedPreset != null -> selectedPreset!!.minutes
        else -> customHours * 60L + customMins
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape  = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color  = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Title ────────────────────────────────────────────────────
                Text(
                    text       = "Block Duration",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = "How long to block $appName?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                // ── Quick presets ─────────────────────────────────────────────
                Text(
                    text       = "Quick select",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QUICK_PRESETS.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick  = { selectedPreset = preset },
                            label    = {
                                Text(
                                    text      = preset.label,
                                    fontSize  = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor     = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor         = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                HorizontalDivider()

                // ── Custom time picker ────────────────────────────────────────
                Text(
                    text       = "Custom duration",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (selectedPreset == null) MaterialTheme.colorScheme.primary
                                 else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Hours stepper
                    DurationStepper(
                        label    = "Hours",
                        value    = customHours,
                        range    = 0..23,
                        onValueChange = { customHours = it; selectedPreset = null },
                        modifier = Modifier.weight(1f)
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium,
                         color = MaterialTheme.colorScheme.onSurface)
                    // Minutes stepper
                    DurationStepper(
                        label    = "Minutes",
                        value    = customMins,
                        range    = 0..59,
                        step     = 5,
                        onValueChange = { customMins = it; selectedPreset = null },
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Summary label ─────────────────────────────────────────────
                val summaryText = when {
                    effectiveMinutes <= 0L -> "Block forever"
                    effectiveMinutes < 60L -> "$effectiveMinutes min"
                    effectiveMinutes % 60 == 0L -> "${effectiveMinutes / 60} hr"
                    else -> "${effectiveMinutes / 60} hr ${effectiveMinutes % 60} min"
                }
                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text      = "Will block for: $summaryText",
                        modifier  = Modifier.padding(12.dp),
                        style     = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color     = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }

                // ── Action buttons ────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp)
                    ) { Text("Cancel") }

                    OutlinedButton(
                        onClick  = { onConfirm(0L) },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp)
                    ) { Text("Forever") }

                    Button(
                        onClick  = { onConfirm(effectiveMinutes) },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = effectiveMinutes > 0L || selectedPreset == null
                    ) { Text("Block") }
                }
            }
        }
    }
}

// ─── +/- Stepper ──────────────────────────────────────────────────────────────
@Composable
fun DurationStepper(
    label        : String,
    value        : Int,
    range        : IntRange,
    step         : Int = 1,
    onValueChange: (Int) -> Unit,
    modifier     : Modifier = Modifier
) {
    Column(
        modifier             = modifier,
        horizontalAlignment  = Alignment.CenterHorizontally,
        verticalArrangement  = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Decrement
            FilledTonalIconButton(
                onClick  = { if (value - step >= range.first) onValueChange(value - step) },
                modifier = Modifier.size(36.dp),
                shape    = CircleShape,
                enabled  = value - step >= range.first
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease $label", modifier = Modifier.size(18.dp))
            }

            // Value display
            Surface(
                shape  = RoundedCornerShape(8.dp),
                color  = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.width(52.dp)
            ) {
                Text(
                    text      = "%02d".format(value),
                    modifier  = Modifier.padding(vertical = 8.dp),
                    style     = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSurface
                )
            }

            // Increment
            FilledTonalIconButton(
                onClick  = { if (value + step <= range.last) onValueChange(value + step) },
                modifier = Modifier.size(36.dp),
                shape    = CircleShape,
                enabled  = value + step <= range.last
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase $label", modifier = Modifier.size(18.dp))
            }
        }
    }
}
