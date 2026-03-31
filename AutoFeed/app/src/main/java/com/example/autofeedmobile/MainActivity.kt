package com.example.autofeedmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.autofeedmobile.network.UserResponse
import com.example.autofeedmobile.ui.theme.AutoFeedMobileTheme

enum class Screen {
    Login, Dashboard, Inventory, Schedule, Requests, Reports
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

                when (currentScreen) {
                    Screen.Login -> {
                        LoginScreen(onLoginSuccess = { user ->
                            userId = user.userId
                            userFullName = user.fullName
                            currentScreen = Screen.Dashboard
                        })
                    }
                    Screen.Dashboard -> {
                        DashboardScreen(
                            userId = userId,
                            userFullName = userFullName,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToRequests = { currentScreen = Screen.Requests },
                            onNavigateToReports = { currentScreen = Screen.Reports }
                        )
                    }
                    Screen.Inventory -> {
                        InventoryScreen(
                            userId = userId,
                            userFullName = userFullName,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onNavigateToRequests = { currentScreen = Screen.Requests },
                            onNavigateToReports = { currentScreen = Screen.Reports }
                        )
                    }
                    Screen.Schedule -> {
                        ScheduleScreen(
                            userId = userId,
                            userFullName = userFullName,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToRequests = { currentScreen = Screen.Requests },
                            onNavigateToReports = { currentScreen = Screen.Reports }
                        )
                    }
                    Screen.Requests -> {
                        RequestScreen(
                            userId = userId,
                            userFullName = userFullName,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onNavigateToReports = { currentScreen = Screen.Reports }
                        )
                    }
                    Screen.Reports -> {
                        ReportScreen(
                            userId = userId,
                            userFullName = userFullName,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard },
                            onNavigateToInventory = { currentScreen = Screen.Inventory },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule },
                            onNavigateToRequests = { currentScreen = Screen.Requests }
                        )
                    }
                }
            }
        }
    }
}
