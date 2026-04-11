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
    Login, Dashboard, Inventory, Schedule, Requests, Reports, Profile, Notifications, Settings, ChangePassword, ForgotPassword
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoFeedMobileTheme {
                val sessionManager = remember { SessionManager(this@MainActivity) }
                var currentScreen by remember { mutableStateOf(Screen.Login) }
                var userId by remember { mutableIntStateOf(-1) }
                var userFullName by remember { mutableStateOf("") }
                var userAvatarUrl by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val token = sessionManager.fetchAuthToken()
                    val user = sessionManager.fetchUser()
                    if (token != null && user != null) {
                        RetrofitClient.setAuthToken(token)
                        userId = user.userId
                        userFullName = user.fullName
                        userAvatarUrl = RetrofitClient.getFullUrl(user.avatarUrl)
                        currentScreen = Screen.Dashboard
                    }
                }

                when (currentScreen) {
                    Screen.Login -> {
                        LoginScreen(
                            onLoginSuccess = { user, token ->
                                sessionManager.saveAuthToken(token)
                                sessionManager.saveUser(user)
                                RetrofitClient.setAuthToken(token)
                                userId = user.userId
                                userFullName = user.fullName
                                userAvatarUrl = RetrofitClient.getFullUrl(user.avatarUrl)
                                currentScreen = Screen.Dashboard
                            },
                            onForgotPassword = { currentScreen = Screen.ForgotPassword }
                        )
                    }
                    Screen.ForgotPassword -> {
                        ForgotPasswordScreen(onBack = { currentScreen = Screen.Login })
                    }
                    Screen.Dashboard -> {
                        DashboardScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { 
                                sessionManager.clearSession()
                                RetrofitClient.setAuthToken(null)
                                currentScreen = Screen.Login 
                            },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToProfile = { currentScreen = Screen.Profile },
                            onNavigateToNotifications = { currentScreen = Screen.Notifications }
                        )
                    }
                    Screen.Inventory -> {
                        InventoryScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { 
                                sessionManager.clearSession()
                                RetrofitClient.setAuthToken(null)
                                currentScreen = Screen.Login 
                            },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onNavigateToProfile = { currentScreen = Screen.Profile },
                            onNavigateToNotifications = { currentScreen = Screen.Notifications }
                        )
                    }
                    Screen.Schedule -> {
                        ScheduleScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { 
                                sessionManager.clearSession()
                                RetrofitClient.setAuthToken(null)
                                currentScreen = Screen.Login 
                            },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToProfile = { currentScreen = Screen.Profile },
                            onNavigateToNotifications = { currentScreen = Screen.Notifications }
                        )
                    }
                    Screen.Requests -> {
                        RequestScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { 
                                sessionManager.clearSession()
                                RetrofitClient.setAuthToken(null)
                                currentScreen = Screen.Login 
                            },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onBackToProfile = { currentScreen = Screen.Profile },
                            onNavigateToNotifications = { currentScreen = Screen.Notifications }
                        )
                    }
                    Screen.Reports -> {
                        ReportScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { 
                                sessionManager.clearSession()
                                RetrofitClient.setAuthToken(null)
                                currentScreen = Screen.Login 
                            },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onBackToProfile = { currentScreen = Screen.Profile },
                            onNavigateToNotifications = { currentScreen = Screen.Notifications }
                        )
                    }
                    Screen.Profile -> {
                        ProfileScreen(
                            userId = userId,
                            userFullName = userFullName,
                            onLogout = { 
                                sessionManager.clearSession()
                                RetrofitClient.setAuthToken(null)
                                currentScreen = Screen.Login 
                            },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onNavigateToRequests = { currentScreen = Screen.Requests },
                            onNavigateToReports = { currentScreen = Screen.Reports },
                            onNavigateToNotifications = { currentScreen = Screen.Notifications },
                            onNavigateToSettings = { currentScreen = Screen.Settings },
                            onProfileUpdated = { updatedUser ->
                                sessionManager.saveUser(updatedUser)
                                userFullName = updatedUser.fullName
                                userAvatarUrl = RetrofitClient.getFullUrl(updatedUser.avatarUrl)
                            }
                        )
                    }
                    Screen.Settings -> {
                        SettingsScreen(
                            onBack = { currentScreen = Screen.Profile },
                            onNavigateToChangePassword = { currentScreen = Screen.ChangePassword }
                        )
                    }
                    Screen.ChangePassword -> {
                        ChangePasswordScreen(
                            userId = userId,
                            onBack = { currentScreen = Screen.Settings }
                        )
                    }
                    Screen.Notifications -> {
                        NotificationScreen(
                            userId = userId,
                            userFullName = userFullName,
                            userAvatarUrl = userAvatarUrl,
                            onLogout = { 
                                sessionManager.clearSession()
                                RetrofitClient.setAuthToken(null)
                                currentScreen = Screen.Login 
                            },
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }
    }
}
