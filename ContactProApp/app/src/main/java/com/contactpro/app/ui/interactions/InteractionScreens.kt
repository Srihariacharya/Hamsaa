package com.contactpro.app.ui.interactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contactpro.app.network.ApiResult
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.InteractionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInteractionScreen(
    contactId: Long,
    contactName: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    vm: InteractionViewModel = viewModel()
) {
    val types        = listOf("CALL", "MEETING", "EMAIL", "NOTE")
    var selectedType by remember { mutableStateOf("CALL") }
    var notes        by remember { mutableStateOf("") }
    var duration     by remember { mutableStateOf("") }

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

    fun typeIcon(type: String) = when (type) {
        "CALL"    -> Icons.Outlined.Phone
        "MEETING" -> Icons.Outlined.Groups
        "EMAIL"   -> Icons.Outlined.Email
        else      -> Icons.Outlined.NoteAlt
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title  = { Text("Log Intelligence", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = HamsaaPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Context badge
            Card(
                colors = CardDefaults.cardColors(containerColor = HamsaaPrimary.copy(alpha = 0.05f)),
                shape  = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, HamsaaPrimary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Person, null, tint = HamsaaPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Target Node: $contactName",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HamsaaPrimary)
                }
            }

            SectionHeader("Engagement Mode")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                types.forEach { type ->
                    val selected = selectedType == type
                    FilterChip(
                        selected = selected,
                        onClick  = { selectedType = type },
                        label    = { Text(type, fontSize = 12.sp) },
                        leadingIcon = { Icon(typeIcon(type), null, Modifier.size(16.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HamsaaPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = TextSecondary,
                            iconColor = TextHint
                        )
                    )
                }
            }

            SectionHeader("Observations")
            InputField(
                value         = notes,
                onValueChange = { notes = it },
                label         = "Engagement Details",
                leadingIcon   = Icons.Outlined.Notes,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = false,
                maxLines      = 5
            )

            if (selectedType == "CALL" || selectedType == "MEETING") {
                InputField(
                    value         = duration,
                    onValueChange = { duration = it },
                    label         = "Duration (seconds)",
                    leadingIcon   = Icons.Outlined.Timer,
                    keyboardType  = androidx.compose.ui.text.input.KeyboardType.Number
                )
            }

            Spacer(Modifier.weight(1f))
            PrimaryButton(
                text      = "Authorize Log Entry",
                onClick   = {
                    vm.createInteraction(
                        contactId = contactId,
                        type      = selectedType,
                        notes     = notes.takeIf { it.isNotBlank() },
                        duration  = duration.toIntOrNull()
                    )
                },
                modifier  = Modifier.fillMaxWidth(),
                isLoading = createState is ApiResult.Loading,
                icon      = Icons.Outlined.DoneAll
            )
        }
    }
}

@Composable
fun InteractionHistorySection(vm: InteractionViewModel) {
    val state by vm.interactions.collectAsState()
    when (state) {
        is ApiResult.Loading -> {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HamsaaPrimary, modifier = Modifier.size(32.dp))
            }
        }
        is ApiResult.Error -> {
            Text("Intelligence retrieval failure",
                style = MaterialTheme.typography.bodySmall,
                color = Error,
                modifier = Modifier.padding(horizontal = 24.dp))
        }
        is ApiResult.Success -> {
            val list = (state as ApiResult.Success).data
            if (list.isEmpty()) {
                EmptyState(
                    icon     = Icons.Outlined.Forum,
                    title    = "Zero History",
                    subtitle = "No intelligence has been gathered for this node yet."
                )
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    list.forEach { InteractionCard(it) }
                }
            }
        }
    }
}
