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

                when (currentScreen) {
                    Screen.Login -> {
                        LoginScreen(onLoginSuccess = {
                            currentScreen = Screen.Dashboard
                        })
                    }
                    Screen.Dashboard -> {
                        DashboardScreen(
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToSchedule = { currentScreen = Screen.Schedule }
                        )
                    }
                    Screen.Schedule -> {
                        ScheduleScreen(
                            onLogout = { currentScreen = Screen.Login },
                            onNavigateToDashboard = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }
    }
}
