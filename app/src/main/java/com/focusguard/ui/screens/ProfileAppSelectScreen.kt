package com.focusguard.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.focusguard.data.AppDatabase
import com.focusguard.data.BlockProfileApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isInProfile: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAppSelectScreen(profileId: Int, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var allApps by remember { mutableStateOf<List<ProfileAppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var profileName by remember { mutableStateOf("List") }

    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }

    LaunchedEffect(profileId) {
        withContext(Dispatchers.IO) {
            val profile = db.blockProfileDao().getProfile(profileId)
            if (profile != null) {
                profileName = profile.name
            }
            
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
            
            db.blockProfileDao().getAppsForProfile(profileId).collect { savedApps ->
                val savedPackages = savedApps.map { it.packageName }.toSet()
                val apps = packages
                    .filter {
                        it.packageName != context.packageName &&
                        pm.getLaunchIntentForPackage(it.packageName) != null
                    }
                    .map {
                        val pkg  = it.packageName
                        val name = try { it.loadLabel(pm).toString() } catch (e: Exception) { pkg }
                        val icon = try { it.loadIcon(pm) } catch (e: Exception) { null }
                        val saved = savedPackages.contains(pkg)
                        ProfileAppInfo(
                            packageName = pkg,
                            appName     = name,
                            icon        = icon,
                            isInProfile = saved
                        )
                    }
                    .sortedWith(compareByDescending<ProfileAppInfo> { it.isInProfile }.thenBy { it.appName.lowercase() })
                
                withContext(Dispatchers.Main) {
                    allApps = apps
                    isLoading = false
                }
            }
        }
    }

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }

    val selectedCount = allApps.count { it.isInProfile }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(profileName, fontWeight = FontWeight.Bold)
                        if (!isLoading) {
                            Text(
                                "$selectedCount apps in list",
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
                    CircularProgressIndicator()
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
                        ProfileAppSelectionRow(
                            app = app,
                            onToggle = { isSelected ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    if (isSelected) {
                                        db.blockProfileDao().deleteProfileApp(profileId, app.packageName)
                                        db.blockProfileDao().insertProfileApp(BlockProfileApp(profileId = profileId, packageName = app.packageName))
                                    } else {
                                        db.blockProfileDao().deleteProfileApp(profileId, app.packageName)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAppSelectionRow(app: ProfileAppInfo, onToggle: (Boolean) -> Unit) {
    var isToggling by remember { mutableStateOf(false) }

    // Reset toggling state when the DB updates the app.isInProfile
    LaunchedEffect(app.isInProfile) {
        isToggling = false
    }

    val cardColor by animateColorAsState(
        targetValue = if (app.isInProfile) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "cardColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        text = app.appName.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
            if (isToggling) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Switch(
                    checked = app.isInProfile,
                    onCheckedChange = { 
                        isToggling = true
                        onToggle(it) 
                    }
                )
            }
        }
    }
}
