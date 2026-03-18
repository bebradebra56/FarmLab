package com.farmlab.labfarmis.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// ─── Flock ───────────────────────────────────────────────────────────────────

enum class FlockType(val displayName: String) {
    LAYER("Layer"),
    BROILER("Broiler"),
    BREEDER("Breeder"),
    TURKEY("Turkey"),
    DUCK("Duck")
}

@Entity(tableName = "flocks")
data class Flock(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: FlockType,
    val breed: String,
    val count: Int,
    val ageWeeks: Int,
    val zoneId: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Egg Record ───────────────────────────────────────────────────────────────

@Entity(tableName = "egg_records")
data class EggRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val flockId: String,
    val date: Long = System.currentTimeMillis(),
    val collected: Int,
    val broken: Int = 0,
    val sold: Int = 0,
    val notes: String = ""
)

// ─── Feed ─────────────────────────────────────────────────────────────────────

enum class FeedType(val displayName: String) {
    LAYER_FEED("Layer Feed"),
    GROWER_FEED("Grower Feed"),
    STARTER_FEED("Starter Feed"),
    SCRATCH_GRAINS("Scratch Grains"),
    SUPPLEMENT("Supplement")
}

@Entity(tableName = "feed_stocks")
data class FeedStock(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: FeedType,
    val quantityKg: Float,
    val dailyConsumptionKg: Float,
    val pricePerKg: Float,
    val supplier: String = "",
    val lastRestocked: Long = System.currentTimeMillis()
) {
    val daysRemaining: Float get() = if (dailyConsumptionKg > 0) quantityKg / dailyConsumptionKg else 999f
    val totalValue: Float get() = quantityKg * pricePerKg
}

// ─── Farm Event ───────────────────────────────────────────────────────────────

enum class EventType(val displayName: String, val emoji: String) {
    VACCINATION("Vaccination", "💉"),
    CLEANING("Cleaning", "🧹"),
    FEED_CHANGE("Feed Change", "🌾"),
    TRANSFER("Transfer", "🔄"),
    EGG_COLLECTION("Egg Collection", "🥚"),
    HEALTH_CHECK("Health Check", "🏥"),
    OTHER("Other", "📌")
}

@Entity(tableName = "farm_events")
data class FarmEvent(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: EventType,
    val date: Long,
    val flockId: String? = null,
    val notes: String = "",
    val isCompleted: Boolean = false
)

// ─── Sale ─────────────────────────────────────────────────────────────────────

enum class SaleType(val displayName: String) {
    EGGS("Eggs"),
    MEAT("Meat"),
    CHICKS("Chicks"),
    MANURE("Manure")
}

@Entity(tableName = "sales")
data class SaleRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: SaleType,
    val quantity: Int,
    val pricePerUnit: Float,
    val date: Long = System.currentTimeMillis(),
    val buyer: String = "",
    val notes: String = ""
) {
    val total: Float get() = quantity * pricePerUnit
}

// ─── Expense ─────────────────────────────────────────────────────────────────

enum class ExpenseCategory(val displayName: String) {
    FEED("Feed"),
    MEDICINE("Medicine"),
    EQUIPMENT("Equipment"),
    UTILITIES("Utilities"),
    LABOR("Labor"),
    OTHER("Other")
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val category: ExpenseCategory,
    val amount: Float,
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

// ─── Diary ───────────────────────────────────────────────────────────────────

enum class FarmMood(val displayName: String, val emoji: String) {
    GREAT("Great", "😄"),
    GOOD("Good", "😊"),
    NEUTRAL("Neutral", "😐"),
    BAD("Bad", "😞")
}

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val eggsCollected: Int = 0,
    val feedUsedKg: Float = 0f,
    val tasks: String = "",
    val problems: String = "",
    val notes: String = "",
    val mood: FarmMood = FarmMood.GOOD
)

// ─── Farm Zone ───────────────────────────────────────────────────────────────

enum class ZoneType(val displayName: String, val emoji: String) {
    COOP("Coop", "🏠"),
    PASTURE("Pasture", "🌿"),
    INCUBATOR("Incubator", "🥚"),
    FEED_STORAGE("Feed Storage", "🌾"),
    GREENHOUSE("Greenhouse", "🌱"),
    WATER_SOURCE("Water Source", "💧"),
    OTHER("Other", "📍")
}

@Entity(tableName = "farm_zones")
data class FarmZone(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: ZoneType,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val flockId: String? = null,
    val notes: String = "",
    val colorHex: Long = 0xFF2F855A
)

// ─── Health (computed, not stored in DB) ─────────────────────────────────────

enum class HealthStatus(val displayName: String) {
    HEALTHY("Healthy"),
    WARNING("Warning"),
    RISK("Risk")
}

data class HealthRecord(
    val flockId: String,
    val score: Int,
    val status: HealthStatus,
    val mortality: Float = 0f,
    val eggProductionRate: Float = 0f,
    val lastUpdated: Long = System.currentTimeMillis()
)
