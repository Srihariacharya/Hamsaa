package com.contactpro.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contactpro.app.data.local.TaskEntity
import com.contactpro.app.network.ApiResult
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.TaskViewModel
import com.contactpro.app.model.ContactResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    userId: Long,
    vm: TaskViewModel = viewModel(),
    contactVm: com.contactpro.app.viewmodel.ContactViewModel = viewModel()
) {
    val tasks by vm.getTasks(userId).collectAsState(initial = emptyList())
    val contactsResult by contactVm.contacts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) { contactVm.loadContacts(userId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Intelligence Tasks", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = HamsaaPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, "Add Task")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (tasks.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Assignment,
                    title = "No Tasks Active",
                    subtitle = "Initialize a new task to track your intelligence objectives."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp,
                        top = 16.dp, bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks) { task ->
                        val res = contactsResult
                        val contacts = if (res is ApiResult.Success) res.data else emptyList<ContactResponse>()
                        val contactName = contacts.find { it.id == task.contactId }?.name
                        TaskCard(task, contactName, onToggle = { vm.toggleTaskCompletion(task) }, onDelete = { vm.deleteTask(task) })
                    }
                }
            }
        }

        if (showAddDialog) {
            val res = contactsResult
            val contacts = if (res is ApiResult.Success) res.data else emptyList<ContactResponse>()
            AddTaskDialog(
                contacts = contacts,
                onDismiss = { showAddDialog = false },
                onAdd = { title, desc, cId ->
                    vm.addTask(TaskEntity(title = title, description = desc, dueDate = null, priority = "MEDIUM", status = "PENDING", contactId = cId, userId = userId))
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun TaskCard(task: TaskEntity, contactName: String?, onToggle: () -> Unit, onDelete: () -> Unit) {
    val isCompleted = task.status == "COMPLETED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, LightBorder.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    null,
                    tint = if (isCompleted) Success else TextHint,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    color = if (isCompleted) TextHint else MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (contactName != null) {
                        Icon(Icons.Outlined.Person, null, tint = HamsaaPrimary, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(contactName, style = MaterialTheme.typography.labelSmall, color = HamsaaPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    if (!task.description.isNullOrBlank()) {
                        Text(
                            task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, null, tint = Error.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    contacts: List<com.contactpro.app.model.ContactResponse>,
    onDismiss: () -> Unit, 
    onAdd: (String, String, Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedContactId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Intelligence Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InputField(title, { title = it }, "Task Title")
                InputField(desc, { desc = it }, "Description (Optional)", singleLine = false, maxLines = 3)
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    val contactName = contacts.find { it.id == selectedContactId }?.name ?: "No Contact Linked"
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Link Contact") },
                        leadingIcon = { Icon(Icons.Outlined.Person, null, tint = HamsaaPrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HamsaaPrimary,
                            unfocusedBorderColor = LightBorder,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("None") }, onClick = { selectedContactId = null; expanded = false })
                        contacts.forEach { contact ->
                            DropdownMenuItem(text = { Text(contact.name) }, onClick = { selectedContactId = contact.id; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(text = "Add", onClick = { if (title.isNotBlank()) onAdd(title, desc, selectedContactId) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
