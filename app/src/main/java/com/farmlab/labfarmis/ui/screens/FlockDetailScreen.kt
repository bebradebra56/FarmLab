package com.farmlab.labfarmis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.farmlab.labfarmis.data.model.*
import com.farmlab.labfarmis.ui.components.*
import com.farmlab.labfarmis.ui.theme.*
import com.farmlab.labfarmis.viewmodel.FarmViewModel

@Composable
fun FlockDetailScreen(
    flockId: String,
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val flocks by viewModel.flocks.collectAsState()
    val eggRecords by viewModel.eggRecords.collectAsState()
    val flock = flocks.find { it.id == flockId } ?: return

    val health = viewModel.getHealthScore(flockId)
    val flockEggs = eggRecords.filter { it.flockId == flockId }
        .sortedByDescending { it.date }.take(14)
    val forecast = viewModel.predictEggProduction(flockId, 7)
    val recentEggs = flockEggs.take(7).reversed()

    var showEditDialog by remember { mutableStateOf(false) }

    val headerColor = when (flock.type) {
        FlockType.LAYER -> listOf(FarmGreen, FarmGreenLight)
        FlockType.BROILER -> listOf(FarmBrown, FarmBrownLight)
        FlockType.TURKEY -> listOf(Color(0xFF92400E), Color(0xFFB45309))
        FlockType.DUCK -> listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6))
        FlockType.BREEDER -> listOf(FarmGreenDark, FarmGreen)
    }

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
                    .background(Brush.linearGradient(headerColor))
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Box(
                        modifier = Modifier.size(60.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (flock.type) {
                                FlockType.LAYER -> "🐓"; FlockType.BROILER -> "🐔"
                                FlockType.TURKEY -> "🦃"; FlockType.DUCK -> "🦆"
                                FlockType.BREEDER -> "🥚"
                            },
                            fontSize = 30.sp
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(flock.name, style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${flock.type.displayName} · ${flock.breed}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f))
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, null, tint = Color.White)
                    }
                }
            }
        }

        // ─── Key Stats ────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("Birds", "${flock.count}", "total count",
                    Icons.Default.Pets, FarmGreen, modifier = Modifier.weight(1f))
                StatCard("Age", "${flock.ageWeeks}w", "weeks old",
                    Icons.Default.Schedule, FarmBrown, modifier = Modifier.weight(1f))
                StatCard("Health", "${health.score}", "/ 100",
                    Icons.Default.Favorite,
                    when (health.status) {
                        HealthStatus.HEALTHY -> Color(0xFF059669)
                        HealthStatus.WARNING -> FarmOrange
                        HealthStatus.RISK -> FarmRed
                    },
                    modifier = Modifier.weight(1f))
            }
        }

        // ─── Health Card ──────────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (health.status) {
                        HealthStatus.HEALTHY -> Color(0xFFD1FAE5)
                        HealthStatus.WARNING -> FarmOrangeContainer
                        HealthStatus.RISK -> FarmRedContainer
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Health Index", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold)
                        HealthBadge(health.status)
                    }
                    LinearProgressIndicator(
                        progress = { health.score / 100f },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = when (health.status) {
                            HealthStatus.HEALTHY -> Color(0xFF059669)
                            HealthStatus.WARNING -> FarmOrange
                            HealthStatus.RISK -> FarmRed
                        },
                        trackColor = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = when (health.status) {
                            HealthStatus.HEALTHY -> "Flock is performing well. Keep up current management."
                            HealthStatus.WARNING -> "Some indicators need attention. Monitor closely."
                            HealthStatus.RISK -> "Immediate attention required. Check for disease or stress."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // ─── Production Rate ──────────────────────────────────────────────────
        if (flock.type == FlockType.LAYER || flock.type == FlockType.BREEDER) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Production Rate", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        val productionPct = (health.eggProductionRate * 100).toInt().coerceIn(0, 100)
                        LabeledProgressBar(
                            label = "Egg Laying Rate",
                            value = productionPct.toFloat(),
                            maxValue = 100f,
                            color = FarmGreen
                        )
                    }
                }
            }

            // Recent Egg Chart
            if (recentEggs.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Egg Production (7 days)", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            SimpleLineChart(
                                data = recentEggs.map { it.collected },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                lineColor = FarmGreen
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(formatDate(recentEggs.first().date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatDate(recentEggs.last().date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Forecast
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmGreenContainer),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.TrendingUp, null, tint = FarmGreenDark, modifier = Modifier.size(20.dp))
                            Text("7-Day Forecast", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = FarmGreenDark)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            forecast.forEachIndexed { i, eggs ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Day ${i + 1}", style = MaterialTheme.typography.labelSmall,
                                        color = FarmGreenDark.copy(alpha = 0.7f), fontSize = 9.sp)
                                    Text("$eggs", style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold, color = FarmGreenDark)
                                }
                            }
                        }
                        Text("Avg: ${forecast.average().toInt()} eggs/day · Total: ${forecast.sum()} eggs",
                            style = MaterialTheme.typography.bodySmall, color = FarmGreenDark.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // ─── Egg History ──────────────────────────────────────────────────────
        if (flockEggs.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Recent Records", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                }
            }
            items(flockEggs.take(7)) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🥚", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(formatDateTime(record.date), style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${record.collected}", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = FarmGreen)
                                Text("Collected", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${record.broken}", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = FarmRed)
                                Text("Broken", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${record.sold}", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = FarmBrown)
                                Text("Sold", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        // ─── Notes ────────────────────────────────────────────────────────────
        if (flock.notes.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Notes, null, tint = FarmBrown)
                        Text(flock.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditFlockDialog(
            flock = flock,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.updateFlock(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun EditFlockDialog(flock: Flock, onDismiss: () -> Unit, onSave: (Flock) -> Unit) {
    var name by remember { mutableStateOf(flock.name) }
    var breed by remember { mutableStateOf(flock.breed) }
    var count by remember { mutableStateOf(flock.count.toString()) }
    var ageWeeks by remember { mutableStateOf(flock.ageWeeks.toString()) }
    var notes by remember { mutableStateOf(flock.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Flock", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Flock Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = breed, onValueChange = { breed = it },
                    label = { Text("Breed") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = count, onValueChange = { count = it.filter { c -> c.isDigit() } },
                        label = { Text("Count") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = ageWeeks, onValueChange = { ageWeeks = it.filter { c -> c.isDigit() } },
                        label = { Text("Age (weeks)") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(flock.copy(
                    name = name.ifBlank { flock.name },
                    breed = breed.ifBlank { flock.breed },
                    count = count.toIntOrNull() ?: flock.count,
                    ageWeeks = ageWeeks.toIntOrNull() ?: flock.ageWeeks,
                    notes = notes
                ))
            }, colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
