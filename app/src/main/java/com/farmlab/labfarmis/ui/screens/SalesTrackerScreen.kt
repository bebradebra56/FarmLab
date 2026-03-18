package com.farmlab.labfarmis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.farmlab.labfarmis.data.model.SaleRecord
import com.farmlab.labfarmis.data.model.SaleType
import com.farmlab.labfarmis.ui.components.*
import com.farmlab.labfarmis.ui.theme.*
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import java.util.UUID

@Composable
fun SalesTrackerScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val sales by viewModel.sales.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf<SaleType?>(null) }

    val filtered = if (filterType == null) sales else sales.filter { it.type == filterType }
    val sortedSales = filtered.sortedByDescending { it.date }

    val totalRevenue = sales.sumOf { it.total.toDouble() }.toFloat()
    val monthRevenue = viewModel.getMonthlyRevenue()
    val eggSales = sales.filter { it.type == SaleType.EGGS }.sumOf { it.total.toDouble() }.toFloat()
    val meatSales = sales.filter { it.type == SaleType.MEAT }.sumOf { it.total.toDouble() }.toFloat()

    val fabBottomPadding = innerPadding.calculateBottomPadding() + 16.dp

    Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = fabBottomPadding + 72.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF065F46), Color(0xFF059669))))
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
                            Text("Sales Tracker", style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${sales.size} transactions total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatCurrency(monthRevenue), style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("this month", style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Summary stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SaleStatCard("Total", formatCurrency(totalRevenue), "All time",
                        Color(0xFF059669), modifier = Modifier.weight(1f))
                    SaleStatCard("Eggs", formatCurrency(eggSales), "🥚",
                        FarmYellow.copy(red = 0.7f), modifier = Modifier.weight(1f))
                    SaleStatCard("Meat", formatCurrency(meatSales), "🐔",
                        FarmBrown, modifier = Modifier.weight(1f))
                }
            }

            // Filter
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = filterType == null, onClick = { filterType = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF059669),
                            selectedLabelColor = Color.White))
                    SaleType.values().forEach { type ->
                        FilterChip(
                            selected = filterType == type,
                            onClick = { filterType = if (filterType == type) null else type },
                            label = { Text(type.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF059669),
                                selectedLabelColor = Color.White)
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SectionHeader(title = "Transactions")
                }
            }

            if (sortedSales.isEmpty()) {
                item {
                    EmptyState(icon = Icons.Default.AttachMoney,
                        title = "No sales recorded", subtitle = "Tap + to record a sale")
                }
            } else {
                items(sortedSales) { sale ->
                    SaleCard(sale = sale, onDelete = { viewModel.deleteSale(sale.id) })
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = fabBottomPadding),
            containerColor = Color(0xFF059669),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, "Add Sale")
        }
    }

    if (showAddDialog) {
        AddSaleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { sale ->
                viewModel.addSale(sale)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SaleStatCard(label: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun SaleCard(sale: SaleRecord, onDelete: () -> Unit) {
    val (emoji, color) = when (sale.type) {
        SaleType.EGGS -> "🥚" to FarmYellow.copy(red = 0.7f)
        SaleType.MEAT -> "🐔" to FarmBrown
        SaleType.CHICKS -> "🐤" to FarmGreen
        SaleType.MANURE -> "🌱" to Color(0xFF059669)
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(sale.type.displayName, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
                Text("${sale.quantity} units × ${formatCurrency(sale.pricePerUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (sale.buyer.isNotBlank()) {
                    Text("Buyer: ${sale.buyer}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(formatDateTime(sale.date), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(formatCurrency(sale.total), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold, color = Color(0xFF059669))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSaleDialog(onDismiss: () -> Unit, onAdd: (SaleRecord) -> Unit) {
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var buyer by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SaleType.EGGS) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Sale", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedType.displayName, onValueChange = {},
                        readOnly = true, label = { Text("Sale Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        SaleType.values().forEach { type ->
                            DropdownMenuItem(text = { Text(type.displayName) },
                                onClick = { selectedType = type; expanded = false })
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantity") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Price/unit") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                val total = (quantity.toIntOrNull() ?: 0) * (price.toFloatOrNull() ?: 0f)
                if (total > 0) {
                    Text("Total: ${formatCurrency(total)}", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                }
                OutlinedTextField(value = buyer, onValueChange = { buyer = it },
                    label = { Text("Buyer (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (quantity.isNotBlank() && price.isNotBlank()) {
                        onAdd(SaleRecord(
                            id = UUID.randomUUID().toString(),
                            type = selectedType,
                            quantity = quantity.toIntOrNull() ?: 0,
                            pricePerUnit = price.toFloatOrNull() ?: 0f,
                            buyer = buyer, notes = notes
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
