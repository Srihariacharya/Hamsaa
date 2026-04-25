package com.contactpro.app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.contactpro.app.ui.auth.LoginScreen
import com.contactpro.app.ui.auth.RegisterScreen
import com.contactpro.app.ui.auth.SplashScreen
import com.contactpro.app.ui.contacts.*
import com.contactpro.app.ui.dashboard.DashboardScreen
import com.contactpro.app.ui.import_contacts.ImportScreen
import com.contactpro.app.ui.profile.ProfileScreen
import com.contactpro.app.ui.followups.FollowUpsScreen
import com.contactpro.app.ui.tasks.TasksScreen
import com.contactpro.app.viewmodel.ContactViewModel

// Route constants
object Routes {
    const val SPLASH   = "splash"
    const val LOGIN    = "login"
    const val REGISTER = "register"
    const val MAIN     = "main"
    // Main sub-routes
    const val CONTACTS       = "contacts"
    const val DASHBOARD      = "dashboard"
    const val IMPORT         = "import"
    const val FOLLOW_UPS     = "follow_ups"
    const val TASKS          = "tasks"
    const val PROFILE        = "profile"
    const val CONTACT_DETAIL = "contact_detail/{contactId}"
    const val ADD_CONTACT    = "add_contact"
    const val EDIT_CONTACT   = "edit_contact/{contactId}"
    const val ADD_INTERACTION= "add_interaction/{contactId}/{contactName}"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    userId: Long,
    modifier: Modifier = Modifier
) {
    // Shared ContactViewModel so list + detail share the same cache
    val contactVm: ContactViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.SPLASH,
        modifier = modifier) {

        composable(Routes.SPLASH) {
            SplashScreen(onSplashComplete = {
                navController.navigate(startDestination) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess       = {
                    navController.navigate(Routes.CONTACTS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // ── Bottom nav destinations ──────────────────────────────────────────
        composable(Routes.CONTACTS) {
            ContactsListScreen(
                userId         = userId,
                onContactClick = { cId ->
                    navController.navigate("contact_detail/$cId")
                },
                onAddContact   = { navController.navigate(Routes.ADD_CONTACT) },
                onImport       = { navController.navigate(Routes.IMPORT) },
                vm             = contactVm
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                userId         = userId,
                onContactClick = { cId -> navController.navigate("contact_detail/$cId") }
            )
        }

        composable(Routes.IMPORT) {
            ImportScreen(userId = userId, onBack = { navController.popBackStack() })
        }

        composable(Routes.FOLLOW_UPS) {
            FollowUpsScreen(
                userId = userId,
                onContactClick = { cId -> navController.navigate("contact_detail/$cId") }
            )
        }

        composable(Routes.TASKS) {
            TasksScreen(userId = userId)
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Contact detail ───────────────────────────────────────────────────
        composable(
            route     = Routes.CONTACT_DETAIL,
            arguments = listOf(navArgument("contactId") { type = NavType.LongType })
        ) { backStack ->
            val contactId = backStack.arguments?.getLong("contactId") ?: return@composable
            ContactDetailScreen(
                contactId       = contactId,
                userId          = userId,
                onBack          = { navController.popBackStack() },
                onEditContact   = { cId -> navController.navigate("edit_contact/$cId") },
                onAddInteraction = { cId ->
                    val name = "Contact"   // name passed via nav arg below
                    navController.navigate("add_interaction/$cId/$name")
                },
                contactVm       = contactVm
            )
        }

        // ── Add contact ──────────────────────────────────────────────────────
        composable(Routes.ADD_CONTACT) {
            AddEditContactScreen(
                userId   = userId,
                onBack   = { navController.popBackStack() },
                onSaved  = {
                    contactVm.loadContacts(userId)
                    navController.popBackStack()
                },
                vm       = contactVm
            )
        }

        // ── Edit contact ─────────────────────────────────────────────────────
        composable(
            route     = Routes.EDIT_CONTACT,
            arguments = listOf(navArgument("contactId") { type = NavType.LongType })
        ) { backStack ->
            val contactId    = backStack.arguments?.getLong("contactId") ?: return@composable
            val detailState  = contactVm.contactDetail.collectAsState().value
            val contact      = (detailState as? com.contactpro.app.network.ApiResult.Success)?.data
            AddEditContactScreen(
                userId          = userId,
                existingContact = contact,
                onBack          = { navController.popBackStack() },
                onSaved         = {
                    contactVm.loadContacts(userId)
                    navController.popBackStack(Routes.CONTACTS, inclusive = false)
                },
                vm              = contactVm
            )
        }

        // ── Add interaction ──────────────────────────────────────────────────
        composable(
            route     = Routes.ADD_INTERACTION,
            arguments = listOf(
                navArgument("contactId")   { type = NavType.LongType   },
                navArgument("contactName") { type = NavType.StringType }
            )
        ) { backStack ->
            val contactId   = backStack.arguments?.getLong("contactId")    ?: return@composable
            val contactName = backStack.arguments?.getString("contactName") ?: "Contact"
            com.contactpro.app.ui.interactions.AddInteractionScreen(
                contactId   = contactId,
                contactName = contactName,
                onBack      = { navController.popBackStack() },
                onSaved     = { navController.popBackStack() }
            )
        }
    }
}
