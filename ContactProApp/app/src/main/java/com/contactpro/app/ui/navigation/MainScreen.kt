package com.contactpro.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Main shell for authenticated users.
 * Shows the bottom nav bar and hosts the AppNavGraph.
 */
@Composable
fun MainScreen(
    navController: NavHostController,
    startDestination: String,
    userId: Long
) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route

    // Screens that should NOT show the bottom bar
    val noBottomBarRoutes = setOf(
        Routes.LOGIN, Routes.REGISTER,
        Routes.ADD_CONTACT, Routes.ADD_INTERACTION,
        Routes.IMPORT
    )
    val showBottomBar = currentRoute?.let { route ->
        noBottomBarRoutes.none { route.startsWith(it) } &&
        !route.startsWith("contact_detail") &&
        !route.startsWith("edit_contact") &&
        !route.startsWith("add_interaction")
    } ?: false

    Scaffold(
        bottomBar = { if (showBottomBar) BottomNavBar(navController) }
    ) { innerPadding ->
        AppNavGraph(
            navController    = navController,
            startDestination = startDestination,
            userId           = userId,
            modifier         = Modifier.padding(innerPadding)
        )
    }
}
