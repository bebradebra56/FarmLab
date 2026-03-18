package com.farmlab.labfarmis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.farmlab.labfarmis.navigation.Screen
import com.farmlab.labfarmis.ui.components.*
import com.farmlab.labfarmis.ui.theme.*
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import java.util.UUID

@Composable
fun FlockListScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val flocks by viewModel.flocks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf<FlockType?>(null) }

    val filtered = if (filterType == null) flocks else flocks.filter { it.type == filterType }
    val fabBottomPadding = innerPadding.calculateBottomPadding() + 16.dp

    Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = fabBottomPadding + 72.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(FarmGreen, FarmGreenLight)))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Flocks", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${flocks.size} groups · ${flocks.sumOf { it.count }} birds total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            // Filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterType == null,
                        onClick = { filterType = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FarmGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                    FlockType.values().forEach { type ->
                        FilterChip(
                            selected = filterType == type,
                            onClick = { filterType = if (filterType == type) null else type },
                            label = { Text(type.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FarmGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Pets,
                        title = "No flocks yet",
                        subtitle = "Tap + to add your first flock"
                    )
                }
            } else {
                items(filtered) { flock ->
                    val health = viewModel.getHealthScore(flock.id)
                    FlockCard(
                        flock = flock,
                        health = health,
                        onClick = { navController.navigate(Screen.FlockDetail.createRoute(flock.id)) },
                        onDelete = { viewModel.deleteFlock(flock.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = fabBottomPadding),
            containerColor = FarmGreen,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Flock")
        }
    }

    if (showAddDialog) {
        AddFlockDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { flock ->
                viewModel.addFlock(flock)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FlockCard(
    flock: Flock,
    health: com.farmlab.labfarmis.data.model.HealthRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                    .background(
                        when (flock.type) {
                            FlockType.LAYER -> FarmGreen.copy(alpha = 0.12f)
                            FlockType.BROILER -> FarmBrown.copy(alpha = 0.12f)
                            else -> FarmYellow.copy(alpha = 0.12f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (flock.type) {
                        FlockType.LAYER -> "🐓"
                        FlockType.BROILER -> "🐔"
                        FlockType.TURKEY -> "🦃"
                        FlockType.DUCK -> "🦆"
                        FlockType.BREEDER -> "🥚"
                    },
                    fontSize = 28.sp
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(flock.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${flock.count} birds · ${flock.breed}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Age: ${flock.ageWeeks} weeks · ${flock.type.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthBadge(health.status)
                    Text("Score: ${health.score}/100",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Delete
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.DeleteOutline, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Flock") },
            text = { Text("Remove \"${flock.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = FarmRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AddFlockDialog(onDismiss: () -> Unit, onAdd: (Flock) -> Unit) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var ageWeeks by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FlockType.LAYER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Flock", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Flock Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = breed, onValueChange = { breed = it },
                    label = { Text("Breed") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = count, onValueChange = { count = it.filter { c -> c.isDigit() } },
                        label = { Text("Count") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = ageWeeks, onValueChange = { ageWeeks = it.filter { c -> c.isDigit() } },
                        label = { Text("Age (weeks)") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                // Type selector
                Text("Type", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FlockType.values().take(3).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FarmGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)") }, maxLines = 2,
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && count.isNotBlank()) {
                        onAdd(
                            Flock(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                type = selectedType,
                                breed = breed.ifBlank { "Unknown" },
                                count = count.toIntOrNull() ?: 0,
                                ageWeeks = ageWeeks.toIntOrNull() ?: 0,
                                notes = notes
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
