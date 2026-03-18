package com.farmlab.labfarmis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.farmlab.labfarmis.data.model.EventType
import com.farmlab.labfarmis.data.model.FarmEvent
import com.farmlab.labfarmis.ui.components.formatDate
import com.farmlab.labfarmis.ui.components.formatDateTime
import com.farmlab.labfarmis.ui.theme.FarmBrown
import com.farmlab.labfarmis.ui.theme.FarmGreen
import com.farmlab.labfarmis.ui.theme.FarmGreenContainer
import com.farmlab.labfarmis.ui.theme.FarmGreenDark
import com.farmlab.labfarmis.ui.theme.FarmOrange
import com.farmlab.labfarmis.ui.theme.FarmYellow
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@Composable
fun FarmCalendarScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val events by viewModel.events.collectAsState()
    val flocks by viewModel.flocks.collectAsState()
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    var displayMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var displayYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    val monthName = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(
        Calendar.getInstance().apply {
            set(Calendar.MONTH, displayMonth)
            set(Calendar.YEAR, displayYear)
        }.time
    )

    val eventsOnDate: (Long) -> List<FarmEvent> = { date ->
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        events.filter { event ->
            val eCal = Calendar.getInstance().apply { timeInMillis = event.date }
            eCal.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH) &&
                    eCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                    eCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
        }
    }

    val selectedEvents = selectedDate?.let { eventsOnDate(it) }
        ?: events.sortedBy { it.date }.take(10)

    val fabBottomPadding = innerPadding.calculateBottomPadding() + 16.dp

    Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = fabBottomPadding + 72.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6))))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Farm Calendar", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${events.filter { !it.isCompleted }.size} pending events",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            // Month navigation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                if (displayMonth == 0) { displayMonth = 11; displayYear-- }
                                else displayMonth--
                            }) {
                                Icon(Icons.Default.ChevronLeft, null)
                            }
                            Text(monthName, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                if (displayMonth == 11) { displayMonth = 0; displayYear++ }
                                else displayMonth++
                            }) {
                                Icon(Icons.Default.ChevronRight, null)
                            }
                        }

                        // Day of week headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                                Text(day, modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Calendar grid
                        val firstDayOfMonth = Calendar.getInstance().apply {
                            set(Calendar.MONTH, displayMonth)
                            set(Calendar.YEAR, displayYear)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                        val daysInMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
                        val today = Calendar.getInstance()

                        val totalCells = startDayOfWeek + daysInMonth
                        val rows = (totalCells + 6) / 7

                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0 until 7) {
                                    val dayIdx = row * 7 + col
                                    val day = dayIdx - startDayOfWeek + 1
                                    if (day < 1 || day > daysInMonth) {
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    } else {
                                        val dayDate = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, displayYear)
                                            set(Calendar.MONTH, displayMonth)
                                            set(Calendar.DAY_OF_MONTH, day)
                                            set(Calendar.HOUR_OF_DAY, 12)
                                        }.timeInMillis

                                        val dayEvents = eventsOnDate(dayDate)
                                        val isToday = today.get(Calendar.DAY_OF_MONTH) == day &&
                                                today.get(Calendar.MONTH) == displayMonth &&
                                                today.get(Calendar.YEAR) == displayYear
                                        val isSelected = selectedDate?.let { sd ->
                                            val sdCal = Calendar.getInstance().apply { timeInMillis = sd }
                                            sdCal.get(Calendar.DAY_OF_MONTH) == day &&
                                                    sdCal.get(Calendar.MONTH) == displayMonth &&
                                                    sdCal.get(Calendar.YEAR) == displayYear
                                        } ?: false

                                        CalendarDay(
                                            day = day,
                                            isToday = isToday,
                                            isSelected = isSelected,
                                            hasEvents = dayEvents.isNotEmpty(),
                                            eventColors = dayEvents.take(3).map {
                                                when (it.type) {
                                                    EventType.VACCINATION -> Color(0xFFEC4899)
                                                    EventType.CLEANING -> Color(0xFF06B6D4)
                                                    EventType.FEED_CHANGE -> FarmYellow.copy(red = 0.8f)
                                                    EventType.EGG_COLLECTION -> FarmGreen
                                                    EventType.HEALTH_CHECK -> Color(0xFF8B5CF6)
                                                    EventType.TRANSFER -> FarmOrange
                                                    EventType.OTHER -> FarmBrown
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                selectedDate = if (isSelected) null else dayDate
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Events list
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    val label = if (selectedDate != null)
                        "Events on ${formatDate(selectedDate!!, "MMM d, yyyy")}"
                    else "All Upcoming Events"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        if (selectedDate != null) {
                            TextButton(onClick = { selectedDate = null }) {
                                Text("Show All", color = FarmGreen)
                            }
                        }
                    }
                }
            }

            if (selectedEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No events on this date", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(selectedEvents) { event ->
                    val flockName = flocks.find { it.id == event.flockId }?.name
                    CalendarEventCard(
                        event = event,
                        flockName = flockName,
                        onToggle = { viewModel.updateEvent(event.copy(isCompleted = !event.isCompleted)) },
                        onDelete = { viewModel.deleteEvent(event.id) }
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
            Icon(Icons.Default.Add, "Add Event")
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            flocks = flocks,
            selectedDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onAdd = { event ->
                viewModel.addEvent(event)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvents: Boolean,
    eventColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.aspectRatio(1f).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(
                    when {
                        isSelected -> FarmGreen
                        isToday -> FarmGreenContainer
                        else -> Color.Transparent
                    }
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$day",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> Color.White
                        isToday -> FarmGreenDark
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            if (hasEvents) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    eventColors.take(3).forEach { color ->
                        Box(
                            modifier = Modifier.size(4.dp).clip(CircleShape).background(color)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarEventCard(
    event: FarmEvent,
    flockName: String?,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = when (event.type) {
        EventType.VACCINATION -> Color(0xFFEC4899)
        EventType.CLEANING -> Color(0xFF06B6D4)
        EventType.FEED_CHANGE -> FarmYellow.copy(red = 0.8f)
        EventType.EGG_COLLECTION -> FarmGreen
        EventType.HEALTH_CHECK -> Color(0xFF8B5CF6)
        EventType.TRANSFER -> FarmOrange
        EventType.OTHER -> FarmBrown
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (event.isCompleted) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.width(4.dp).height(48.dp).clip(RoundedCornerShape(2.dp))
                    .background(if (event.isCompleted) MaterialTheme.colorScheme.outline else typeColor)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${event.type.emoji} ${event.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (event.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    buildString {
                        append(formatDateTime(event.date))
                        if (flockName != null) append(" · $flockName")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (event.notes.isNotBlank()) {
                    Text(event.notes, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Checkbox(
                    checked = event.isCompleted, onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = FarmGreen)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEventDialog(
    flocks: List<com.farmlab.labfarmis.data.model.Flock>,
    selectedDate: Long?,
    onDismiss: () -> Unit,
    onAdd: (FarmEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EventType.OTHER) }
    var selectedFlockId by remember { mutableStateOf<String?>(null) }
    var typeExpanded by remember { mutableStateOf(false) }
    var flockExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Event", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text("Event Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                    OutlinedTextField(
                        value = "${selectedType.emoji} ${selectedType.displayName}",
                        onValueChange = {}, readOnly = true, label = { Text("Event Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        EventType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text("${type.emoji} ${type.displayName}") },
                                onClick = { selectedType = type; typeExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = flockExpanded, onExpandedChange = { flockExpanded = !flockExpanded }) {
                    OutlinedTextField(
                        value = flocks.find { it.id == selectedFlockId }?.name ?: "No specific flock",
                        onValueChange = {}, readOnly = true, label = { Text("Linked Flock (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(flockExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = flockExpanded, onDismissRequest = { flockExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("No specific flock") },
                            onClick = { selectedFlockId = null; flockExpanded = false }
                        )
                        flocks.forEach { flock ->
                            DropdownMenuItem(
                                text = { Text(flock.name) },
                                onClick = { selectedFlockId = flock.id; flockExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)") }, maxLines = 2, modifier = Modifier.fillMaxWidth())

                if (selectedDate != null) {
                    Text("Date: ${formatDateTime(selectedDate)}",
                        style = MaterialTheme.typography.bodySmall, color = FarmGreen)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(FarmEvent(
                            id = UUID.randomUUID().toString(),
                            title = title, type = selectedType,
                            date = selectedDate ?: System.currentTimeMillis(),
                            flockId = selectedFlockId, notes = notes
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
