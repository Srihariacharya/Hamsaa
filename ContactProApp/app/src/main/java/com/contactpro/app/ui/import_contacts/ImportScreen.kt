package com.contactpro.app.ui.import_contacts

import android.provider.CallLog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contactpro.app.model.CallLogEntry
import com.contactpro.app.model.DeviceContact
import com.contactpro.app.network.ApiResult
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.ImportViewModel
import com.google.accompanist.permissions.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ImportScreen(
    userId: Long,
    onBack: () -> Unit,
    vm: ImportViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Network Sync", "Call Intelligence")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Network Ingestion", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = MaterialTheme.colorScheme.primary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) NetworkSyncTab(userId, vm)
                else CallLogImportTab(userId, vm)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NetworkSyncTab(userId: Long, vm: ImportViewModel) {
    val context = LocalContext.current
    val contacts by vm.deviceContacts.collectAsState()
    val importState by vm.importState.collectAsState()
    val permissionState = rememberPermissionState(android.Manifest.permission.READ_CONTACTS)
    var showDisclosure by remember { mutableStateOf(!permissionState.status.isGranted) }

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            vm.fetchDeviceContacts()
        }
    }

    LaunchedEffect(importState) {
        if (importState is ApiResult.Success) {
            vm.resetImportState()
            android.widget.Toast.makeText(context, "Network successfully ingested!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (showDisclosure && !permissionState.status.isGranted) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            icon = { Icon(Icons.Outlined.Shield, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Intelligence Authorization") },
            text = {
                Text("Hamsaa requires access to your device contacts and call history to synchronize your professional network and analyze engagement trends. This data is processed securely to provide you with interaction intelligence.")
            },
            confirmButton = {
                PrimaryButton("Proceed to Authorization", onClick = { 
                    showDisclosure = false
                    permissionState.launchPermissionRequest() 
                })
            },
            dismissButton = {
                TextButton(onClick = { showDisclosure = false }) { Text("Not Now") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        if (!permissionState.status.isGranted) {
            EmptyState(
                icon = Icons.Outlined.Contacts,
                title = "Sync Permission Required",
                subtitle = "Authorize Hamsaa to sync with your device contacts for real-time intelligence."
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Grant Sync Access",
                onClick = { showDisclosure = true },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Card(
                onClick = { vm.fetchDeviceContacts() },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Sync, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Device Network Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Sync nodes from your phone directly into the Hamsaa intelligence engine.",
                        style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (contacts.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No contacts found on device", color = TextSecondary)
                }
            } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Detected Nodes (${contacts.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { if (contacts.any { !it.selected }) vm.selectAll() else vm.deselectAll() }) {
                    Text(if (contacts.all { it.selected }) "Deselect All" else "Select All", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(4.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(contacts) { contact ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { vm.toggleDeviceContactSelection(contact.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (contact.selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(0.5.dp, if (contact.selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = contact.selected,
                                    onCheckedChange = { vm.toggleDeviceContactSelection(contact.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                    Text(contact.name.take(1), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(contact.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(contact.phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                val syncCount by vm.importCount.collectAsState()
                val totalSelected = contacts.count { it.selected }
                PrimaryButton(
                    text = if (importState is ApiResult.Loading) "Ingesting $syncCount / $totalSelected..." else "Authorize Selection Ingestion",
                    onClick = { vm.importSelectedContacts(userId) },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = importState is ApiResult.Loading
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CallLogImportTab(userId: Long, vm: ImportViewModel) {
    val context = LocalContext.current
    val logs by vm.callLogs.collectAsState()
    val permissionState = rememberPermissionState(android.Manifest.permission.READ_CALL_LOG)
    var showDisclosure by remember { mutableStateOf(!permissionState.status.isGranted) }

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            vm.fetchCallLogs()
        }
    }

    if (showDisclosure && !permissionState.status.isGranted) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            icon = { Icon(Icons.Outlined.Shield, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Call Intelligence Access") },
            text = {
                Text("Hamsaa analyzes your call history to automatically generate interaction logs and calculate engagement frequencies. This helps in maintaining accurate intelligence on your professional network.")
            },
            confirmButton = {
                PrimaryButton("Grant Permission", onClick = { 
                    showDisclosure = false
                    permissionState.launchPermissionRequest() 
                })
            },
            dismissButton = {
                TextButton(onClick = { showDisclosure = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        if (permissionState.status.isGranted) {
            if (logs.isEmpty()) {
                EmptyState(Icons.Outlined.History, "No Call Intelligence", "No recent engagement logs found on this device.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(logs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(0.5.dp, LightBorder.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (log.type == CallLog.Calls.INCOMING_TYPE) Icons.Outlined.CallReceived else Icons.Outlined.CallMade,
                                    contentDescription = null,
                                    tint = if (log.type == CallLog.Calls.INCOMING_TYPE) Success else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.name ?: log.number, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(log.date)), 
                                        style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Text("${log.duration}s", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        } else {
            EmptyState(
                icon = Icons.Outlined.Lock,
                title = "Authorization Required",
                subtitle = "Hamsaa requires permission to analyze engagement logs for network intelligence."
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Grant Access",
                onClick = { showDisclosure = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
