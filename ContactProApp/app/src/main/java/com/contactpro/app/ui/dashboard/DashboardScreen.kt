package com.contactpro.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import com.contactpro.app.R
import com.contactpro.app.network.RetrofitClient
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userId: Long,
    onContactClick: (Long) -> Unit,
    vm: DashboardViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(userId) { vm.loadDashboard(userId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.logo_brand),
                            contentDescription = "Logo",
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("HAMSAA", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground, letterSpacing = 2.sp)
                            Text("Intelligence Dashboard", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = rememberCoroutineScope()
                    
                    IconButton(onClick = { 
                        scope.launch {
                            try {
                                RetrofitClient.apiService.resetInteractions(userId)
                                vm.loadDashboard(userId)
                                com.contactpro.app.SyncManager.syncRecentCalls(context, userId)
                                vm.loadDashboard(userId)
                            } catch (e: Exception) {}
                        }
                    }) {
                        Icon(Icons.Outlined.DeleteSweep, "Reset Intelligence", tint = Error)
                    }

                    IconButton(onClick = { 
                        vm.loadDashboard(userId)
                        scope.launch { com.contactpro.app.SyncManager.syncRecentCalls(context, userId) }
                    }) {
                        Icon(Icons.Outlined.Refresh, "Refresh", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingOverlay()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Welcome Section
            Column {
                Text("Welcome Back,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(state.userName, style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            // Stats Cards
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionHeader("Network Analytics")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfessionalStatCard(
                        label = "Total Contacts",
                        value = "${state.totalContacts}",
                        icon  = Icons.Outlined.People,
                        color = HamsaaPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ProfessionalStatCard(
                        label = "Engagements",
                        value = "${state.totalInteractions}",
                        icon  = Icons.Outlined.Analytics,
                        color = Success,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfessionalStatCard(
                        label = "Active Ratio",
                        value = "${if (state.totalContacts > 0) (state.activeContacts * 100 / state.totalContacts) else 0}%",
                        icon  = Icons.Outlined.Bolt,
                        color = Warning,
                        modifier = Modifier.weight(1f)
                    )
                    ProfessionalStatCard(
                        label = "Total Engagement",
                        value = "${"%.1f".format(state.avgDuration)}m",
                        icon  = Icons.Outlined.Timer,
                        color = Info,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Trend Chart
            if (state.interactionTrends.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Interaction Trends")
                    TrendChart(state.interactionTrends)
                }
            }

            // Priority Reminders
            if (state.inactiveContacts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Priority Attention Needed")
                    state.inactiveContacts.take(10).forEach { contact ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onContactClick(contact.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Warning.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PriorityHigh, null, tint = Warning, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contact.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text(getInactiveDaysText(contact), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Outlined.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun getInactiveDaysText(contact: com.contactpro.app.model.ContactResponse): String {
    val dateStr = contact.lastInteractionDate?.take(10) ?: return "No interaction history"
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return "Inactive"
        val diff = java.util.Calendar.getInstance().time.time - date.time
        val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff)
        "Inactive for $days days"
    } catch (e: Exception) {
        "Inactive"
    }
}

@Composable
fun ProfessionalStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border   = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TrendChart(trends: List<com.contactpro.app.model.TrendPoint>) {
    val max = trends.maxOfOrNull { it.value }?.toFloat() ?: 1f
    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            trends.forEach { point ->
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(if (max > 0) (point.value.toFloat() / max.toFloat()).coerceAtLeast(0.05f) else 0.05f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${point.value}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(point.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}
