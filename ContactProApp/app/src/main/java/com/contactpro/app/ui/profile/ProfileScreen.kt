package com.contactpro.app.ui.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.contactpro.app.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contactpro.app.network.ApiResult
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val userName    by vm.userName.collectAsState("")
    val userEmail   by vm.userEmail.collectAsState("")
    val userId      by vm.userId.collectAsState(-1L)
    val updateState by vm.updateState.collectAsState()

    val snackbarHost = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    val currentTheme by vm.session.themeMode.collectAsState(initial = "System")

    LaunchedEffect(updateState) {
        if (updateState is ApiResult.Success) {
            snackbarHost.showSnackbar("Profile updated")
            vm.resetUpdateState()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text  = { Text("Are you sure you want to sign out of Hamsaa?") },
            confirmButton = {
                TextButton(onClick = { vm.logout(); onLogout() }) {
                    Text("Sign Out", color = Error)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            icon = { Icon(Icons.Outlined.Shield, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Privacy & Data Protocols") },
            text  = { 
                Text("Your data is encrypted using industry-standard protocols. Hamsaa ensures that your contact intelligence remains private and is never shared with third parties without explicit authorization.") 
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) { Text("Acknowledge") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.logo_brand),
                    contentDescription = "Logo",
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                )
            },
            title = { Text("Hamsaa Intelligence") },
            text  = { 
                Text("Hamsaa is an advanced Business Intelligence & Contact Management System designed to streamline professional networking and interaction tracking. It leverages smart ingestion and trend analysis to provide actionable insights into your professional network, ensuring you never miss a critical follow-up.") 
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Contact Us") },
            text  = { 
                Column {
                    Text("For support or inquiries, reach out to our developers:")
                    Spacer(Modifier.height(8.dp))
                    Text("• srivishnupejathaya127@gmail.com", fontWeight = FontWeight.Bold)
                    Text("• srihariacharya30@gmail.com", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(onClick = { showContactDialog = false }) { Text("Done") }
            }
        )
    }

    if (showPasswordDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Password & Security") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Update your security credentials below.", style = MaterialTheme.typography.bodySmall)
                    InputField(oldPass, { oldPass = it; errorText = null }, "Current Password")
                    InputField(newPass, { newPass = it; errorText = null }, "New Password")
                    InputField(confirmPass, { confirmPass = it; errorText = null }, "Confirm New Password")
                    
                    errorText?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                PrimaryButton("Update", onClick = {
                    if (newPass != confirmPass) {
                        errorText = "New passwords do not match"
                    } else if (oldPass.isBlank() || newPass.isBlank()) {
                        errorText = "All fields are required"
                    } else {
                        if (userId > 0) {
                            vm.changePassword(userId, oldPass, newPass)
                            showPasswordDialog = false
                        } else {
                            errorText = "Session error. Please re-login."
                        }
                    }
                })
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showNotificationDialog) {
        var push by remember { mutableStateOf(true) }
        var email by remember { mutableStateOf(false) }
        var sms by remember { mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Notification Settings") },
            text = {
                Column {
                    NotificationToggle("Push Notifications", push) { push = it }
                    NotificationToggle("Email Reports", email) { email = it }
                    NotificationToggle("SMS Alerts", sms) { sms = it }
                }
            },
            confirmButton = {
                Button(onClick = { showNotificationDialog = false }) { Text("Save") }
            }
        )
    }

    val userPhone   by vm.userPhone.collectAsState("")
    val userCompany by vm.userCompany.collectAsState("")

    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InputField(editName, { editName = it }, "Full Name")
                }
            },
            confirmButton = {
                PrimaryButton("Save", onClick = {
                    vm.updateProfile(userId, editName, userPhone.ifEmpty { null }, userCompany.ifEmpty { null })
                    showEditProfileDialog = false
                })
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    listOf("System", "Light", "Dark").forEach { themeOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    vm.setTheme(themeOption)
                                    showThemeDialog = false 
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == themeOption,
                                onClick = { 
                                    vm.setTheme(themeOption)
                                    showThemeDialog = false 
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(themeOption)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))

            // Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Outlined.Edit, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Account Section
            ProfileSectionLabel("Account")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ProfileOptionRow(Icons.Outlined.Lock, "Password & Security") { showPasswordDialog = true }
                    Divider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ProfileOptionRow(Icons.Outlined.Notifications, "Notifications") { showNotificationDialog = true }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Preferences Section
            ProfileSectionLabel("Preferences")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ProfileOptionRow(Icons.Outlined.Info, "About Us") { showAboutDialog = true }
                    Divider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ProfileOptionRow(Icons.Outlined.Palette, "Theme", trailingText = currentTheme) { showThemeDialog = true }
                    Divider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ProfileOptionRow(Icons.Outlined.Shield, "Privacy & Data Protocols") { showPrivacyDialog = true }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Support Section
            ProfileSectionLabel("Support")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ProfileOptionRow(Icons.Outlined.Email, "Contact Us") { showContactDialog = true }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Logout Button
            TextButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = Error)
            ) {
                Icon(Icons.Filled.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun NotificationToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary))
    }
}

@Composable
fun ProfileSectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun ProfileOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (trailingText != null) {
            Text(trailingText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
    }
}
