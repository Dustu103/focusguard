package com.focusguard

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.focusguard.data.PinManager
import com.focusguard.ui.screens.*
import com.focusguard.ui.theme.FocusGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pinManager = PinManager(this)

        setContent {
            FocusGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {

                        composable("splash") {
                            SplashScreen(onSplashFinished = {
                                val prefs = getSharedPreferences("FocusGuardPrefs", Context.MODE_PRIVATE)
                                val hasModeSelected  = prefs.getBoolean("has_selected_mode", false)
                                val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)

                                val u = com.focusguard.utils.PermissionUtils.hasUsageStatsPermission(this@MainActivity)
                                val a = com.focusguard.utils.PermissionUtils.isAccessibilityEnabled(this@MainActivity)
                                val d = com.focusguard.utils.PermissionUtils.isDeviceAdminActive(this@MainActivity)
                                val allPermsGranted = u && a && d

                                val destination = when {
                                    !hasSeenOnboarding && !allPermsGranted -> "onboarding"
                                    else -> "mode_select"
                                }
                                navController.navigate(destination) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }

                        composable("mode_select") {
                            ModeSelectionScreen(
                                onFocusModeSelected = {
                                    // Mark mode as selected and go to PIN setup (or dashboard)
                                    val prefs = getSharedPreferences("FocusGuardPrefs", Context.MODE_PRIVATE)
                                    prefs.edit()
                                        .putBoolean("has_selected_mode", true)
                                        .putString("app_mode", "focus")
                                        .apply()
                                    
                                    val dest = if (pinManager.isPinSet()) "dashboard" else "setup_pin"
                                    navController.navigate(dest)
                                },
                                onParentalModeSelected = {
                                    val prefs = getSharedPreferences("FocusGuardPrefs", Context.MODE_PRIVATE)
                                    prefs.edit()
                                        .putBoolean("has_selected_mode", true)
                                        .putString("app_mode", "parental")
                                        .apply()
                                    
                                    val dest = if (pinManager.isPinSet()) "dashboard" else "setup_pin"
                                    navController.navigate(dest)
                                }
                            )
                        }

                        composable("coming_soon") {
                            ComingSoonScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable("onboarding") {
                            OnboardingScreen(onFinishOnboarding = {
                                navController.navigate("mode_select") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            })
                        }

                        composable("setup_pin") {
                            SetupPinScreen(onPinSet = {
                                navController.navigate("dashboard") {
                                    popUpTo("setup_pin") { inclusive = true }
                                }
                            })
                        }

                        composable("dashboard") {
                            DashboardScreen(
                                onViewSchedules = { navController.navigate("schedules") },
                                onSelectApps    = { navController.navigate("app_select") },
                                onSettingsClick = { navController.navigate("settings") },
                                onCustomDomains = { navController.navigate("custom_domains") },
                                onDailyLimits   = { navController.navigate("daily_limits") },
                                onNavigateBack  = { navController.popBackStack() }
                            )
                        }

                        composable("app_select") {
                            AppSelectScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable("settings") {
                            SettingsScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable("custom_domains") {
                            CustomDomainScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable("schedules") {
                            ScheduleScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable("daily_limits") {
                            com.focusguard.ui.screens.DailyLimitsView(onNavigateBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
