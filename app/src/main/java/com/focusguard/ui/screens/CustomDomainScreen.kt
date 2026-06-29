package com.focusguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.VpnService
import com.focusguard.services.FocusVpnService
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.focusguard.data.AppDatabase
import com.focusguard.data.BlockedDomain
import com.focusguard.data.PinManager
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Language
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDomainScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val pinManager = remember { PinManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var domainInput by remember { mutableStateOf("") }
    var domainToDelete by remember { mutableStateOf<String?>(null) }

    val vpnLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            FocusVpnService.start(context)
        }
    }

    fun restartVpn() {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            vpnLauncher.launch(intent)
        } else {
            FocusVpnService.start(context)
        }
    }

    if (domainToDelete != null) {
        UnblockPinDialog(
            appName = domainToDelete!!,
            pinManager = pinManager,
            onConfirmed = {
                val d = domainToDelete!!
                domainToDelete = null
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    db.blockedDomainDao().deleteDomain(d)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { restartVpn() }
                }
            },
            onDismiss = { domainToDelete = null }
        )
    }
    
    val domains by db.blockedDomainDao().getBlockedDomains().collectAsState(initial = emptyList())

    // Attempt to start VPN when entering screen if there are domains
    LaunchedEffect(domains.size) {
        if (domains.isNotEmpty()) {
            restartVpn()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Block Custom Websites", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Enter a website like 'facebook.com' to block it entirely across all your browsers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            
            // Premium Input Field
            OutlinedTextField(
                value = domainInput,
                onValueChange = { domainInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("example.com", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f)) },
                singleLine = true,
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                trailingIcon = {
                    Button(
                        onClick = {
                            if (domainInput.isNotBlank()) {
                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    db.blockedDomainDao().insertDomain(BlockedDomain(domainInput.trim().lowercase()))
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { restartVpn() }
                                }
                                domainInput = ""
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp).height(40.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Add", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            )
            
            Spacer(Modifier.height(32.dp))
            Text("Blocked Websites", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(domains) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha=0.15f), androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = item.domain, 
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { domainToDelete = item.domain },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
