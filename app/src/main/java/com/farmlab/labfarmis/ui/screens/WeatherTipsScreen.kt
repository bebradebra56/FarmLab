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
import com.farmlab.labfarmis.ui.components.SectionHeader
import com.farmlab.labfarmis.ui.theme.*
import java.util.Calendar

@Composable
fun WeatherTipsScreen(navController: NavController, innerPadding: PaddingValues) {
    val month = Calendar.getInstance().get(Calendar.MONTH)
    val season = when (month) {
        in 2..4 -> Season.SPRING
        in 5..7 -> Season.SUMMER
        in 8..10 -> Season.AUTUMN
        else -> Season.WINTER
    }
    val weather = simulateWeather(season)

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
                    .background(
                        Brush.linearGradient(
                            when (season) {
                                Season.SPRING -> listOf(Color(0xFF059669), Color(0xFF34D399))
                                Season.SUMMER -> listOf(Color(0xFFD97706), FarmYellow)
                                Season.AUTUMN -> listOf(Color(0xFF92400E), FarmOrange)
                                Season.WINTER -> listOf(Color(0xFF1E40AF), Color(0xFF60A5FA))
                            }
                        )
                    )
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
                        Text("Weather & Tips", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${season.displayName} farming guidance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f))
                    }
                    Text(season.emoji, fontSize = 40.sp)
                }
            }
        }

        // Current Weather Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Current Conditions", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherStat("🌡️", "${weather.tempMin}°–${weather.tempMax}°C", "Temperature")
                        WeatherStat("💧", "${weather.humidity}%", "Humidity")
                        WeatherStat("💨", "${weather.windSpeed} km/h", "Wind")
                        WeatherStat(weather.icon, weather.condition, "Conditions")
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (weather.alert != null) FarmRedContainer else Color(0xFFD1FAE5)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (weather.alert != null) Icons.Default.Warning else Icons.Default.CheckCircle,
                                null,
                                tint = if (weather.alert != null) FarmRed else Color(0xFF059669),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                weather.alert ?: "Conditions are favorable for poultry farming today.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (weather.alert != null) Color(0xFF991B1B) else Color(0xFF065F46)
                            )
                        }
                    }
                }
            }
        }

        // Season Tips
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                SectionHeader(title = "${season.displayName} Farming Tips")
            }
        }

        items(seasonTips(season)) { tip ->
            TipCard(tip = tip)
        }

        // General advice
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FarmYellowContainer),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Lightbulb, null, tint = FarmBrown, modifier = Modifier.size(20.dp))
                        Text("Year-Round Best Practices", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = FarmBrown)
                    }
                    listOf(
                        "📊 Track egg production daily for trend analysis",
                        "🔬 Perform monthly health checks on all flocks",
                        "💊 Maintain a vaccination log for every bird group",
                        "🧹 Deep clean coops every 2-3 weeks",
                        "📝 Keep the farm diary updated daily",
                        "🌡️ Install thermometers in all coops"
                    ).forEach { advice ->
                        Text(advice, style = MaterialTheme.typography.bodySmall, color = FarmBrown)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherStat(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(icon, fontSize = 28.sp)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
}

@Composable
private fun TipCard(tip: FarmTip) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(tip.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(tip.emoji, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(tip.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(tip.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private enum class Season(val displayName: String, val emoji: String) {
    SPRING("Spring", "🌱"),
    SUMMER("Summer", "☀️"),
    AUTUMN("Autumn", "🍂"),
    WINTER("Winter", "❄️")
}

private data class WeatherData(
    val tempMin: Int, val tempMax: Int,
    val humidity: Int, val windSpeed: Int,
    val condition: String, val icon: String,
    val alert: String? = null
)

private data class FarmTip(
    val emoji: String, val title: String,
    val description: String, val color: Color
)

private fun simulateWeather(season: Season) = when (season) {
    Season.SPRING -> WeatherData(12, 18, 65, 12, "Partly Cloudy", "⛅",
        null)
    Season.SUMMER -> WeatherData(24, 34, 45, 8, "Sunny & Hot", "☀️",
        "⚠️ High heat alert: Ensure extra ventilation and cooling water for birds")
    Season.AUTUMN -> WeatherData(8, 15, 72, 18, "Windy/Cloudy", "🌥️",
        null)
    Season.WINTER -> WeatherData(-2, 6, 80, 22, "Cold & Damp", "🌨️",
        "⚠️ Cold stress warning: Check heating systems and insulate coops")
}

private fun seasonTips(season: Season): List<FarmTip> = when (season) {
    Season.SPRING -> listOf(
        FarmTip("🌱", "Prepare for New Chicks", "Spring is ideal for brooding. Prepare clean brooders with proper heating and bedding.", FarmGreen),
        FarmTip("💧", "Increase Water Access", "Rising temperatures increase water needs. Clean waterers daily.", Color(0xFF0EA5E9)),
        FarmTip("🌿", "Pasture Rotation", "Begin rotating birds through pastures to prevent overgrazing and parasite buildup.", FarmGreen),
        FarmTip("💉", "Spring Vaccination", "Schedule annual vaccines — Newcastle, IB, and Marek's boosters due.", Color(0xFFEC4899)),
        FarmTip("🧹", "Deep Spring Clean", "Thorough coop cleaning and disinfection prevents disease carryover from winter.", Color(0xFF06B6D4))
    )
    Season.SUMMER -> listOf(
        FarmTip("🌡️", "Heat Stress Management", "Provide shade, cold water, and increased ventilation. Reduce feed during hottest hours.", FarmOrange),
        FarmTip("💧", "Electrolyte Supplementation", "Add electrolytes to drinking water during heat waves to prevent dehydration.", Color(0xFF0EA5E9)),
        FarmTip("🪟", "Ventilation Check", "Inspect and clean all vents. Consider cooling fans for intensive housing.", FarmYellow),
        FarmTip("🌾", "Adjust Feed Schedule", "Feed in early morning and evening when temperatures are lower.", FarmBrown),
        FarmTip("🦟", "Parasite Control", "Warm weather increases mite, lice, and worm pressure. Inspect regularly.", FarmRed)
    )
    Season.AUTUMN -> listOf(
        FarmTip("💡", "Lighting Program", "Days shorten — add supplemental lighting (14-16 hrs) to maintain egg production.", FarmYellow),
        FarmTip("🌾", "Feed Inventory Stock-Up", "Order extra feed before winter price increases. Stock for 30+ days.", FarmBrown),
        FarmTip("🏠", "Coop Insulation Check", "Seal drafts and improve insulation before cold weather arrives.", Color(0xFF8B5CF6)),
        FarmTip("🍂", "Molting Management", "Support molting birds with high-protein feed to accelerate feather regrowth.", FarmGreen),
        FarmTip("🔋", "Equipment Maintenance", "Service heating systems, feeders, and waterers before winter.", FarmOrange)
    )
    Season.WINTER -> listOf(
        FarmTip("🔥", "Heating Management", "Maintain coop temperature above 10°C. Avoid condensation build-up.", FarmRed),
        FarmTip("❄️", "Frozen Water Prevention", "Use heated waterers or check manually 3x daily to prevent freezing.", Color(0xFF60A5FA)),
        FarmTip("🌾", "Increase Feed Ration", "Cold increases caloric needs. Boost feed by 10-15% in very cold weather.", FarmBrown),
        FarmTip("💡", "Lighting Hours", "Maintain 15+ hours of light daily to support laying through short days.", FarmYellow),
        FarmTip("🧹", "Bedding Management", "Deep litter method generates warmth. Add fresh bedding frequently.", Color(0xFF8B5CF6))
    )
}
