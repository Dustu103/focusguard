package com.focusguard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.content.Context
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusguard.data.AppDatabase
import com.focusguard.data.BlockProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    onNavigateBack: () -> Unit,
    onEditProfileApps: (Int) -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val profiles by db.blockProfileDao().getAllProfiles().collectAsState(initial = emptyList())
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var showTimerDialog by remember { mutableStateOf<BlockProfile?>(null) }
    val prefs = context.getSharedPreferences("FocusGuardPrefs", Context.MODE_PRIVATE)
    val appMode = prefs.getString("app_mode", "focus")
    
    if (showTimerDialog != null) {
        BlockDurationDialog(
            appName = showTimerDialog!!.name,
            onDismiss = { showTimerDialog = null },
            onConfirm = { minutes ->
                val profileIdToUpdate = showTimerDialog?.id
                if (profileIdToUpdate != null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        val activeUntil = if (minutes > 0) System.currentTimeMillis() + minutes * 60_000L else 0L
                        db.blockProfileDao().updateProfileStatus(profileIdToUpdate, true, activeUntil)
                    }
                }
                showTimerDialog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Lists", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
                text = { Text("Create List") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (profiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No lists created yet. Tap + to create one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(profiles) { profile ->
                        ProfileCardItem(
                            profile = profile,
                            db = db,
                            appMode = appMode,
                            onEditApps = { onEditProfileApps(profile.id) },
                            onDelete = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    db.blockProfileDao().deleteProfile(profile.id)
                                    db.blockProfileDao().deleteAppsForProfile(profile.id)
                                }
                            },
                            onRequestTimer = { showTimerDialog = profile }
                        )
                    }
                }
            }
        }
        
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New List") },
                text = {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("List Name (e.g. Study Mode)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val nameToSave = newProfileName.trim()
                            if (nameToSave.isNotBlank()) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    db.blockProfileDao().insertProfile(BlockProfile(name = nameToSave))
                                }
                                newProfileName = ""
                                showCreateDialog = false
                            }
                        }
                    ) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun ProfileCardItem(
    profile: BlockProfile,
    db: AppDatabase,
    appMode: String?,
    onEditApps: () -> Unit,
    onDelete: () -> Unit,
    onRequestTimer: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val apps by db.blockProfileDao().getAppsForProfile(profile.id).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val pm = context.packageManager
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditApps() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name.ifBlank { "Unnamed List" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Tap to add or remove apps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f))
                }
                
                val isProfileActive = profile.isActive && (profile.activeUntil == 0L || profile.activeUntil > System.currentTimeMillis())
                Switch(
                    checked = isProfileActive,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            if (appMode == "parental") {
                                coroutineScope.launch(Dispatchers.IO) {
                                    db.blockProfileDao().updateProfileStatus(profile.id, true, 0L)
                                }
                            } else {
                                onRequestTimer()
                            }
                        } else {
                            coroutineScope.launch(Dispatchers.IO) {
                                db.blockProfileDao().updateProfileStatus(profile.id, false, 0L)
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete List", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            if (apps.isNotEmpty()) {
                val uniqueApps = apps.distinctBy { it.packageName }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    uniqueApps.take(6).forEach { app ->
                        val icon = remember(app.packageName) {
                            try { pm.getApplicationIcon(app.packageName) } catch(e: Exception) { null }
                        }
                        if (icon != null) {
                            Image(
                                bitmap = icon.toBitmap(96, 96).asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha=0.2f), shape = CircleShape))
                        }
                    }
                    if (uniqueApps.size > 6) {
                        Box(
                            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.2f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+${uniqueApps.size - 6}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
