package com.focusguard.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.focusguard.data.AppDatabase
import com.focusguard.data.PinManager
import com.focusguard.data.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─── Schedule type ────────────────────────────────────────────────────────────
private enum class ScheduleType { APP, URL }

// ─── Screen ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val scheduleManager = remember { com.focusguard.services.ScheduleManager(context) }

    val schedules by db.scheduleDao().getAllSchedules().collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }

    // Keep Android AlarmManager perfectly in sync with the database rules
    LaunchedEffect(schedules) {
        scheduleManager.scheduleAll(schedules)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Schedules", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Schedule", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp)
            )
        }
    ) { padding ->
        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📅", style = MaterialTheme.typography.displayMedium)
                    }
                    Text("No schedules yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        "Tap \"New Schedule\" to automatically block apps\nor websites at specific times.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(schedules, key = { it.id }) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        onToggle = {
                            coroutineScope.launch(Dispatchers.IO) {
                                db.scheduleDao().insertOrUpdate(schedule.copy(isActive = !schedule.isActive))
                            }
                        },
                        onDelete = {
                            coroutineScope.launch(Dispatchers.IO) {
                                db.scheduleDao().delete(schedule.id)
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showCreateDialog) {
        CreateScheduleDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { schedule ->
                coroutineScope.launch(Dispatchers.IO) {
                    db.scheduleDao().insertOrUpdate(schedule)
                }
                showCreateDialog = false
            }
        )
    }
}

// ─── Schedule card ─────────────────────────────────────────────────────────────
@Composable
fun ScheduleCard(schedule: Schedule, onToggle: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }
    val days = parseDays(schedule.daysOfWeek)
    val isApp = schedule.scheduleType == "APP"
    var showPinDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf("") } // "toggle" or "delete"

    if (showPinDialog) {
        UnblockPinDialog(
            appName = "Schedule for " + (if (isApp) schedule.targetLabel else schedule.targetUrl),
            pinManager = pinManager,
            onConfirmed = {
                showPinDialog = false
                if (pendingAction == "toggle") onToggle() else onDelete()
            },
            onDismiss = { showPinDialog = false }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (schedule.isActive) 4.dp else 0.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (schedule.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Type icon badge
                    Surface(
                        shape = CircleShape,
                        color = if (isApp)
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isApp) Icons.Default.PhoneAndroid else Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (isApp) MaterialTheme.colorScheme.secondary
                                       else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(16.dp))

                    Column {
                        // Target label
                        Text(
                            text = when {
                                isApp && schedule.targetLabel.isNotEmpty() -> schedule.targetLabel
                                isApp -> schedule.packageName.ifEmpty { "All apps" }
                                schedule.targetUrl.isNotEmpty() -> schedule.targetUrl
                                else -> "Custom website"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(4.dp))
                        // Time range
                        Text(
                            text = "%02d:%02d – %02d:%02d".format(
                                schedule.startHour, schedule.startMinute,
                                schedule.endHour, schedule.endMinute
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Switch(
                    checked = schedule.isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = days,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                TextButton(
                    onClick = { pendingAction = "delete"; showPinDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Create dialog — multi-step ───────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleDialog(onDismiss: () -> Unit, onSave: (Schedule) -> Unit) {
    // Step 0 = type selection, Step 1 = details
    var step by remember { mutableStateOf(0) }
    var scheduleType by remember { mutableStateOf(ScheduleType.APP) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "step"
            ) { currentStep ->
                when (currentStep) {
                    0 -> TypeSelectionStep(
                        onSelected = { type ->
                            scheduleType = type
                            step = 1
                        },
                        onDismiss = onDismiss
                    )
                    else -> DetailsStep(
                        scheduleType = scheduleType,
                        onBack = { step = 0 },
                        onDismiss = onDismiss,
                        onSave = onSave
                    )
                }
            }
        }
    }
}

// ─── Step 0: Type picker ──────────────────────────────────────────────────────
@Composable
private fun TypeSelectionStep(
    onSelected: (ScheduleType) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create Schedule", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "What do you want to block?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // App option card
        TypeOptionCard(
            icon = Icons.Default.PhoneAndroid,
            title = "Block an App",
            subtitle = "Choose an installed app to block during this schedule",
            tint = MaterialTheme.colorScheme.secondary,
            onClick = { onSelected(ScheduleType.APP) }
        )

        // URL option card
        TypeOptionCard(
            icon = Icons.Default.Language,
            title = "Block a Website",
            subtitle = "Enter a domain or URL to block during this schedule",
            tint = MaterialTheme.colorScheme.tertiary,
            onClick = { onSelected(ScheduleType.URL) }
        )

        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Text("Cancel")
        }
    }
}

@Composable
private fun TypeOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = tint.copy(alpha = 0.08f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = tint.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─── Step 1: Details (time + days + target) ────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsStep(
    scheduleType: ScheduleType,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val dayKeys   = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    val dayLabels = listOf("M",   "T",   "W",   "T",   "F",   "S",   "S")

    var startHour   by remember { mutableStateOf(8)  }
    var startMinute by remember { mutableStateOf(0)  }
    var endHour     by remember { mutableStateOf(17) }
    var endMinute   by remember { mutableStateOf(0)  }
    var selectedDays by remember { mutableStateOf(setOf("MON","TUE","WED","THU","FRI")) }

    // App-type state
    var appQuery     by remember { mutableStateOf("") }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }
    var selectedApp  by remember { mutableStateOf<AppInfo?>(null) }

    // URL-type state
    var urlInput by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }

    // Load installed apps once for APP type
    if (scheduleType == ScheduleType.APP) {
        LaunchedEffect(Unit) {
            isLoadingApps = true
            withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val pkgs = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
                installedApps = pkgs
                    .filter { pm.getLaunchIntentForPackage(it.packageName) != null && it.packageName != context.packageName }
                    .map {
                        val name = try { it.loadLabel(pm).toString() } catch (e: Exception) { it.packageName }
                        val icon = try { it.loadIcon(pm) } catch (e: Exception) { null }
                        AppInfo(it.packageName, name, icon, false)
                    }
                    .sortedBy { it.appName.lowercase() }
            }
            isLoadingApps = false
        }
    }

    val filteredApps = remember(installedApps, appQuery) {
        if (appQuery.isBlank()) installedApps
        else installedApps.filter { it.appName.contains(appQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .heightIn(max = 600.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (scheduleType == ScheduleType.APP) "Block an App" else "Block a Website",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        // ── Target section ────────────────────────────────────────────────────
        if (scheduleType == ScheduleType.APP) {
            Text("Select app", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
                 color = MaterialTheme.colorScheme.primary)

            // Selected app chip
            if (selectedApp != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        selectedApp!!.icon?.let { drawable ->
                            val bmp = remember(selectedApp!!.packageName) { drawable.toBitmap(64,64).asImageBitmap() }
                            Image(bmp, null, modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)))
                        }
                        Text(selectedApp!!.appName, fontWeight = FontWeight.SemiBold,
                             color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.weight(1f))
                        TextButton(onClick = { selectedApp = null }, contentPadding = PaddingValues(0.dp)) {
                            Text("Change", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else {
                // Search + list
                OutlinedTextField(
                    value = appQuery,
                    onValueChange = { appQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search apps…") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 180.dp)
                ) {
                    if (isLoadingApps) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(28.dp))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(filteredApps, key = { it.packageName }) { app ->
                                AppPickerRow(app = app, onClick = { selectedApp = app; appQuery = "" })
                            }
                        }
                    }
                }
            }
        } else {
            // URL input
            Text("Website / Domain", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
                 color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("e.g. instagram.com") },
                leadingIcon = { Icon(Icons.Default.Language, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        HorizontalDivider()

        // ── Time pickers ──────────────────────────────────────────────────────
        Text("Time range", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
             color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("Start", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                TimeStepperRow(hour = startHour, minute = startMinute,
                    onHourChange = { startHour = it }, onMinuteChange = { startMinute = it })
            }
            Text("→", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("End", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                TimeStepperRow(hour = endHour, minute = endMinute,
                    onHourChange = { endHour = it }, onMinuteChange = { endMinute = it })
            }
        }

        // ── Day selector ──────────────────────────────────────────────────────
        Text("Days", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
             color = MaterialTheme.colorScheme.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            dayKeys.forEachIndexed { i, key ->
                val isSelected = key in selectedDays
                FilterChip(
                    selected = isSelected,
                    onClick  = { selectedDays = if (isSelected) selectedDays - key else selectedDays + key },
                    label    = { Text(dayLabels[i], fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(8.dp)
                )
            }
        }

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
        }

        // ── Actions ────────────────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    error = null
                    if (selectedDays.isEmpty()) { error = "Select at least one day"; return@Button }
                    if (startHour > endHour || (startHour == endHour && startMinute >= endMinute)) {
                        error = "End time must be after start time"; return@Button
                    }
                    if (scheduleType == ScheduleType.APP && selectedApp == null) {
                        error = "Please select an app"; return@Button
                    }
                    if (scheduleType == ScheduleType.URL && urlInput.isBlank()) {
                        error = "Please enter a website or domain"; return@Button
                    }
                    val daysString = dayKeys.filter { it in selectedDays }.joinToString(",")
                    onSave(
                        Schedule(
                            scheduleType = scheduleType.name,
                            packageName  = if (scheduleType == ScheduleType.APP) selectedApp!!.packageName else "",
                            targetLabel  = if (scheduleType == ScheduleType.APP) selectedApp!!.appName else urlInput.trim(),
                            targetUrl    = if (scheduleType == ScheduleType.URL) urlInput.trim() else "",
                            startHour    = startHour,
                            startMinute  = startMinute,
                            endHour      = endHour,
                            endMinute    = endMinute,
                            daysOfWeek   = daysString,
                            isActive     = true
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Save") }
        }
    }
}

// ─── Small app row for the picker ────────────────────────────────────────────
@Composable
private fun AppPickerRow(app: AppInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (app.icon != null) {
            val bmp = remember(app.packageName) { app.icon.toBitmap(64,64).asImageBitmap() }
            Image(bmp, null, modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)))
        } else {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(app.appName.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Text(app.appName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1)
    }
}

// ─── HH:MM stepper row ────────────────────────────────────────────────────────
@Composable
private fun TimeStepperRow(
    hour: Int, minute: Int,
    onHourChange: (Int) -> Unit, onMinuteChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        SmallStepper(value = hour,   range = 0..23, onValueChange = onHourChange)
        Text(":", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        SmallStepper(value = minute, range = 0..59, step = 5, onValueChange = onMinuteChange)
    }
}

@Composable
private fun SmallStepper(value: Int, range: IntRange, step: Int = 1, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = { val n = value + step; if (n <= range.last) onValueChange(n) else onValueChange(range.first) },
            modifier = Modifier.size(28.dp), shape = CircleShape
        ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp)) }

        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.width(40.dp)) {
            Text(
                text = "%02d".format(value),
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        FilledTonalIconButton(
            onClick = { val n = value - step; if (n >= range.first) onValueChange(n) else onValueChange(range.last) },
            modifier = Modifier.size(28.dp), shape = CircleShape
        ) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(14.dp)) }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
private fun parseDays(daysOfWeek: String): String {
    val names = mapOf(
        "MON" to "Mon","TUE" to "Tue","WED" to "Wed",
        "THU" to "Thu","FRI" to "Fri","SAT" to "Sat","SUN" to "Sun"
    )
    val selected = daysOfWeek.split(",").mapNotNull { names[it.trim()] }
    return if (selected.size == 7) "Every day" else selected.joinToString(", ")
}
