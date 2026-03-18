package com.farmlab.labfarmis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.farmlab.labfarmis.navigation.Screen
import com.farmlab.labfarmis.ui.screens.SplashScreen
import com.farmlab.labfarmis.ui.screens.DashboardScreen
import com.farmlab.labfarmis.ui.screens.FarmMapScreen
import com.farmlab.labfarmis.ui.screens.FlockListScreen
import com.farmlab.labfarmis.ui.screens.FlockDetailScreen
import com.farmlab.labfarmis.ui.screens.EggTrackerScreen
import com.farmlab.labfarmis.ui.screens.FeedManagerScreen
import com.farmlab.labfarmis.ui.screens.HealthMonitorScreen
import com.farmlab.labfarmis.ui.screens.PhotoCheckScreen
import com.farmlab.labfarmis.ui.screens.FarmCalendarScreen
import com.farmlab.labfarmis.ui.screens.WeatherTipsScreen
import com.farmlab.labfarmis.ui.screens.SalesTrackerScreen
import com.farmlab.labfarmis.ui.screens.ExpensesScreen
import com.farmlab.labfarmis.ui.screens.ReportsScreen
import com.farmlab.labfarmis.ui.screens.FarmDiaryScreen
import com.farmlab.labfarmis.ui.screens.MoreScreen
import com.farmlab.labfarmis.ut.presentation.app.FarmLabApplication
import com.farmlab.labfarmis.viewmodel.FarmViewModel
import com.farmlab.labfarmis.viewmodel.FarmViewModelFactory

@Composable
fun FarmLabApp() {
    val app = LocalContext.current.applicationContext as FarmLabApplication
    val viewModel: FarmViewModel = viewModel(factory = FarmViewModelFactory(app.repository))
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard, "Home", Icons.Default.Dashboard),
        BottomNavItem(Screen.Flocks, "Flocks", Icons.Default.Pets),
        BottomNavItem(Screen.EggTracker, "Eggs", Icons.Default.Egg),
        BottomNavItem(Screen.FarmCalendar, "Calendar", Icons.Default.CalendarMonth),
        BottomNavItem(Screen.More, "More", Icons.Default.GridView)
    )

    val showBottomBar = currentRoute != Screen.Splash.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route ||
                                (item.screen is Screen.Flocks && currentRoute?.startsWith("flock_detail") == true)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(imageVector = item.icon, contentDescription = item.label)
                            },
                            label = {
                                Text(text = item.label, style = MaterialTheme.typography.labelSmall)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onFinished = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.FarmMap.route) {
                FarmMapScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.Flocks.route) {
                FlockListScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.FlockDetail.route) { backStackEntry ->
                val flockId = backStackEntry.arguments?.getString("flockId") ?: return@composable
                FlockDetailScreen(flockId = flockId, viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.EggTracker.route) {
                EggTrackerScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.FeedManager.route) {
                FeedManagerScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.HealthMonitor.route) {
                HealthMonitorScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.PhotoCheck.route) {
                PhotoCheckScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.FarmCalendar.route) {
                FarmCalendarScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.WeatherTips.route) {
                WeatherTipsScreen(navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.SalesTracker.route) {
                SalesTrackerScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.Expenses.route) {
                ExpensesScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.FarmDiary.route) {
                FarmDiaryScreen(viewModel = viewModel, navController = navController, innerPadding = innerPadding)
            }
            composable(Screen.More.route) {
                MoreScreen(navController = navController, innerPadding = innerPadding)
            }
        }
    }
}

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
