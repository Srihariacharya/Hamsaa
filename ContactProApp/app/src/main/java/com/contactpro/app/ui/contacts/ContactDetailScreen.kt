package com.contactpro.app.ui.contacts

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contactpro.app.model.ContactResponse
import com.contactpro.app.network.ApiResult
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.interactions.InteractionHistorySection
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.ContactViewModel
import com.contactpro.app.viewmodel.InteractionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId: Long,
    userId: Long,
    onBack: () -> Unit,
    onEditContact: (Long) -> Unit,
    onAddInteraction: (Long) -> Unit,
    contactVm: ContactViewModel    = viewModel(),
    interactionVm: InteractionViewModel = viewModel(),
    taskVm: com.contactpro.app.viewmodel.TaskViewModel = viewModel()
) {
    val detailState    by contactVm.contactDetail.collectAsState()
    val deleteState    by contactVm.deleteState.collectAsState()
    val linkedTasks    by taskVm.getTasksByContact(contactId).collectAsState(emptyList())
    val snackbarHost   = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(contactId) {
        contactVm.loadContactDetail(contactId)
        interactionVm.loadInteractions(contactId)
    }

    LaunchedEffect(deleteState) {
        if (deleteState is ApiResult.Success) { contactVm.resetDeleteState(); onBack() }
        if (deleteState is ApiResult.Error) {
            snackbarHost.showSnackbar((deleteState as ApiResult.Error).message)
            contactVm.resetDeleteState()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title            = { Text("Terminate Node Connection") },
            text             = { Text("This will permanently remove the contact and all associated intelligence logs.") },
            confirmButton    = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    contactVm.deleteContact(contactId, userId)
                }) { Text("Delete", color = Error) }
            },
            dismissButton    = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Node Intelligence", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = HamsaaPrimary) }
                },
                actions = {
                    IconButton(onClick = { onEditContact(contactId) }) {
                        Icon(Icons.Outlined.Edit, "Modify", tint = HamsaaPrimary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Outlined.Delete, "Terminate", tint = Error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when (detailState) {
            is ApiResult.Loading, null -> LoadingOverlay()
            is ApiResult.Error -> EmptyState(
                icon     = Icons.Outlined.ErrorOutline,
                title    = "Load Error",
                subtitle = (detailState as ApiResult.Error).message,
                actionLabel = "Retry",
                onAction = { contactVm.loadContactDetail(contactId) }
            )
            is ApiResult.Success -> {
                val contact = (detailState as ApiResult.Success<ContactResponse>).data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(HamsaaPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(contact.name.take(1).uppercase(),
                                    color      = HamsaaPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 36.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(contact.name, style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            
                            contact.category?.let {
                                Surface(
                                    color = HamsaaPrimary.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text(it.uppercase(), 
                                        style = MaterialTheme.typography.labelMedium, 
                                        color = HamsaaPrimary, 
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AssistChip(
                                    onClick = { contactVm.toggleFavorite(contactId, userId) },
                                    label   = { Text(if (contact.isFavorite) "Unfavorite" else "Favorite") },
                                    leadingIcon = {
                                        Icon(
                                            if (contact.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                            null, tint = if (contact.isFavorite) Color(0xFFFFB300) else TextHint
                                        )
                                    },
                                    shape = CircleShape,
                                    border = BorderStroke(0.5.dp, LightBorder.copy(alpha = 0.5f))
                                )
                                AssistChip(
                                    onClick = { contactVm.toggleBlock(contactId, userId) },
                                    label   = { Text(if (contact.isBlocked) "Unblock" else "Block") },
                                    leadingIcon = {
                                        Icon(
                                            if (contact.isBlocked) Icons.Filled.Block else Icons.Outlined.Block,
                                            null, tint = if (contact.isBlocked) Error else TextHint
                                        )
                                    },
                                    shape = CircleShape,
                                    border = BorderStroke(0.5.dp, LightBorder.copy(alpha = 0.5f))
                                )
                                AssistChip(
                                    onClick = { onAddInteraction(contactId) },
                                    label   = { Text("Log Activity") },
                                    leadingIcon = { Icon(Icons.Outlined.AddCircleOutline, null, tint = HamsaaPrimary) },
                                    shape = CircleShape,
                                    border = BorderStroke(0.5.dp, LightBorder.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }

                    // Detail section
                    SectionHeader("Entity Attributes", modifier = Modifier.padding(horizontal = 24.dp))
                    Card(
                        modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape     = RoundedCornerShape(20.dp),
                        border    = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DetailRow(Icons.Outlined.Phone,     "Connectivity",    contact.phone)
                            contact.email?.let { DetailRow(Icons.Outlined.Email, "Secure Channel", it) }
                            DetailRow(Icons.Outlined.Person, "Classification", contact.gender ?: "Others")
                            contact.dob?.let    { DetailRow(Icons.Outlined.Cake, "Commencement", it) }
                            if (contact.followUpFrequency > 0)
                                DetailRow(Icons.Outlined.Alarm, "Refresh Interval", "${contact.followUpFrequency} Days")
                        }
                    }

                    // Tasks linked to this contact
                    if (linkedTasks.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        SectionHeader("Linked Reminders", modifier = Modifier.padding(horizontal = 24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = LightSurface),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(0.5.dp, LightBorder.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                linkedTasks.forEach { task ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (task.status == "COMPLETED") Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            null,
                                            tint = if (task.status == "COMPLETED") Success else TextHint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(task.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    
                    // Interaction history
                    SectionHeader("Intelligence Logs", modifier = Modifier.padding(horizontal = 24.dp))
                    InteractionHistorySection(interactionVm)
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(HamsaaPrimary.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = HamsaaPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextHint)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}
