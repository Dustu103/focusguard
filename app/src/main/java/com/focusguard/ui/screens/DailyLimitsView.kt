package com.focusguard.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusguard.data.AppDatabase
import com.focusguard.data.UsageLimit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLimitsView(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var limits by remember { mutableStateOf<List<UsageLimit>>(emptyList()) }
    val pm = context.packageManager

    // Load limits
    LaunchedEffect(Unit) {
        db.usageLimitDao().getAllLimits().collect { newLimits ->
            limits = newLimits
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily App Limits") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Limit")
            }
        }
    ) { padding ->
        if (limits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "No daily limits set.\nTap + to restrict app usage.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(limits) { limit ->
                    UsageLimitRow(
                        limit = limit,
                        pm = pm,
                        onDelete = {
                            scope.launch { db.usageLimitDao().deleteLimit(limit.id) }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddLimitDialog(
            onDismiss = { showAddDialog = false },
            onLimitAdded = { packageName, minutes ->
                scope.launch {
                    val limit = UsageLimit(
                        target = packageName,
                        targetType = "app",
                        limitMinutes = minutes
                    )
                    db.usageLimitDao().insertOrUpdate(limit)
                    showAddDialog = false
                }
            },
            pm = pm
        )
    }
}

@Composable
fun UsageLimitRow(limit: UsageLimit, pm: PackageManager, onDelete: () -> Unit) {
    val appName = remember(limit.target) {
        try {
            val ai = pm.getApplicationInfo(limit.target, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: Exception) {
            limit.target
        }
    }

    val appIcon = remember<androidx.compose.ui.graphics.ImageBitmap?>(limit.target) {
        try {
            val drawable = pm.getApplicationIcon(limit.target)
            val b = if (drawable is android.graphics.drawable.BitmapDrawable) {
                drawable.bitmap
            } else {
                val btmp = android.graphics.Bitmap.createBitmap(
                    if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1,
                    if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(btmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                btmp
            }
            b.asImageBitmap()
        } catch (e: Exception) { null }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (appIcon != null) {
                Image(bitmap = appIcon, contentDescription = null, modifier = Modifier.size(40.dp))
            } else {
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(appName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Limit: ${limit.limitMinutes / 60}h ${limit.limitMinutes % 60}m per day", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddLimitDialog(onDismiss: () -> Unit, onLimitAdded: (String, Int) -> Unit, pm: PackageManager) {
    var installedApps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
    var selectedPackage by remember { mutableStateOf<String?>(null) }
    var hours by remember { mutableStateOf("1") }
    var minutes by remember { mutableStateOf("0") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            installedApps = apps.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName.contains("youtube") }
                .sortedBy { pm.getApplicationLabel(it).toString().lowercase() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Limit") },
        text = {
            if (installedApps.isEmpty()) {
                CircularProgressIndicator()
            } else {
                Column(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    if (selectedPackage == null) {
                        Text("Select an app:", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn {
                            items(installedApps) { app ->
                                val appName = pm.getApplicationLabel(app).toString()
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { selectedPackage = app.packageName }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(appName, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        val appName = installedApps.find { it.packageName == selectedPackage }?.let { pm.getApplicationLabel(it).toString() } ?: selectedPackage!!
                        Text("Set limit for $appName:", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = hours,
                                onValueChange = { hours = it },
                                label = { Text("Hours") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = minutes,
                                onValueChange = { minutes = it },
                                label = { Text("Minutes") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { selectedPackage = null }) { Text("Change App") }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedPackage != null) {
                Button(onClick = {
                    val h = hours.toIntOrNull() ?: 0
                    val m = minutes.toIntOrNull() ?: 0
                    val totalMinutes = (h * 60) + m
                    if (totalMinutes > 0) {
                        onLimitAdded(selectedPackage!!, totalMinutes)
                    }
                }) { Text("Save") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
