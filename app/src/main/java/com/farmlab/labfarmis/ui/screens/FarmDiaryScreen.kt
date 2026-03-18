package com.farmlab.labfarmis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.farmlab.labfarmis.data.model.DiaryEntry
import com.farmlab.labfarmis.data.model.FarmMood
import com.farmlab.labfarmis.ui.components.EmptyState
import com.farmlab.labfarmis.ui.components.SectionHeader
import com.farmlab.labfarmis.ui.components.StatCard
import com.farmlab.labfarmis.ui.components.formatDateTime
import com.farmlab.labfarmis.ui.theme.FarmBrown
import com.farmlab.labfarmis.ui.theme.FarmGreen
import com.farmlab.labfarmis.ui.theme.FarmOrange
import com.farmlab.labfarmis.ui.theme.FarmRed
import com.farmlab.labfarmis.ui.theme.FarmYellow
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import java.util.UUID

@Composable
fun FarmDiaryScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var expandedEntry by remember { mutableStateOf<String?>(null) }

    val sorted = diaryEntries.sortedByDescending { it.date }
    val totalEggsLogged = sorted.sumOf { it.eggsCollected }
    val avgMood = sorted.map { it.mood.ordinal }.average().toFloat()

    val fabBottomPadding = innerPadding.calculateBottomPadding() + 16.dp

    Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = fabBottomPadding + 72.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF0C4A6E), Color(0xFF0891B2))))
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Farm Diary", style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${sorted.size} entries · ${totalEggsLogged} eggs logged",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f))
                        }
                        Text("📖", fontSize = 32.sp)
                    }
                }
            }

            // Summary cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("Entries", "${sorted.size}", "total logged",
                        Icons.Default.MenuBook, Color(0xFF0891B2), modifier = Modifier.weight(1f))
                    StatCard("Avg Mood", FarmMood.values().getOrNull(avgMood.toInt())?.emoji ?: "😊",
                        "overall",
                        Icons.Default.Mood, FarmYellow.copy(red = 0.7f), modifier = Modifier.weight(1f))
                    StatCard("Best Day", "${if (sorted.isNotEmpty()) sorted.maxOf { it.eggsCollected } else 0}",
                        "eggs in one day",
                        Icons.Default.Egg, FarmGreen, modifier = Modifier.weight(1f))
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SectionHeader(title = "Journal Entries")
                }
            }

            if (sorted.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.MenuBook,
                        title = "No diary entries",
                        subtitle = "Start documenting your farm day by day"
                    )
                }
            } else {
                items(sorted) { entry ->
                    DiaryEntryCard(
                        entry = entry,
                        isExpanded = expandedEntry == entry.id,
                        onClick = {
                            expandedEntry = if (expandedEntry == entry.id) null else entry.id
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = fabBottomPadding),
            containerColor = Color(0xFF0891B2),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, "Add Entry")
        }
    }

    if (showAddDialog) {
        AddDiaryEntryDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { entry ->
                viewModel.addDiaryEntry(entry)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DiaryEntryCard(entry: DiaryEntry, isExpanded: Boolean, onClick: () -> Unit) {
    val moodColor = when (entry.mood) {
        FarmMood.GREAT -> Color(0xFF059669)
        FarmMood.GOOD -> FarmGreen
        FarmMood.NEUTRAL -> FarmYellow.copy(red = 0.7f)
        FarmMood.BAD -> FarmRed
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                            .background(moodColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(entry.mood.emoji, fontSize = 24.sp)
                    }
                    Column {
                        Text(formatDateTime(entry.date), style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold)
                        Text(entry.mood.displayName, style = MaterialTheme.typography.bodySmall,
                            color = moodColor)
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DiaryStatPill("🥚 ${entry.eggsCollected}", "eggs")
                DiaryStatPill("🌾 ${entry.feedUsedKg.toInt()}kg", "feed")
            }

            // Tasks preview
            if (entry.tasks.isNotBlank()) {
                Text(
                    text = if (isExpanded) entry.tasks else
                        entry.tasks.take(60) + if (entry.tasks.length > 60) "…" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded content
            if (isExpanded) {
                if (entry.problems.isNotBlank()) {
                    HorizontalDivider()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Warning, null, tint = FarmOrange, modifier = Modifier.size(16.dp))
                        Column {
                            Text("Problems", style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold, color = FarmOrange)
                            Text(entry.problems, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (entry.notes.isNotBlank()) {
                    HorizontalDivider()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Notes, null, tint = FarmBrown, modifier = Modifier.size(16.dp))
                        Column {
                            Text("Notes", style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold, color = FarmBrown)
                            Text(entry.notes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryStatPill(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AddDiaryEntryDialog(onDismiss: () -> Unit, onAdd: (DiaryEntry) -> Unit) {
    var eggsCollected by remember { mutableStateOf("") }
    var feedUsedKg by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf("") }
    var problems by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(FarmMood.GOOD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Diary Entry", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Mood selector
                Text("How was your day?", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FarmMood.values().forEach { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { selectedMood = mood },
                            label = { Text("${mood.emoji} ${mood.displayName}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (mood) {
                                    FarmMood.GREAT -> Color(0xFF059669)
                                    FarmMood.GOOD -> FarmGreen
                                    FarmMood.NEUTRAL -> FarmYellow.copy(red = 0.7f)
                                    FarmMood.BAD -> FarmRed
                                },
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = eggsCollected,
                        onValueChange = { eggsCollected = it.filter { c -> c.isDigit() } },
                        label = { Text("Eggs Collected") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = feedUsedKg,
                        onValueChange = { feedUsedKg = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Feed (kg)") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = tasks, onValueChange = { tasks = it },
                    label = { Text("Tasks Done") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = problems, onValueChange = { problems = it },
                    label = { Text("Problems (if any)") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(DiaryEntry(
                        id = UUID.randomUUID().toString(),
                        eggsCollected = eggsCollected.toIntOrNull() ?: 0,
                        feedUsedKg = feedUsedKg.toFloatOrNull() ?: 0f,
                        tasks = tasks, problems = problems, notes = notes,
                        mood = selectedMood
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0891B2))
            ) { Text("Save Entry") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
