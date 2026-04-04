package com.example.autofeedmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.network.UserResponse
import com.example.autofeedmobile.ui.theme.AutoFeedMobileTheme

enum class Screen {
    Login, Dashboard, Inventory, Schedule, Requests, Reports, Profile, Alerts
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoFeedMobileTheme {
                var currentScreen by remember { mutableStateOf(Screen.Login) }
                var userId by remember { mutableIntStateOf(-1) }
                var userFullName by remember { mutableStateOf("") }
                var userAvatarUrl by remember { mutableStateOf<String?>(null) }

                when (currentScreen) {
                    Screen.Login -> {
                        LoginScreen(onLoginSuccess = { user ->
                            userId = user.userId
                            userFullName = user.fullName
                            userAvatarUrl = RetrofitClient.getFullUrl(user.avatarUrl)
                            currentScreen = Screen.Dashboard
                        })
                    }
                    Screen.Dashboard -> {
                        DashboardScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToProfile = { currentScreen = Screen.Profile },
                            onNavigateToAlerts = { currentScreen = Screen.Alerts }
                        )
                    }
                    Screen.Inventory -> {
                        InventoryScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onNavigateToProfile = { currentScreen = Screen.Profile }
                        )
                    }
                    Screen.Schedule -> {
                        ScheduleScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToProfile = { currentScreen = Screen.Profile }
                        )
                    }
                    Screen.Requests -> {
                        RequestScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onBackToProfile = { currentScreen = Screen.Profile }
                        )
                    }
                    Screen.Reports -> {
                        ReportScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onBackToProfile = { currentScreen = Screen.Profile }
                        )
                    }
                    Screen.Profile -> {
                        ProfileScreen(
                            userId = userId,
                            userFullName = userFullName,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onNavigateToRequests = { currentScreen = Screen.Requests },
                            onNavigateToReports = { currentScreen = Screen.Reports },
                            onProfileUpdated = { updatedUser ->
                                userFullName = updatedUser.fullName
                                userAvatarUrl = RetrofitClient.getFullUrl(updatedUser.avatarUrl)
                            }
                        )
                    }
                    Screen.Alerts -> {
                        AlertsScreen(
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { currentScreen = Screen.Login },
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }
    }
}
