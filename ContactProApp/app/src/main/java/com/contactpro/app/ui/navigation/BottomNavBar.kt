package com.contactpro.app.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.contactpro.app.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Contacts  : BottomNavItem("contacts",  "Network",
        Icons.Filled.Group,    Icons.Outlined.Group)
    object FollowUps : BottomNavItem("follow_ups", "Reminders",
        Icons.Filled.NotificationsActive, Icons.Outlined.NotificationsActive)
    object Tasks     : BottomNavItem("tasks", "Tasks",
        Icons.Filled.FactCheck, Icons.Outlined.FactCheck)
    object Dashboard : BottomNavItem("dashboard", "Intelligence",
        Icons.Filled.AutoGraph, Icons.Outlined.AutoGraph)
    object Profile   : BottomNavItem("profile",   "Identity",
        Icons.Filled.Shield,    Icons.Outlined.Shield)
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Contacts,
        BottomNavItem.FollowUps,
        BottomNavItem.Tasks,
        BottomNavItem.Dashboard,
        BottomNavItem.Profile
    )
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.height(80.dp).shadow(
            elevation = 16.dp, 
            spotColor = Color.Black.copy(alpha = 0.1f),
            ambientColor = Color.Black.copy(alpha = 0.1f)
        )
    ) {
        items.forEach { item ->
            val selected = currentRoute?.startsWith(item.route) == true
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon  = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.height(24.dp)
                    )
                },
                label = { 
                    Text(item.label, 
                        fontSize = 11.sp, 
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor       = MaterialTheme.colorScheme.primary,
                    selectedTextColor       = MaterialTheme.colorScheme.primary,
                    unselectedIconColor     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    indicatorColor          = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
