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
fun HealthMonitorScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val flocks by viewModel.flocks.collectAsState()
    val healthRecords = flocks.map { viewModel.getHealthScore(it.id) }

    val avgScore = if (healthRecords.isNotEmpty())
        healthRecords.map { it.score }.average().toInt() else 0
    val overallStatus = when {
        avgScore >= 70 -> HealthStatus.HEALTHY
        avgScore >= 45 -> HealthStatus.WARNING
        else -> HealthStatus.RISK
    }
    val healthyCount = healthRecords.count { it.status == HealthStatus.HEALTHY }
    val warningCount = healthRecords.count { it.status == HealthStatus.WARNING }
    val riskCount = healthRecords.count { it.status == HealthStatus.RISK }

    val headerColor = when (overallStatus) {
        HealthStatus.HEALTHY -> listOf(FarmGreenDark, FarmGreen)
        HealthStatus.WARNING -> listOf(Color(0xFF92400E), FarmOrange)
        HealthStatus.RISK -> listOf(Color(0xFF991B1B), FarmRed)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Health Monitor", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Farm-wide flock health overview",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$avgScore", style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Avg Score", style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Overall stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HealthCountCard("Healthy", healthyCount, Color(0xFF059669), Color(0xFFD1FAE5), modifier = Modifier.weight(1f))
                HealthCountCard("Warning", warningCount, FarmOrange, FarmOrangeContainer, modifier = Modifier.weight(1f))
                HealthCountCard("Risk", riskCount, FarmRed, FarmRedContainer, modifier = Modifier.weight(1f))
            }
        }

        // Farm Health Score Gauge
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Farm Health Index", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Score circle
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                                .background(
                                    when (overallStatus) {
                                        HealthStatus.HEALTHY -> Color(0xFFD1FAE5)
                                        HealthStatus.WARNING -> FarmOrangeContainer
                                        HealthStatus.RISK -> FarmRedContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$avgScore", style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when (overallStatus) {
                                        HealthStatus.HEALTHY -> Color(0xFF065F46)
                                        HealthStatus.WARNING -> Color(0xFF92400E)
                                        HealthStatus.RISK -> Color(0xFF991B1B)
                                    })
                                Text("/100", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LinearProgressIndicator(
                                progress = { avgScore / 100f },
                                modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)),
                                color = when (overallStatus) {
                                    HealthStatus.HEALTHY -> Color(0xFF059669)
                                    HealthStatus.WARNING -> FarmOrange
                                    HealthStatus.RISK -> FarmRed
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            HealthBadge(overallStatus)
                            Text(
                                text = when (overallStatus) {
                                    HealthStatus.HEALTHY -> "Your farm is in excellent condition!"
                                    HealthStatus.WARNING -> "Some flocks need attention."
                                    HealthStatus.RISK -> "Immediate action required."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Individual flock health
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SectionHeader(title = "Flock Health Details")
            }
        }

        items(flocks) { flock ->
            val health = viewModel.getHealthScore(flock.id)
            FlockHealthCard(flock = flock, health = health)
        }

        // Health Tips
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FarmGreenContainer),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Lightbulb, null, tint = FarmGreenDark)
                        Text("Health Management Tips", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = FarmGreenDark)
                    }
                    val tips = listOf(
                        "🌡️ Maintain optimal coop temperature (18-24°C for layers)",
                        "💧 Ensure clean water access at all times — change daily",
                        "🌾 Balanced diet improves egg production by up to 20%",
                        "🔍 Daily observation helps catch early disease signs",
                        "💉 Follow vaccination schedule strictly",
                        "🧹 Clean and disinfect coops every 2 weeks"
                    )
                    tips.forEach { tip ->
                        Text(tip, style = MaterialTheme.typography.bodySmall, color = FarmGreenDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthCountCard(
    label: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("$count", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FlockHealthCard(flock: Flock, health: HealthRecord) {
    val statusColor = when (health.status) {
        HealthStatus.HEALTHY -> Color(0xFF059669)
        HealthStatus.WARNING -> FarmOrange
        HealthStatus.RISK -> FarmRed
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = when (flock.type) {
                            FlockType.LAYER -> "🐓"; FlockType.BROILER -> "🐔"
                            FlockType.TURKEY -> "🦃"; FlockType.DUCK -> "🦆"
                            FlockType.BREEDER -> "🥚"
                        },
                        fontSize = 28.sp
                    )
                    Column {
                        Text(flock.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${flock.count} birds · ${flock.ageWeeks}w · ${flock.breed}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HealthBadge(health.status)
                    Text("${health.score}/100", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = statusColor)
                }
            }

            LinearProgressIndicator(
                progress = { health.score / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.15f)
            )

            if (flock.type == FlockType.LAYER && health.eggProductionRate > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Production rate: ${(health.eggProductionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall, color = FarmGreen)
                    Text("Last updated: today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
