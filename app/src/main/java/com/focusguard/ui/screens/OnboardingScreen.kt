@file:OptIn(ExperimentalMaterial3Api::class)
package com.focusguard.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch

import com.focusguard.utils.PermissionUtils.isAccessibilityEnabled
import com.focusguard.utils.PermissionUtils.isDeviceAdminActive
import com.focusguard.utils.PermissionUtils.hasUsageStatsPermission

enum class PermissionType { NONE, USAGE, ACCESSIBILITY, ADMIN }

data class OnboardingPage(
    val title: String,
    val description: String,
    val instructions: List<String> = emptyList(),
    val permissionType: PermissionType = PermissionType.NONE,
    val icon: ImageVector? = null,
    val iconTint: Color? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinishOnboarding: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val manufacturer = android.os.Build.MANUFACTURER.lowercase()

    val accessibilityPath = when {
        manufacturer.contains("samsung") -> "Tap 'Installed apps' → Find FocusGuard"
        manufacturer.contains("xiaomi") || manufacturer.contains("poco") -> "Tap 'Downloaded apps' → Find FocusGuard"
        manufacturer.contains("oppo") || manufacturer.contains("vivo") || manufacturer.contains("realme") -> "Find FocusGuard under Accessibility"
        else -> "Find FocusGuard under Downloaded Apps or Installed Services"
    }

    val usagePath = when {
        manufacturer.contains("samsung") -> "Scroll down to FocusGuard"
        manufacturer.contains("xiaomi") || manufacturer.contains("poco") -> "Find FocusGuard in the list"
        else -> "Find FocusGuard in the list"
    }

    val baseAccessibilityInstructions = mutableListOf(accessibilityPath, "Turn the switch ON")
    if (android.os.Build.VERSION.SDK_INT >= 33) {
        baseAccessibilityInstructions.add(0, "If it says 'Restricted Setting', go back, tap ⋮ and choose 'Allow restricted settings'")
    }

    val pages = listOf(
        OnboardingPage(
            title = "Take Back Your Time",
            description = "Silence distractions and build healthier digital habits effortlessly.",
            icon = Icons.Default.Timer,
            iconTint = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = "Usage Access",
            description = "We need this to detect when a distracting app is opened so we can block it.",
            instructions = listOf(usagePath, "Toggle 'Permit usage access'"),
            permissionType = PermissionType.USAGE,
            icon = Icons.Default.CheckCircle,
            iconTint = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = "Accessibility Service",
            description = "Required to instantly draw the block screen over distracting apps.",
            instructions = baseAccessibilityInstructions,
            permissionType = PermissionType.ACCESSIBILITY,
            icon = Icons.Default.CheckCircle,
            iconTint = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            title = "Device Admin",
            description = "Prevents the app from being easily uninstalled while a block is active.",
            instructions = listOf("Tap 'Activate' on the system prompt that appears"),
            permissionType = PermissionType.ADMIN,
            icon = Icons.Default.Security,
            iconTint = MaterialTheme.colorScheme.error
        )
    )

    // ── Permission states ──────────────────────────────────────────────────────
    var accEnabled by remember { mutableStateOf(false) }
    var usageEnabled by remember { mutableStateOf(false) }
    var adminEnabled by remember { mutableStateOf(false) }

    /** Re-read all permissions from the system. Called on first composition AND
     *  every time the Activity resumes (i.e. after user returns from any system dialog). */
    fun refreshPermissions() {
        accEnabled = isAccessibilityEnabled(context)
        usageEnabled = hasUsageStatsPermission(context)
        adminEnabled = isDeviceAdminActive(context)
        android.util.Log.d("FocusOnboarding",
            "Permissions refresh → usage=$usageEnabled acc=$accEnabled admin=$adminEnabled")
    }

    // Initial check on first composition
    LaunchedEffect(Unit) { refreshPermissions() }

    // Re-check every time the Activity comes back to the foreground.
    // This is the MOST reliable way to detect permission changes after
    // returning from Settings / Device Admin dialog / Accessibility dialog.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Request notification permission on Android 13+ so our Device Admin PIN
    // notification can be shown when someone tries to deactivate the admin.
    val notifPermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* silently continue regardless of result */ }
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Small delay so the pager is fully visible first
            kotlinx.coroutines.delay(1500)
            notifPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // ── Pager ──────────────────────────────────────────────────────────────────
    val initialPage = remember {
        val u = hasUsageStatsPermission(context)
        val a = isAccessibilityEnabled(context)
        val d = isDeviceAdminActive(context)
        when {
            !u -> 1
            !a -> 2
            !d -> 3
            else -> 0
        }
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    // Launcher for Device Admin dialog (correct Compose API for startActivityForResult)
    val deviceAdminLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d("DeviceAdminFlow", "ActivityResult returned! ResultCode: ${result.resultCode}")
        
        // Explicitly update the state when the launcher returns so the UI immediately detects it
        val active = isDeviceAdminActive(context)
        android.util.Log.d("DeviceAdminFlow", "isDeviceAdminActive check post-return: $active")
        adminEnabled = active
    }

    // (Auto-advance removed to prevent page-clipping bugs. User will manually tap 'Next' when the button turns green.)

    // ── UI ─────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                pageSize = androidx.compose.foundation.pager.PageSize.Fill,
                beyondBoundsPageCount = 0,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { position ->
                val page = pages[position]
                val isGranted = when (page.permissionType) {
                    PermissionType.USAGE -> usageEnabled
                    PermissionType.ACCESSIBILITY -> accEnabled
                    PermissionType.ADMIN -> adminEnabled
                    PermissionType.NONE -> true
                }
                PagerScreen(
                    modifier = Modifier.fillMaxSize(),
                    onBoardingPage = page,
                    isGranted = isGranted,
                    onGrantClick = {
                        when (page.permissionType) {
                            PermissionType.USAGE -> context.startActivity(
                                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                    .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            )
                            PermissionType.ACCESSIBILITY -> context.startActivity(
                                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            )
                            PermissionType.ADMIN -> {
                                val comp = android.content.ComponentName("com.focusguard", "com.focusguard.admin.DeviceAdminReceiver")
                                android.util.Log.d("DeviceAdminFlow", "Button Clicked! Launching Intent for Component: ${comp.flattenToString()}")
                                val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                    putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp)
                                    putExtra(
                                        android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                        "FocusGuard uses Device Admin to prevent the app from being uninstalled during a focus block."
                                    )
                                }
                                deviceAdminLauncher.launch(intent)
                            }
                            else -> {}
                        }
                    }
                )
            }

            // ── Bottom navigation bar ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dot indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pages.forEachIndexed { index, _ ->
                            val isSelected = index == pagerState.currentPage
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 8.dp,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "dot"
                            )
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(width)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    val currentPage = pages[pagerState.currentPage]
                    val canProceed = when (currentPage.permissionType) {
                        PermissionType.USAGE -> usageEnabled
                        PermissionType.ACCESSIBILITY -> accEnabled
                        PermissionType.ADMIN -> adminEnabled
                        PermissionType.NONE -> true
                    }

                    if (pagerState.currentPage != pages.size - 1) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            enabled = canProceed,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                if (canProceed) "Next" else "Required",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (canProceed) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                val prefs = context.getSharedPreferences("FocusGuardPrefs", Context.MODE_PRIVATE)
                                prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                                onFinishOnboarding()
                            },
                            enabled = canProceed,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                        ) {
                            Text(
                                if (canProceed) "Start" else "Required",
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (canProceed) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    onBoardingPage: OnboardingPage,
    isGranted: Boolean = true,
    onGrantClick: () -> Unit = {}
) {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon circle
        if (onBoardingPage.icon != null && onBoardingPage.iconTint != null) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(onBoardingPage.iconTint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = onBoardingPage.icon,
                    contentDescription = null,
                    tint = onBoardingPage.iconTint,
                    modifier = Modifier.size(72.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Text(
            text = onBoardingPage.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = onBoardingPage.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (onBoardingPage.instructions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "How to set up:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    onBoardingPage.instructions.forEachIndexed { index, instruction ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = instruction,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (onBoardingPage.permissionType != PermissionType.NONE) {
            if (isGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Permission Granted!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Button(
                    onClick = onGrantClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        "Grant Permission",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
