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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contactpro.app.TaskNotificationHelper
import com.contactpro.app.data.local.TaskEntity
import com.contactpro.app.network.ApiResult
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.TaskViewModel
import com.contactpro.app.model.ContactResponse
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun TasksScreen(
    userId: Long,
    vm: TaskViewModel = viewModel(),
    contactVm: com.contactpro.app.viewmodel.ContactViewModel = viewModel()
) {
    val tasks by vm.getTasks(userId).collectAsState(initial = emptyList())
    val contactsResult by contactVm.contacts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Notification Permission Handling (Android 13+)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val permissionState = com.google.accompanist.permissions.rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        LaunchedEffect(Unit) {
            if (!permissionState.status.isGranted) {
                permissionState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(userId) { contactVm.loadContacts(userId) }
    LaunchedEffect(Unit) { TaskNotificationHelper.createChannel(context) }

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
                        TaskCard(
                            task, contactName,
                            onToggle = { vm.toggleTaskCompletion(task) },
                            onDelete = {
                                TaskNotificationHelper.cancelNotification(context, task.id)
                                vm.deleteTask(task)
                            }
                        )
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
                onAdd = { title, desc, cId, dueDate ->
                    val task = TaskEntity(
                        title = title,
                        description = desc,
                        dueDate = dueDate,
                        priority = "MEDIUM",
                        status = "PENDING",
                        contactId = cId,
                        userId = userId
                    )
                    vm.addTask(task)

                    // Schedule notification if due date is set
                    if (dueDate != null) {
                        TaskNotificationHelper.scheduleNotification(
                            context, task.createdAt, title, dueDate
                        )
                    }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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

                // Show due date
                if (!task.dueDate.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CalendarToday, null,
                            tint = if (isDueDateOverdue(task.dueDate)) Error else Info,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            formatDueDate(task.dueDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDueDateOverdue(task.dueDate)) Error else Info,
                            fontWeight = FontWeight.Medium
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

private fun isDueDateOverdue(dueDateStr: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dueDate = sdf.parse(dueDateStr) ?: return false
        dueDate.before(java.util.Date())
    } catch (e: Exception) { false }
}

private fun formatDueDate(dueDateStr: String): String {
    return try {
        val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputSdf.parse(dueDateStr) ?: return dueDateStr
        
        // Check if today
        val todaySdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = todaySdf.format(java.util.Date())
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = todaySdf.format(tomorrow.time)
        
        when (dueDateStr) {
            today -> "Due Today"
            tomorrowStr -> "Due Tomorrow"
            else -> "Due: ${outputSdf.format(date)}"
        }
    } catch (e: Exception) { dueDateStr }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    contacts: List<com.contactpro.app.model.ContactResponse>,
    onDismiss: () -> Unit, 
    onAdd: (String, String, Long?, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedContactId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Date Picker state
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        selectedDate = sdf.format(java.util.Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Intelligence Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InputField(title, { title = it }, "Task Title")
                InputField(desc, { desc = it }, "Description (Optional)", singleLine = false, maxLines = 3)
                
                // Date Picker Field
                OutlinedTextField(
                    value = if (selectedDate != null) formatDueDate(selectedDate!!) else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Due Date") },
                    placeholder = { Text("Select a due date") },
                    leadingIcon = { Icon(Icons.Outlined.CalendarToday, null, tint = HamsaaPrimary) },
                    trailingIcon = {
                        if (selectedDate != null) {
                            IconButton(onClick = { selectedDate = null }) {
                                Icon(Icons.Outlined.Close, "Clear", tint = TextHint, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = LightBorder,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = HamsaaPrimary,
                        disabledPlaceholderColor = TextHint
                    )
                )

                // Contact dropdown
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
            PrimaryButton(text = "Add", onClick = { if (title.isNotBlank()) onAdd(title, desc, selectedContactId, selectedDate) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
