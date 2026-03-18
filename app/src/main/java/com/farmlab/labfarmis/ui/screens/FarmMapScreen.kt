package com.farmlab.labfarmis.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.farmlab.labfarmis.data.model.*
import com.farmlab.labfarmis.navigation.Screen
import com.farmlab.labfarmis.ui.components.HealthBadge
import com.farmlab.labfarmis.ui.theme.*
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import java.util.UUID

@Composable
fun FarmMapScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val zones by viewModel.farmZones.collectAsState()
    val flocks by viewModel.flocks.collectAsState()
    var selectedZone by remember { mutableStateOf<FarmZone?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
    ) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(FarmGreenDark, FarmGreen)))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Farm Map", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${zones.size} zones", style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f))
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        }

        // Map canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(340.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(zones) {
                            detectTapGestures { tapOffset ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val tapped = zones.lastOrNull { zone ->
                                    val zx = zone.x * w
                                    val zy = zone.y * h
                                    val zw = zone.width * w
                                    val zh = zone.height * h
                                    tapOffset.x in zx..(zx + zw) && tapOffset.y in zy..(zy + zh)
                                }
                                selectedZone = if (selectedZone?.id == tapped?.id) null else tapped
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Background grid
                    drawRect(Color(0xFFF0F7EE))
                    val gridColor = Color(0xFFD0E8CC)
                    val gridStep = 40.dp.toPx()
                    var x = 0f
                    while (x < w) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 0.5f)
                        x += gridStep
                    }
                    var y = 0f
                    while (y < h) {
                        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.5f)
                        y += gridStep
                    }

                    // Draw zones
                    zones.forEach { zone ->
                        val zx = zone.x * w
                        val zy = zone.y * h
                        val zw = zone.width * w
                        val zh = zone.height * h
                        val zoneColor = Color(zone.colorHex.toInt())
                        val isSelected = selectedZone?.id == zone.id

                        // Zone fill
                        drawRoundRect(
                            color = zoneColor.copy(alpha = if (isSelected) 0.45f else 0.25f),
                            topLeft = Offset(zx, zy),
                            size = Size(zw, zh),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                        )

                        // Zone border
                        drawRoundRect(
                            color = zoneColor,
                            topLeft = Offset(zx, zy),
                            size = Size(zw, zh),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                            style = Stroke(width = if (isSelected) 3.dp.toPx() else 1.5f.dp.toPx())
                        )

                        // Zone label
                        val textResult = textMeasurer.measure(
                            text = zone.type.emoji + " " + zone.name,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(zone.colorHex.toInt()).copy(alpha = 0.9f)
                            )
                        )
                        drawText(
                            textLayoutResult = textResult,
                            topLeft = Offset(
                                x = zx + (zw - textResult.size.width) / 2f,
                                y = zy + (zh - textResult.size.height) / 2f
                            )
                        )
                    }
                }

                // Legend overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Tap zone for details", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        // Selected zone info
        selectedZone?.let { zone ->
            val linkedFlock = flocks.find { it.id == zone.flockId }
            val health = linkedFlock?.let { viewModel.getHealthScore(it.id) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(zone.type.emoji, fontSize = 28.sp)
                            Column {
                                Text(zone.name, style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                Text(zone.type.displayName, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (linkedFlock != null) {
                                TextButton(onClick = {
                                    navController.navigate(Screen.FlockDetail.createRoute(linkedFlock.id))
                                }) { Text("View Flock", color = FarmGreen) }
                            }
                            IconButton(onClick = {
                                viewModel.deleteFarmZone(zone.id)
                                selectedZone = null
                            }) {
                                Icon(Icons.Default.DeleteOutline, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (linkedFlock != null) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("🐓 ${linkedFlock.name}", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold)
                                Text("${linkedFlock.count} birds · ${linkedFlock.ageWeeks}w · ${linkedFlock.breed}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            health?.let { HealthBadge(it.status) }
                        }
                    }
                    if (zone.notes.isNotBlank()) {
                        Text(zone.notes, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Zone list
        if (selectedZone == null) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Text("All Zones", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp))
                }
                items(zones) { zone ->
                    val linkedFlock = flocks.find { it.id == zone.flockId }
                    ZoneListItem(zone = zone, linkedFlock = linkedFlock,
                        onClick = { selectedZone = zone })
                }
            }
        } else {
            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 16.dp))
        }
    }

    if (showAddDialog) {
        AddZoneDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { zone ->
                viewModel.addFarmZone(zone)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ZoneListItem(zone: FarmZone, linkedFlock: com.farmlab.labfarmis.data.model.Flock?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(zone.type.emoji, fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (linkedFlock != null) "${zone.type.displayName} · ${linkedFlock.name}"
                    else zone.type.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddZoneDialog(onDismiss: () -> Unit, onAdd: (FarmZone) -> Unit) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ZoneType.COOP) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Zone", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Zone Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = "${selectedType.emoji} ${selectedType.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Zone Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ZoneType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text("${type.emoji} ${type.displayName}") },
                                onClick = { selectedType = type; expanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val rng = java.util.Random()
                        onAdd(FarmZone(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            type = selectedType,
                            x = 0.05f + rng.nextFloat() * 0.5f,
                            y = 0.05f + rng.nextFloat() * 0.5f,
                            width = 0.2f + rng.nextFloat() * 0.15f,
                            height = 0.15f + rng.nextFloat() * 0.1f,
                            notes = notes,
                            colorHex = when (selectedType) {
                                ZoneType.COOP -> 0xFF2F855AL
                                ZoneType.PASTURE -> 0xFF68D391L
                                ZoneType.INCUBATOR -> 0xFFED8936L
                                ZoneType.FEED_STORAGE -> 0xFFF6C453L
                                ZoneType.GREENHOUSE -> 0xFF48BB78L
                                ZoneType.WATER_SOURCE -> 0xFF63B3EDL
                                ZoneType.OTHER -> 0xFF8B5E3CL
                            }
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
