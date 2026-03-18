package com.farmlab.labfarmis.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object FarmMap : Screen("farm_map")
    object Flocks : Screen("flocks")
    object FlockDetail : Screen("flock_detail/{flockId}") {
        fun createRoute(flockId: String) = "flock_detail/$flockId"
    }
    object EggTracker : Screen("egg_tracker")
    object FeedManager : Screen("feed_manager")
    object HealthMonitor : Screen("health_monitor")
    object PhotoCheck : Screen("photo_check")
    object FarmCalendar : Screen("farm_calendar")
    object WeatherTips : Screen("weather_tips")
    object SalesTracker : Screen("sales_tracker")
    object Expenses : Screen("expenses")
    object Reports : Screen("reports")
    object FarmDiary : Screen("farm_diary")
    object More : Screen("more")
}
