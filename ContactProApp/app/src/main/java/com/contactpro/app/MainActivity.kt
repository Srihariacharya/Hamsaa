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

                var startDest by remember { mutableStateOf<String?>(null) }
                var userId    by remember { mutableLongStateOf(-1L) }

                // Read session once to determine start destination
                LaunchedEffect(Unit) {
                    val loggedIn  = session.isLoggedIn.first()
                    val savedId   = session.userId.first()
                    startDest = if (loggedIn && savedId > 0) Routes.CONTACTS else Routes.LOGIN
                    userId    = savedId
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
