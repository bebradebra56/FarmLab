package com.farmlab.labfarmis.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.farmlab.labfarmis.data.model.Expense
import com.farmlab.labfarmis.data.model.ExpenseCategory
import com.farmlab.labfarmis.ui.components.EmptyState
import com.farmlab.labfarmis.ui.components.SectionHeader
import com.farmlab.labfarmis.ui.components.StatCard
import com.farmlab.labfarmis.ui.components.formatCurrency
import com.farmlab.labfarmis.ui.components.formatDateTime
import com.farmlab.labfarmis.ui.theme.FarmBrown
import com.farmlab.labfarmis.ui.theme.FarmGreen
import com.farmlab.labfarmis.ui.theme.FarmOrange
import com.farmlab.labfarmis.ui.theme.FarmRed
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import java.util.UUID

@Composable
fun ExpensesScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val expenses by viewModel.expenses.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var filterCategory by remember { mutableStateOf<ExpenseCategory?>(null) }

    val filtered = if (filterCategory == null) expenses else expenses.filter { it.category == filterCategory }
    val sorted = filtered.sortedByDescending { it.date }

    val totalExpenses = expenses.sumOf { it.amount.toDouble() }.toFloat()
    val monthExpenses = viewModel.getMonthlyExpenses()
    val categoryTotals = ExpenseCategory.values().map { cat ->
        cat to expenses.filter { it.category == cat }.sumOf { it.amount.toDouble() }.toFloat()
    }.filter { it.second > 0 }

    val categoryColors = mapOf(
        ExpenseCategory.FEED to FarmGreen,
        ExpenseCategory.MEDICINE to Color(0xFFEC4899),
        ExpenseCategory.EQUIPMENT to Color(0xFF8B5CF6),
        ExpenseCategory.UTILITIES to Color(0xFF0EA5E9),
        ExpenseCategory.LABOR to FarmOrange,
        ExpenseCategory.OTHER to FarmBrown
    )

    val fabBottomPadding = innerPadding.calculateBottomPadding() + 16.dp

    Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = fabBottomPadding + 72.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF991B1B), FarmRed)))
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
                            Text("Expenses", style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${expenses.size} transactions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatCurrency(monthExpenses), style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("this month", style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("Total Spent", formatCurrency(totalExpenses), "all time",
                        Icons.Default.Receipt, FarmRed, modifier = Modifier.weight(1f))
                    StatCard("This Month", formatCurrency(monthExpenses), "current month",
                        Icons.Default.CalendarMonth, FarmOrange, modifier = Modifier.weight(1f))
                }
            }

            // Donut chart
            if (categoryTotals.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Expenses by Category", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Donut chart
                                ExpenseDonutChart(
                                    data = categoryTotals,
                                    colors = categoryTotals.map { (cat, _) ->
                                        categoryColors[cat] ?: FarmBrown
                                    },
                                    modifier = Modifier.size(120.dp)
                                )
                                // Legend
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    categoryTotals.sortedByDescending { it.second }.forEach { (cat, amount) ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.size(10.dp).clip(CircleShape)
                                                    .background(categoryColors[cat] ?: FarmBrown)
                                            )
                                            Text(cat.displayName, style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f))
                                            Text(formatCurrency(amount), style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filter chips
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(selected = filterCategory == null, onClick = { filterCategory = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FarmRed, selectedLabelColor = Color.White))
                }
            }

            item {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(ExpenseCategory.values()) { cat ->
                        FilterChip(
                            selected = filterCategory == cat,
                            onClick = { filterCategory = if (filterCategory == cat) null else cat },
                            label = { Text(cat.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = categoryColors[cat] ?: FarmRed,
                                selectedLabelColor = Color.White)
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionHeader(title = "Expense Records")
                }
            }

            if (sorted.isEmpty()) {
                item {
                    EmptyState(icon = Icons.Default.Receipt,
                        title = "No expenses recorded",
                        subtitle = "Tap + to add an expense")
                }
            } else {
                items(sorted) { expense ->
                    ExpenseCard(
                        expense = expense,
                        color = categoryColors[expense.category] ?: FarmBrown,
                        onDelete = { viewModel.deleteExpense(expense.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = fabBottomPadding),
            containerColor = FarmRed,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, "Add Expense")
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { expense ->
                viewModel.addExpense(expense)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ExpenseDonutChart(
    data: List<Pair<ExpenseCategory, Float>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
    Canvas(modifier = modifier) {
        val strokeWidth = 24.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        var startAngle = -90f

        data.forEachIndexed { i, (_, value) ->
            val sweep = (value / total) * 360f
            drawArc(
                color = colors.getOrElse(i) { FarmBrown },
                startAngle = startAngle,
                sweepAngle = sweep - 2f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, color: Color, onDelete: () -> Unit) {
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
                modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (expense.category) {
                        ExpenseCategory.FEED -> Icons.Default.Grass
                        ExpenseCategory.MEDICINE -> Icons.Default.HealthAndSafety
                        ExpenseCategory.EQUIPMENT -> Icons.Default.Build
                        ExpenseCategory.UTILITIES -> Icons.Default.ElectricBolt
                        ExpenseCategory.LABOR -> Icons.Default.Person
                        ExpenseCategory.OTHER -> Icons.Default.Category
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(expense.description, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(expense.category.displayName, style = MaterialTheme.typography.bodySmall,
                    color = color)
                Text(formatDateTime(expense.date), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(formatCurrency(expense.amount), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold, color = FarmRed)
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
private fun AddExpenseDialog(onDismiss: () -> Unit, onAdd: (Expense) -> Unit) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FEED) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedCategory.displayName, onValueChange = {},
                        readOnly = true, label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ExpenseCategory.values().forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.displayName) },
                                onClick = { selectedCategory = cat; expanded = false })
                        }
                    }
                }

                OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (\$)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && amount.isNotBlank()) {
                        onAdd(Expense(
                            id = UUID.randomUUID().toString(),
                            category = selectedCategory,
                            amount = amount.toFloatOrNull() ?: 0f,
                            description = description, notes = notes
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FarmRed)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
