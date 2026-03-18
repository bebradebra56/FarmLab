package com.farmlab.labfarmis.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.farmlab.labfarmis.navigation.Screen
import com.farmlab.labfarmis.ui.theme.*

@Composable
fun MoreScreen(navController: NavController, innerPadding: PaddingValues) {
    val context = LocalContext.current

    val menuItems = listOf(
        MoreMenuItem(
            "Farm Map", Icons.Default.Map, "2D farm layout", FarmGreen,
            Screen.FarmMap.route
        ),
        MoreMenuItem(
            "Feed Manager",
            Icons.Default.Grass,
            "Feed inventory & stock",
            FarmYellow.copy(red = 0.8f),
            Screen.FeedManager.route
        ),
        MoreMenuItem(
            "Health Monitor",
            Icons.Default.HealthAndSafety,
            "Flock health scores",
            Color(0xFF0EA5E9),
            Screen.HealthMonitor.route
        ),
        MoreMenuItem(
            "Photo Check", Icons.Default.CameraAlt, "AI bird diagnostics", FarmBrown,
            Screen.PhotoCheck.route
        ),
        MoreMenuItem(
            "Weather & Tips", Icons.Default.WbSunny, "Seasonal farming tips", FarmOrange,
            Screen.WeatherTips.route
        ),
        MoreMenuItem(
            "Sales Tracker", Icons.Default.AttachMoney, "Eggs, meat & chicks", Color(0xFF059669),
            Screen.SalesTracker.route
        ),
        MoreMenuItem(
            "Expenses", Icons.Default.Receipt, "Track farm costs", FarmRed,
            Screen.Expenses.route
        ),
        MoreMenuItem(
            "Reports", Icons.Default.BarChart, "Analytics & charts", Color(0xFF7C3AED),
            Screen.Reports.route
        ),
        MoreMenuItem(
            "Farm Diary", Icons.Default.MenuBook, "Daily journal entries", Color(0xFF0891B2),
            Screen.FarmDiary.route
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(FarmBrown, FarmBrownLight)))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column {
                Text(
                    "More Features", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = Color.White
                )
                Text(
                    "All tools for your smart farm", style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(
                start = 12.dp, end = 12.dp,
                top = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(menuItems) { item ->
                MoreMenuCard(item = item, onClick = { navController.navigate(item.route) })
            }
            item {
                MoreMenuCard(
                    item = MoreMenuItem(
                        "Privacy Policy",
                        Icons.Default.PrivacyTip,
                        "Tap to read",
                        Color(0xFF059669),
                        Screen.FarmDiary.route
                    ), onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://farrmlab.com/privacy-policy.html")
                        )
                        context.startActivity(intent)
                    })
            }
        }
    }
}

@Composable
private fun MoreMenuCard(item: MoreMenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                maxLines = 1
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                maxLines = 2,
                lineHeight = 11.sp
            )
        }
    }
}

private data class MoreMenuItem(
    val title: String,
    val icon: ImageVector,
    val subtitle: String,
    val color: Color,
    val route: String
)
