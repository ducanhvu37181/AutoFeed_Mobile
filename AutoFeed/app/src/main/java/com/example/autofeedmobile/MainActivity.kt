package com.example.autofeedmobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.autofeedmobile.network.RetrofitClient
import java.util.concurrent.TimeUnit
import com.example.autofeedmobile.data.SessionManager
import com.example.autofeedmobile.ui.auth.ChangePasswordScreen
import com.example.autofeedmobile.ui.auth.ForgotPasswordScreen
import com.example.autofeedmobile.ui.auth.LoginScreen
import com.example.autofeedmobile.ui.barn.BarnManagementScreen
import com.example.autofeedmobile.ui.chicken.ChickenManagementScreen
import com.example.autofeedmobile.ui.dashboard.DashboardScreen
import com.example.autofeedmobile.ui.inventory.InventoryScreen
import com.example.autofeedmobile.ui.notification.NotificationHelper
import com.example.autofeedmobile.ui.notification.NotificationScreen
import com.example.autofeedmobile.ui.notification.NotificationWorker
import com.example.autofeedmobile.ui.report.ReportScreen
import com.example.autofeedmobile.ui.request.RequestScreen
import com.example.autofeedmobile.ui.schedule.ScheduleScreen
import com.example.autofeedmobile.ui.settings.ProfileScreen
import com.example.autofeedmobile.ui.settings.SettingsScreen
import com.example.autofeedmobile.ui.theme.AutoFeedMobileTheme

enum class Screen {
    Login, Dashboard, Inventory, Schedule, Requests, Reports, Profile, Notifications, Settings, ChangePassword, ForgotPassword, ChickenManagement, BarnManagement
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            AutoFeedMobileTheme {
                val context = androidx.compose.ui.platform.LocalContext.current
                var hasNotificationPermission by remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        } else true
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasNotificationPermission = isGranted
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    setupBackgroundWorker(context)
                }

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

                LaunchedEffect(userId) {
                    if (userId != -1) {
                        // Immediate check on login/start
                        val immediateWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                            .build()
                        WorkManager.getInstance(context).enqueue(immediateWork)

                        // Periodic check while app is in foreground (every 1 minute for "always" feel)
                        while (true) {
                            kotlinx.coroutines.delay(60000)
                            val periodicWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                                .build()
                            WorkManager.getInstance(context).enqueue(periodicWork)
                        }
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
                            onNavigateToNotifications = { currentScreen = Screen.Notifications },
                            onNavigateToRequests = { currentScreen = Screen.Requests },
                            onNavigateToReports = { currentScreen = Screen.Reports },
                            onNavigateToChickenManagement = { currentScreen = Screen.ChickenManagement },
                            onNavigateToBarnManagement = { currentScreen = Screen.BarnManagement }
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
                            onNavigateToNotifications = { currentScreen = Screen.Notifications },
                            onNavigateToChickenManagement = { currentScreen = Screen.ChickenManagement },
                            onNavigateToBarnManagement = { currentScreen = Screen.BarnManagement }
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
                            onNavigateToNotifications = { currentScreen = Screen.Notifications },
                            onNavigateToChickenManagement = { currentScreen = Screen.ChickenManagement },
                            onNavigateToBarnManagement = { currentScreen = Screen.BarnManagement }
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
                            onNavigateToNotifications = { currentScreen = Screen.Notifications },
                            onNavigateToChickenManagement = { currentScreen = Screen.ChickenManagement },
                            onNavigateToBarnManagement = { currentScreen = Screen.BarnManagement }
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
                            onNavigateToNotifications = { currentScreen = Screen.Notifications },
                            onNavigateToChickenManagement = { currentScreen = Screen.ChickenManagement },
                            onNavigateToBarnManagement = { currentScreen = Screen.BarnManagement }
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
                            onNavigateToChickenManagement = { currentScreen = Screen.ChickenManagement },
                            onNavigateToBarnManagement = { currentScreen = Screen.BarnManagement },
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
                    Screen.ChickenManagement -> {
                        ChickenManagementScreen(
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
                            onNavigateToProfile = { currentScreen = Screen.Profile },
                            onNavigateToNotifications = { currentScreen = Screen.Notifications },
                            onNavigateToChickenManagement = { currentScreen = Screen.ChickenManagement },
                            onNavigateToBarnManagement = { currentScreen = Screen.BarnManagement }
                        )
                    }
                    Screen.BarnManagement -> {
                        BarnManagementScreen(
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
                            onNavigateToProfile = { currentScreen = Screen.Profile },
                            onNavigateToNotifications = { currentScreen = Screen.Notifications },
                            onNavigateToChickenManagement = { currentScreen = Screen.ChickenManagement }
                        )
                    }
                }
            }
        }
    }

    private fun setupBackgroundWorker(context: android.content.Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "notification_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}
