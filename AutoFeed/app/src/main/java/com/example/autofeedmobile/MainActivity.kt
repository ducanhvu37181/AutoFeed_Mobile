package com.example.autofeedmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.autofeedmobile.ui.theme.AutoFeedMobileTheme

enum class Screen {
    Login, Dashboard, Schedule
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoFeedMobileTheme {
                var currentScreen by remember { mutableStateOf(Screen.Login) }
                var userId by remember { mutableIntStateOf(-1) }

                when (currentScreen) {
                    Screen.Login -> {
                        LoginScreen(onLoginSuccess = { id ->
                            userId = id
                            currentScreen = Screen.Dashboard
                        })
                    }
                    Screen.Dashboard -> {
                        DashboardScreen(
                            userId = userId,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule }
                        )
                    }
                    Screen.Schedule -> {
                        ScheduleScreen(
                            userId = userId,
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }
    }
}
