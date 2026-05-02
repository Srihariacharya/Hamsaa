package com.contactpro.app.ui.contacts

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contactpro.app.network.ApiResult
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.ContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsListScreen(
    userId: Long,
    onContactClick: (Long) -> Unit,
    onAddContact: () -> Unit,
    onImport: () -> Unit,
    vm: ContactViewModel = viewModel()
) {
    val contacts    by vm.filteredContacts.collectAsState(emptyList())
    val contactsRaw by vm.contacts.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()

    val selectedIds = remember { mutableStateListOf<Long>() }
    var isSelectionMode by remember { mutableStateOf(false) }

    LaunchedEffect(userId) { vm.loadContacts(userId) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Favorites", "Blocked")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("${selectedIds.size} selected", style = MaterialTheme.typography.titleLarge)
                    } else {
                        Column {
                            Text("Contacts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (contactsRaw is ApiResult.Success) {
                                val count = (contactsRaw as ApiResult.Success).data.size
                                Text("$count contacts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = rememberCoroutineScope()
                    
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            if (selectedIds.size == contacts.size) {
                                selectedIds.clear()
                            } else {
                                selectedIds.clear()
                                selectedIds.addAll(contacts.map { it.id })
                            }
                        }) {
                            Icon(if (selectedIds.size == contacts.size) Icons.Default.Deselect else Icons.Default.SelectAll, "Select All")
                        }
                        IconButton(onClick = {
                            vm.deleteContactsBatch(selectedIds.toList(), userId)
                            selectedIds.clear()
                            isSelectionMode = false
                        }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Error)
                        }
                        IconButton(onClick = { 
                            isSelectionMode = false
                            selectedIds.clear()
                        }) {
                            Icon(Icons.Default.Close, "Cancel")
                        }
                    } else {
                        IconButton(onClick = { isSelectionMode = true }) {
                            Icon(Icons.Default.EditNotifications, "Select", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onImport) {
                            Icon(Icons.Outlined.CloudUpload, "Import", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { 
                            vm.loadContacts(userId)
                            scope.launch { com.contactpro.app.SyncManager.syncRecentCalls(context, userId) }
                        }) {
                            Icon(Icons.Outlined.Refresh, "Refresh", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddContact,
                icon    = { Icon(Icons.Filled.Add, null) },
                text    = { Text("Add Contact") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = Color.White,
                shape = CircleShape
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Search bar
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { vm.setSearchQuery(it) },
                placeholder   = { Text("Search contacts...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                leadingIcon   = { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon  = if (searchQuery.isNotBlank()) {
                    { IconButton(onClick = { vm.setSearchQuery("") }) {
                        Icon(Icons.Filled.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
                    }
                } else null,
                modifier      = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                shape         = RoundedCornerShape(16.dp),
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 2.dp
                    )
                }
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { 
                            Text(title, 
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ) 
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when (contactsRaw) {
                is ApiResult.Loading -> LoadingOverlay()
                is ApiResult.Error   -> {
                    EmptyState(
                        icon     = Icons.Outlined.CloudOff,
                        title    = "Connection Error",
                        subtitle = (contactsRaw as ApiResult.Error).message
                    )
                }
                is ApiResult.Success -> {
                    val displayList = when (selectedTab) {
                        1 -> contacts.filter { it.isFavorite }
                        2 -> contacts.filter { it.isBlocked }
                        else -> contacts
                    }

                    if (displayList.isEmpty()) {
                        EmptyState(
                            icon     = Icons.Outlined.PeopleOutline,
                            title    = "No Nodes Found",
                            subtitle = "Start building your network by adding a contact."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp, end = 20.dp,
                                top = 8.dp, bottom = 100.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayList, key = { it.id }) { contact ->
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    if (isSelectionMode) {
                                        Checkbox(
                                            checked = selectedIds.contains(contact.id),
                                            onCheckedChange = { 
                                                if (it) selectedIds.add(contact.id) else selectedIds.remove(contact.id)
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                    ContactCard(
                                        contact         = contact,
                                        modifier        = Modifier.weight(1f),
                                        onClick         = { 
                                            if (isSelectionMode) {
                                                if (selectedIds.contains(contact.id)) selectedIds.remove(contact.id)
                                                else selectedIds.add(contact.id)
                                            } else {
                                                onContactClick(contact.id)
                                            }
                                        },
                                        onFavoriteClick = { if (!isSelectionMode) vm.toggleFavorite(contact.id, userId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
