package com.farmlab.labfarmis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.farmlab.labfarmis.navigation.Screen
import com.farmlab.labfarmis.ui.components.*
import com.farmlab.labfarmis.ui.theme.*
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val flocks by viewModel.flocks.collectAsState()
    val feedStocks by viewModel.feedStocks.collectAsState()
    val events by viewModel.events.collectAsState()

    val todayEggs = viewModel.getTodayEggs()
    val totalBirds = viewModel.getTotalBirdCount()
    val lowFeedAlerts = viewModel.getLowFeedAlerts()
    val upcomingEvents = viewModel.getUpcomingEvents(7)
    val monthRevenue = viewModel.getMonthlyRevenue()
    val monthExpenses = viewModel.getMonthlyExpenses()

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
    val today = SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH).format(Date())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding() + 16.dp
        )
    ) {
        // ─── Header ───────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(FarmGreenDark, FarmGreen)))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = greeting, style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f))
                    Text(text = "Your Farm", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = today, style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f))
                }
            }
        }

        // ─── Alerts ───────────────────────────────────────────────────────────
        if (lowFeedAlerts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmOrangeContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Warning, null, tint = FarmOrange, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Low Feed Alert", style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold, color = Color(0xFF7C2D12))
                            Text(
                                text = lowFeedAlerts.joinToString(", ") { "${it.name}: ${it.daysRemaining.toInt()}d left" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            }
        }

        // ─── Quick Stats ──────────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Today's Overview")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = "Eggs Today",
                        value = todayEggs.toString(),
                        subtitle = "collected",
                        icon = Icons.Default.Egg,
                        iconTint = FarmYellow,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Birds",
                        value = totalBirds.toString(),
                        subtitle = "${flocks.size} flocks",
                        icon = Icons.Default.Pets,
                        iconTint = FarmGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = "Revenue",
                        value = formatCurrency(monthRevenue),
                        subtitle = "this month",
                        icon = Icons.Default.TrendingUp,
                        iconTint = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Expenses",
                        value = formatCurrency(monthExpenses),
                        subtitle = "this month",
                        icon = Icons.Default.TrendingDown,
                        iconTint = FarmRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ─── Quick Actions ────────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(title = "Quick Access")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(quickActions(navController)) { action ->
                        QuickActionChip(action)
                    }
                }
            }
        }

        // ─── Upcoming Events ──────────────────────────────────────────────────
        if (upcomingEvents.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "Upcoming Events",
                        actionLabel = "View All",
                        onAction = { navController.navigate(Screen.FarmCalendar.route) }
                    )
                    upcomingEvents.take(3).forEach { event ->
                        EventRow(event = event, onToggle = {
                            viewModel.updateEvent(event.copy(isCompleted = !event.isCompleted))
                        })
                    }
                }
            }
        }

        // ─── Flock Overview ───────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = "Flock Overview",
                    actionLabel = if (flocks.isNotEmpty()) "View All" else null,
                    onAction = { navController.navigate(Screen.Flocks.route) }
                )
                if (flocks.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Pets,
                        title = "No flocks added yet",
                        subtitle = "Go to Flocks tab to add your first group"
                    )
                } else {
                    flocks.take(3).forEach { flock ->
                        val health = viewModel.getHealthScore(flock.id)
                        FlockSummaryRow(
                            flock = flock,
                            health = health,
                            onClick = { navController.navigate(Screen.FlockDetail.createRoute(flock.id)) }
                        )
                    }
                }
            }
        }

        // ─── Feed Status ──────────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = "Feed Status",
                    actionLabel = if (feedStocks.isNotEmpty()) "Manage" else null,
                    onAction = { navController.navigate(Screen.FeedManager.route) }
                )
                if (feedStocks.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Grass,
                        title = "No feed stocks added",
                        subtitle = "Track your feed inventory via More → Feed Manager"
                    )
                } else {
                    feedStocks.take(3).forEach { feed ->
                        FeedStatusRow(feed = feed)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun QuickActionChip(action: QuickAction) {
    Card(
        modifier = Modifier
            .width(90.dp)
            .clickable(onClick = action.onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = action.color.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(action.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(action.icon, null, tint = action.color, modifier = Modifier.size(22.dp))
            }
            Text(text = action.label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp,
                maxLines = 1)
        }
    }
}

@Composable
private fun EventRow(event: com.farmlab.labfarmis.data.model.FarmEvent, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = event.type.emoji, fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (event.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface)
                Text(text = formatDate(event.date, "MMM d, yyyy"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Checkbox(checked = event.isCompleted, onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = FarmGreen))
        }
    }
}

@Composable
private fun FlockSummaryRow(
    flock: com.farmlab.labfarmis.data.model.Flock,
    health: com.farmlab.labfarmis.data.model.HealthRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(FarmGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (flock.type.displayName == "Broiler") "🐔" else "🐓", fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(flock.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${flock.count} birds · ${flock.breed} · ${flock.ageWeeks}w",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HealthBadge(health.status)
                Text("${health.score}/100", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FeedStatusRow(feed: com.farmlab.labfarmis.data.model.FeedStock) {
    val daysLeft = feed.daysRemaining
    val color = when {
        daysLeft < 3 -> FarmRed
        daysLeft < 7 -> FarmOrange
        else -> FarmGreen
    }
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🌾", fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(feed.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                LinearProgressIndicator(
                    progress = { (feed.daysRemaining / 30f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        .padding(top = 4.dp),
                    color = color,
                    trackColor = color.copy(alpha = 0.15f)
                )
            }
            Text(
                text = "${daysLeft.toInt()}d",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

private data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)


private fun quickActions(navController: NavController): List<QuickAction> = listOf(
    QuickAction("Farm Map", Icons.Default.Map, FarmGreen) { navController.navigate(Screen.FarmMap.route) },
    QuickAction("Health", Icons.Default.HealthAndSafety, Color(0xFF0EA5E9)) { navController.navigate(Screen.HealthMonitor.route) },
    QuickAction("Photo Check", Icons.Default.CameraAlt, FarmBrown) { navController.navigate(Screen.PhotoCheck.route) },
    QuickAction("Feed", Icons.Default.Grass, FarmYellow.copy(red = 0.8f)) { navController.navigate(Screen.FeedManager.route) },
    QuickAction("Sales", Icons.Default.AttachMoney, Color(0xFF059669)) { navController.navigate(Screen.SalesTracker.route) },
    QuickAction("Reports", Icons.Default.BarChart, Color(0xFF7C3AED)) { navController.navigate(Screen.Reports.route) }
)
