package com.contactpro.app.ui.followups

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contactpro.app.model.ContactResponse
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.FollowUpGroup
import com.contactpro.app.viewmodel.FollowUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpsScreen(
    userId: Long,
    onContactClick: (Long) -> Unit,
    vm: FollowUpViewModel = viewModel()
) {
    val groups by vm.groups.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    LaunchedEffect(userId) {
        vm.loadFollowUps(userId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Reminders", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { vm.loadFollowUps(userId) }) {
                        Icon(Icons.Outlined.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                LoadingOverlay()
            } else if (groups.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.NotificationsNone,
                    title = "All caught up!",
                    subtitle = "No nodes require attention right now."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groups.forEach { group ->
                        item {
                            GroupHeader(group)
                        }
                        items(when(group) {
                            is FollowUpGroup.Overdue -> group.contacts
                            is FollowUpGroup.Today -> group.contacts
                            is FollowUpGroup.ThisWeek -> group.contacts
                            is FollowUpGroup.Upcoming -> group.contacts
                            is FollowUpGroup.NoHistory -> group.contacts
                        }) { contact ->
                            FollowUpCard(contact, group, onContactClick)
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(group: FollowUpGroup) {
    val (title, color, icon) = when(group) {
        is FollowUpGroup.Overdue -> Triple("Overdue", Error, Icons.Outlined.Error)
        is FollowUpGroup.Today -> Triple("Due Today", Warning, Icons.Outlined.AccessTime)
        is FollowUpGroup.ThisWeek -> Triple("This Week", Success, Icons.Outlined.CalendarMonth)
        is FollowUpGroup.Upcoming -> Triple("Upcoming", Info, Icons.Outlined.EventNote)
        is FollowUpGroup.NoHistory -> Triple("No History", TextHint, Icons.Outlined.HistoryToggleOff)
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FollowUpCard(
    contact: ContactResponse,
    group: FollowUpGroup,
    onContactClick: (Long) -> Unit
) {
    val color = when(group) {
        is FollowUpGroup.Overdue -> Error
        is FollowUpGroup.Today -> Warning
        is FollowUpGroup.ThisWeek -> Success
        is FollowUpGroup.Upcoming -> Info
        is FollowUpGroup.NoHistory -> TextHint
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onContactClick(contact.id) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(contact.name.take(1), color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(contact.category ?: "Uncategorized", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
                        IconButton(onClick = { /* call action */ }) {
                            Icon(Icons.Outlined.Phone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
        }
    }
}
