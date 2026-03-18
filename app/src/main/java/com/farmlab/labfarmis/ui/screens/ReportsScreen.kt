package com.farmlab.labfarmis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.farmlab.labfarmis.data.model.HealthStatus
import com.farmlab.labfarmis.ui.components.*
import com.farmlab.labfarmis.ui.theme.*
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val flocks by viewModel.flocks.collectAsState()
    val eggRecords by viewModel.eggRecords.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    val eggTrend = viewModel.getEggTrend(days = 14)
    val eggChartData = eggTrend.map { it.second }

    val monthRevenue = viewModel.getMonthlyRevenue()
    val monthExpenses = viewModel.getMonthlyExpenses()
    val netProfit = monthRevenue - monthExpenses

    val totalBirds = viewModel.getTotalBirdCount()
    val avgHealth = if (flocks.isNotEmpty())
        flocks.map { viewModel.getHealthScore(it.id).score }.average().toInt()
    else 0

    val revenueByWeek = buildRevenueWeeks(sales, expenses)
    val topFlock = flocks.maxByOrNull {
        eggRecords.filter { r -> r.flockId == it.id }.sumOf { r -> r.collected }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding() + 16.dp
        )
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF4C1D95), Color(0xFF7C3AED))))
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Column {
                        Text("Farm Reports", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Analytics & performance overview",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Key Metrics
        item {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(title = "Key Metrics — This Month")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Revenue", formatCurrency(monthRevenue), "this month",
                        Icons.Default.TrendingUp, Color(0xFF059669), modifier = Modifier.weight(1f))
                    StatCard("Expenses", formatCurrency(monthExpenses), "this month",
                        Icons.Default.TrendingDown, FarmRed, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Net Profit", formatCurrency(netProfit), if (netProfit >= 0) "profit" else "loss",
                        Icons.Default.AccountBalance,
                        if (netProfit >= 0) Color(0xFF059669) else FarmRed,
                        modifier = Modifier.weight(1f))
                    StatCard("Avg Health", "$avgHealth/100", "${flocks.size} flocks",
                        Icons.Default.Favorite, FarmGreen, modifier = Modifier.weight(1f))
                }
            }
        }

        // Egg Production Chart
        if (eggChartData.size >= 3) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Egg Production (14 days)", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            Text("Total: ${eggChartData.sum()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = FarmGreen, fontWeight = FontWeight.SemiBold)
                        }
                        SimpleLineChart(
                            data = eggChartData,
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            lineColor = FarmGreen,
                            fillColor = FarmGreen.copy(alpha = 0.12f)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (eggTrend.isNotEmpty()) {
                                Text(formatDate(eggTrend.first().first),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatDate(eggTrend.last().first),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Avg/day", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${if (eggChartData.isNotEmpty()) eggChartData.average().toInt() else 0}",
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Best day", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${if (eggChartData.isNotEmpty()) eggChartData.max() else 0}",
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                                    color = FarmGreen)
                            }
                            Column {
                                Text("Worst day", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${if (eggChartData.isNotEmpty()) eggChartData.min() else 0}",
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                                    color = FarmRed)
                            }
                        }
                    }
                }
            }
        }

        // Weekly Revenue vs Expenses
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Weekly Revenue vs Expenses", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f).height(160.dp)) {
                            SimpleBarChart(
                                data = revenueByWeek.map { it.first to it.second },
                                barColor = Color(0xFF059669),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Box(modifier = Modifier.weight(1f).height(160.dp)) {
                            SimpleBarChart(
                                data = revenueByWeek.map { it.first to it.third },
                                barColor = FarmRed,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF059669)))
                            Text("Revenue", style = MaterialTheme.typography.labelSmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp))
                                .background(FarmRed))
                            Text("Expenses", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Flock Performance Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Flock Performance", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    // Header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Flock", modifier = Modifier.weight(2f),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        Text("Eggs", modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        Text("Health", modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider()
                    flocks.forEach { flock ->
                        val totalEggs = eggRecords.filter { it.flockId == flock.id }.sumOf { it.collected }
                        val health = viewModel.getHealthScore(flock.id)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(flock.name, modifier = Modifier.weight(2f),
                                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text("$totalEggs", modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall, color = FarmGreen,
                                fontWeight = FontWeight.SemiBold)
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                HealthBadge(health.status)
                            }
                        }
                    }
                }
            }
        }

        // Top performer
        topFlock?.let { flock ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmYellowContainer),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🏆", style = MaterialTheme.typography.headlineSmall)
                        Column {
                            Text("Top Performer", style = MaterialTheme.typography.labelMedium,
                                color = FarmBrown, fontWeight = FontWeight.Bold)
                            Text(flock.name, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold, color = FarmBrown)
                            Text("${eggRecords.filter { it.flockId == flock.id }.sumOf { it.collected }} eggs total",
                                style = MaterialTheme.typography.bodySmall, color = FarmBrown)
                        }
                    }
                }
            }
        }
    }
}

private fun buildRevenueWeeks(
    sales: List<com.farmlab.labfarmis.data.model.SaleRecord>,
    expenses: List<com.farmlab.labfarmis.data.model.Expense>
): List<Triple<String, Float, Float>> {
    return (3 downTo 0).map { weeksAgo ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, -weeksAgo)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        val weekStart = cal.timeInMillis
        val weekEnd = weekStart + 7 * 24L * 60 * 60 * 1000

        val rev = sales.filter { it.date in weekStart until weekEnd }.sumOf { it.total.toDouble() }.toFloat()
        val exp = expenses.filter { it.date in weekStart until weekEnd }.sumOf { it.amount.toDouble() }.toFloat()
        val label = "W${4 - weeksAgo}"
        Triple(label, rev, exp)
    }
}
