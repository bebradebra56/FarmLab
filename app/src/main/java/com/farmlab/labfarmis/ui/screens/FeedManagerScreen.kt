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
import androidx.navigation.NavController
import com.farmlab.labfarmis.data.model.FeedStock
import com.farmlab.labfarmis.data.model.FeedType
import com.farmlab.labfarmis.ui.components.*
import com.farmlab.labfarmis.ui.theme.*
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import java.util.UUID

@Composable
fun FeedManagerScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val feedStocks by viewModel.feedStocks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var restockTarget by remember { mutableStateOf<FeedStock?>(null) }

    val totalFeedKg = feedStocks.sumOf { it.quantityKg.toDouble() }.toFloat()
    val totalFeedValue = feedStocks.sumOf { it.totalValue.toDouble() }.toFloat()
    val lowStockFeeds = feedStocks.filter { it.daysRemaining < 7f }

    val fabBottomPadding = innerPadding.calculateBottomPadding() + 16.dp

    Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = fabBottomPadding + 72.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF92400E), FarmBrown)))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Feed Manager", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${feedStocks.size} types · ${totalFeedKg.toInt()} kg total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f))
                        Text("Stock value: ${formatCurrency(totalFeedValue)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            // Alerts
            if (lowStockFeeds.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = FarmRedContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Warning, null, tint = FarmRed, modifier = Modifier.size(18.dp))
                                Text("Low Stock Alert", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            }
                            lowStockFeeds.forEach { feed ->
                                Text(
                                    "• ${feed.name}: ${feed.daysRemaining.toInt()} days remaining",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionHeader(title = "Feed Inventory")
                }
            }

            items(feedStocks) { feed ->
                FeedCard(
                    feed = feed,
                    onRestock = { restockTarget = feed },
                    onDelete = { viewModel.deleteFeedStock(feed.id) }
                )
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
            Icon(Icons.Default.Add, "Add Feed")
        }
    }

    if (showAddDialog) {
        AddFeedDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { stock ->
                viewModel.addFeedStock(stock)
                showAddDialog = false
            }
        )
    }

    restockTarget?.let { feed ->
        RestockDialog(
            feed = feed,
            onDismiss = { restockTarget = null },
            onRestock = { addedKg ->
                viewModel.updateFeedStock(feed.copy(
                    quantityKg = feed.quantityKg + addedKg,
                    lastRestocked = System.currentTimeMillis()
                ))
                restockTarget = null
            }
        )
    }
}

@Composable
private fun FeedCard(feed: FeedStock, onRestock: () -> Unit, onDelete: () -> Unit) {
    val daysLeft = feed.daysRemaining
    val statusColor = when {
        daysLeft < 3 -> FarmRed
        daysLeft < 7 -> FarmOrange
        daysLeft < 14 -> FarmYellow.copy(red = 0.8f)
        else -> FarmGreen
    }
    val progress = (daysLeft / 30f).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(feed.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(feed.type.displayName, style = MaterialTheme.typography.bodySmall, color = FarmBrown)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onRestock, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.AddCircleOutline, "Restock", tint = FarmGreen, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.DeleteOutline, "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("${feed.quantityKg.toInt()} kg", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = statusColor)
                    Text("in stock", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${daysLeft.toInt()} days", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = statusColor)
                    Text("remaining", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatCurrency(feed.totalValue), style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text("stock value", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.15f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Daily: ${feed.dailyConsumptionKg} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${formatCurrency(feed.pricePerKg)}/kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Supplier: ${feed.supplier}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFeedDialog(onDismiss: () -> Unit, onAdd: (FeedStock) -> Unit) {
    var name by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var quantityKg by remember { mutableStateOf("") }
    var dailyKg by remember { mutableStateOf("") }
    var priceKg by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FeedType.LAYER_FEED) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Feed Stock", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Feed Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Feed Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        FeedType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = { selectedType = type; expanded = false }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantityKg, onValueChange = { quantityKg = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Qty (kg)") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = dailyKg, onValueChange = { dailyKg = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Daily (kg)") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = priceKg, onValueChange = { priceKg = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Price/kg") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = supplier, onValueChange = { supplier = it },
                        label = { Text("Supplier") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && quantityKg.isNotBlank()) {
                        onAdd(FeedStock(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            type = selectedType,
                            quantityKg = quantityKg.toFloatOrNull() ?: 0f,
                            dailyConsumptionKg = dailyKg.toFloatOrNull() ?: 1f,
                            pricePerKg = priceKg.toFloatOrNull() ?: 0f,
                            supplier = supplier
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RestockDialog(feed: FeedStock, onDismiss: () -> Unit, onRestock: (Float) -> Unit) {
    var addKg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restock ${feed.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current stock: ${feed.quantityKg.toInt()} kg",
                    style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = addKg,
                    onValueChange = { addKg = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Add quantity (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val total = (feed.quantityKg + (addKg.toFloatOrNull() ?: 0f))
                Text("New total: ${total.toInt()} kg · ${(total / feed.dailyConsumptionKg).toInt()} days",
                    style = MaterialTheme.typography.bodySmall, color = FarmGreen)
            }
        },
        confirmButton = {
            Button(
                onClick = { onRestock(addKg.toFloatOrNull() ?: 0f) },
                colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
            ) { Text("Restock") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
