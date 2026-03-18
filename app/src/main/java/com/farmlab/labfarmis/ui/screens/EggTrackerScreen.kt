package com.farmlab.labfarmis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.farmlab.labfarmis.data.model.EggRecord
import com.farmlab.labfarmis.ui.components.*
import com.farmlab.labfarmis.ui.theme.*
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import java.util.UUID

@Composable
fun EggTrackerScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val eggRecords by viewModel.eggRecords.collectAsState()
    val flocks by viewModel.flocks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFlockId by remember { mutableStateOf<String?>(null) }

    val displayRecords = if (selectedFlockId == null) eggRecords
    else eggRecords.filter { it.flockId == selectedFlockId }
    val sorted = displayRecords.sortedByDescending { it.date }

    val totalCollected = sorted.sumOf { it.collected }
    val totalBroken = sorted.sumOf { it.broken }
    val totalSold = sorted.sumOf { it.sold }
    val todayEggs = viewModel.getTodayEggs()

    val chartData = sorted.take(14).reversed().map { it.collected }

    val fabBottomPadding = innerPadding.calculateBottomPadding() + 16.dp

    Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = fabBottomPadding + 72.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(FarmYellow.copy(red = 0.8f, green = 0.6f), FarmYellow)))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Egg Tracker", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Today: $todayEggs eggs collected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }

            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryEggCard("Collected", totalCollected, "🥚", FarmGreen, modifier = Modifier.weight(1f))
                    SummaryEggCard("Broken", totalBroken, "💔", FarmRed, modifier = Modifier.weight(1f))
                    SummaryEggCard("Sold", totalSold, "💰", FarmBrown, modifier = Modifier.weight(1f))
                }
            }

            // Chart
            if (chartData.size >= 3) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Production Trend (14 days)", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            SimpleLineChart(
                                data = chartData,
                                modifier = Modifier.fillMaxWidth().height(130.dp),
                                lineColor = FarmYellow.copy(red = 0.8f, green = 0.65f),
                                fillColor = FarmYellow.copy(alpha = 0.12f)
                            )
                        }
                    }
                }
            }

            // Flock filter
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(title = "Records")
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFlockId == null,
                                onClick = { selectedFlockId = null },
                                label = { Text("All Flocks") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FarmYellow.copy(red = 0.8f),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(flocks) { flock ->
                            FilterChip(
                                selected = selectedFlockId == flock.id,
                                onClick = { selectedFlockId = if (selectedFlockId == flock.id) null else flock.id },
                                label = { Text(flock.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FarmYellow.copy(red = 0.8f),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            if (sorted.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Egg,
                        title = "No egg records",
                        subtitle = "Tap + to record today's collection"
                    )
                }
            } else {
                items(sorted) { record ->
                    val flockName = flocks.find { it.id == record.flockId }?.name ?: "Unknown"
                    EggRecordCard(record = record, flockName = flockName)
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = fabBottomPadding),
            containerColor = FarmYellow,
            contentColor = FarmBrown
        ) {
            Icon(Icons.Default.Add, "Add Record")
        }
    }

    if (showAddDialog) {
        AddEggRecordDialog(
            flocks = flocks,
            onDismiss = { showAddDialog = false },
            onAdd = { record ->
                viewModel.addEggRecord(record)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SummaryEggCard(
    label: String,
    value: Int,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(value.toString(), style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EggRecordCard(record: EggRecord, flockName: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(formatDateTime(record.date), style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text(flockName, style = MaterialTheme.typography.bodySmall,
                        color = FarmGreen)
                }
                Text("🥚", fontSize = 24.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EggStat("Collected", record.collected, FarmGreen)
                EggStat("Broken", record.broken, FarmRed)
                EggStat("Sold", record.sold, FarmBrown)
                EggStat("Kept", record.collected - record.broken - record.sold, Color(0xFF0EA5E9))
            }
            if (record.notes.isNotBlank()) {
                Text(record.notes, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EggStat(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEggRecordDialog(
    flocks: List<com.farmlab.labfarmis.data.model.Flock>,
    onDismiss: () -> Unit,
    onAdd: (EggRecord) -> Unit
) {
    var selectedFlockId by remember { mutableStateOf(flocks.firstOrNull()?.id ?: "") }
    var collected by remember { mutableStateOf("") }
    var broken by remember { mutableStateOf("0") }
    var sold by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val selectedFlock = flocks.find { it.id == selectedFlockId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Egg Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Flock picker
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedFlock?.name ?: "Select Flock",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Flock") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        flocks.forEach { flock ->
                            DropdownMenuItem(
                                text = { Text(flock.name) },
                                onClick = { selectedFlockId = flock.id; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(value = collected, onValueChange = { collected = it.filter { c -> c.isDigit() } },
                    label = { Text("Eggs Collected") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = broken, onValueChange = { broken = it.filter { c -> c.isDigit() } },
                        label = { Text("Broken") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = sold, onValueChange = { sold = it.filter { c -> c.isDigit() } },
                        label = { Text("Sold") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (collected.isNotBlank() && selectedFlockId.isNotBlank()) {
                        onAdd(EggRecord(
                            id = UUID.randomUUID().toString(),
                            flockId = selectedFlockId,
                            collected = collected.toIntOrNull() ?: 0,
                            broken = broken.toIntOrNull() ?: 0,
                            sold = sold.toIntOrNull() ?: 0,
                            notes = notes
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
