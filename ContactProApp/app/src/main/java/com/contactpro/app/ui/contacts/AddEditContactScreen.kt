package com.contactpro.app.ui.contacts

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contactpro.app.model.ContactRequest
import com.contactpro.app.model.ContactResponse
import com.contactpro.app.network.ApiResult
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.ContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    userId: Long,
    existingContact: ContactResponse? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    vm: ContactViewModel = viewModel()
) {
    val isEdit = existingContact != null
    var name     by remember { mutableStateOf(existingContact?.name     ?: "") }
    var phone    by remember { mutableStateOf(existingContact?.phone    ?: "") }
    var email    by remember { mutableStateOf(existingContact?.email    ?: "") }
    var category by remember { mutableStateOf(existingContact?.category ?: "") }
    var gender   by remember { mutableStateOf(existingContact?.gender   ?: "Others") }
    var dob      by remember { mutableStateOf(existingContact?.dob      ?: "") }
    var notes    by remember { mutableStateOf("") }
    var followUp by remember { mutableStateOf(existingContact?.followUpFrequency?.toString() ?: "7") }

    var nameError  by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val createState by vm.createState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(createState) {
        when (createState) {
            is ApiResult.Success -> { vm.resetCreateState(); onSaved() }
            is ApiResult.Error   -> {
                snackbarHost.showSnackbar((createState as ApiResult.Error).message)
                vm.resetCreateState()
            }
            else -> Unit
        }
    }

    val genderOptions   = listOf("Male", "Female", "Others")
    val categoryOptions = listOf("Family", "Friend", "Partner", "Client", "Lead", "Other")
    var genderExpanded   by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    fun save() {
        nameError  = if (name.isBlank()) "Name required" else null
        phoneError = if (phone.isBlank()) "Phone required" else null
        if (nameError != null || phoneError != null) return

        val req = ContactRequest(
            name  = name, phone = phone,
            email = email.takeIf { it.isNotBlank() },
            category = category.takeIf { it.isNotBlank() },
            notes    = notes.takeIf { it.isNotBlank() },
            gender   = gender.takeIf { it.isNotBlank() },
            dob      = dob.takeIf { it.isNotBlank() },
            followUpFrequency = followUp.toIntOrNull() ?: 7,
            userId   = userId
        )
        if (isEdit && existingContact != null)
            vm.updateContact(existingContact.id, userId, req)
        else
            vm.createContact(req)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Modify Contact" else "New Contact", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = MaterialTheme.colorScheme.primary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("Basic Details")
                    InputField(name, { name = it; nameError = null }, "Full Name *",
                        leadingIcon = Icons.Outlined.Person, isError = nameError != null, errorMessage = nameError)
                    InputField(phone, { phone = it; phoneError = null }, "Phone Number *",
                        leadingIcon = Icons.Outlined.Phone, isError = phoneError != null, errorMessage = phoneError,
                        keyboardType = KeyboardType.Phone)
                    InputField(email, { email = it }, "Email Address",
                        leadingIcon = Icons.Outlined.Email, keyboardType = KeyboardType.Email)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("Classification & Timing")

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value         = category,
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Category") },
                            leadingIcon   = { Icon(Icons.Outlined.Label, null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                            modifier      = Modifier.fillMaxWidth().menuAnchor(),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            categoryOptions.forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { category = it; categoryExpanded = false })
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = it }
                    ) {
                        OutlinedTextField(
                            value         = gender,
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Gender") },
                            leadingIcon   = { Icon(Icons.Outlined.Transgender, null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded) },
                            modifier      = Modifier.fillMaxWidth().menuAnchor(),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                            genderOptions.forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { gender = it; genderExpanded = false })
                            }
                        }
                    }

                    InputField(followUp, { followUp = it }, "Follow-up Refresh (Days)", 
                        leadingIcon = Icons.Outlined.Refresh, keyboardType = KeyboardType.Number)
                }
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text      = if (isEdit) "Authorize Update" else "Ingest Contact",
                onClick   = ::save,
                modifier  = Modifier.fillMaxWidth(),
                isLoading = createState is ApiResult.Loading,
                icon      = if (isEdit) Icons.Outlined.Save else Icons.Outlined.AddCircle
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
