package com.contactpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.isSystemInDarkTheme
import com.contactpro.app.ui.navigation.MainScreen
import com.contactpro.app.ui.navigation.Routes
import com.contactpro.app.ui.theme.HamsaaTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val session = SessionManager(this)

        setContent {
            val themeMode by session.themeMode.collectAsState(initial = "System")
            val darkTheme = when (themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            HamsaaTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                val userId by session.userId.collectAsState(initial = -2L)
                val isLoggedIn by session.isLoggedIn.collectAsState(initial = false)
                var startDest by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(isLoggedIn, userId) {
                    if (startDest == null && userId != -2L) {
                        // Load token before setting startDest
                        val token = session.token.first()
                        com.contactpro.app.network.RetrofitClient.authToken = token
                        
                        startDest = if (isLoggedIn && userId > 0) Routes.CONTACTS else Routes.LOGIN
                    }
                    if (isLoggedIn && userId > 0) {
                        launch { com.contactpro.app.SyncManager.syncRecentCalls(this@MainActivity, userId) }
                    }
                }

                if (startDest != null) {
                    MainScreen(
                        navController    = navController,
                        startDestination = startDest!!,
                        userId           = userId
                    )
                }
            }
        }
    }
}
