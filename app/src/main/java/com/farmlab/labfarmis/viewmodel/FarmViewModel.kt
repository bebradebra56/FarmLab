package com.farmlab.labfarmis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmlab.labfarmis.data.FarmRepository
import com.farmlab.labfarmis.data.model.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FarmViewModel(private val repository: FarmRepository) : ViewModel() {

    val flocks: StateFlow<List<Flock>> = repository.flocks
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val eggRecords: StateFlow<List<EggRecord>> = repository.eggRecords
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val feedStocks: StateFlow<List<FeedStock>> = repository.feedStocks
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val events: StateFlow<List<FarmEvent>> = repository.events
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val sales: StateFlow<List<SaleRecord>> = repository.sales
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val expenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val diaryEntries: StateFlow<List<DiaryEntry>> = repository.diaryEntries
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val farmZones: StateFlow<List<FarmZone>> = repository.farmZones
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ─── Health Scoring ───────────────────────────────────────────────────────

    fun getHealthScore(flockId: String): HealthRecord {
        val flock = flocks.value.find { it.id == flockId }
            ?: return HealthRecord(flockId, 50, HealthStatus.WARNING)

        val recentEggs = eggRecords.value.filter { it.flockId == flockId }
            .sortedByDescending { it.date }.take(7)

        var score = 65

        if (flock.type == FlockType.LAYER) {
            score += when {
                flock.ageWeeks in 20..60 -> 15
                flock.ageWeeks in 16..19 -> 8
                flock.ageWeeks > 60 -> -10
                else -> 0
            }
        } else if (flock.type == FlockType.BROILER) {
            score += when {
                flock.ageWeeks in 3..7 -> 12
                flock.ageWeeks > 8 -> -5
                else -> 5
            }
        }

        val avgEggs = if (recentEggs.isNotEmpty()) recentEggs.map { it.collected }.average().toFloat() else 0f
        val productionRate = if (flock.count > 0) avgEggs / flock.count else 0f
        if (flock.type == FlockType.LAYER) {
            score += (productionRate * 20).toInt().coerceIn(-10, 20)
        }

        val avgBroken = if (recentEggs.isNotEmpty()) recentEggs.map { it.broken }.average().toFloat() else 0f
        if (avgBroken > 3) score -= 8
        else if (avgBroken <= 1) score += 3

        score = score.coerceIn(0, 100)

        val status = when {
            score >= 70 -> HealthStatus.HEALTHY
            score >= 45 -> HealthStatus.WARNING
            else -> HealthStatus.RISK
        }

        return HealthRecord(
            flockId = flockId,
            score = score,
            status = status,
            eggProductionRate = productionRate,
            lastUpdated = System.currentTimeMillis()
        )
    }

    // ─── Egg Forecast ─────────────────────────────────────────────────────────

    fun predictEggProduction(flockId: String, days: Int = 7): List<Int> {
        val flock = flocks.value.find { it.id == flockId } ?: return List(days) { 0 }
        val recentEggs = eggRecords.value.filter { it.flockId == flockId }
            .sortedByDescending { it.date }.take(7)

        val avg = if (recentEggs.isNotEmpty())
            recentEggs.map { it.collected }.average().toFloat()
        else flock.count * 0.72f

        val trend = if (recentEggs.size >= 3) {
            (recentEggs[0].collected.toFloat() - recentEggs[2].collected.toFloat()) / 2f
        } else 0f

        return (0 until days).map { day ->
            (avg + trend * day * 0.05f).toInt().coerceAtLeast(0)
        }
    }

    // ─── Dashboard Stats ──────────────────────────────────────────────────────

    fun getTodayEggs(): Int {
        val todayStart = getTodayStart()
        return eggRecords.value.filter { it.date >= todayStart }.sumOf { it.collected }
    }

    fun getTotalBirdCount(): Int = flocks.value.sumOf { it.count }

    fun getLowFeedAlerts(): List<FeedStock> =
        feedStocks.value.filter { it.daysRemaining < 7f }

    fun getUpcomingEvents(days: Int = 7): List<FarmEvent> {
        val now = System.currentTimeMillis()
        val future = now + days * 24L * 60 * 60 * 1000
        return events.value.filter { !it.isCompleted && it.date in now..future }
            .sortedBy { it.date }
    }

    fun getMonthlyRevenue(): Float =
        sales.value.filter { isThisMonth(it.date) }.sumOf { it.total.toDouble() }.toFloat()

    fun getMonthlyExpenses(): Float =
        expenses.value.filter { isThisMonth(it.date) }.sumOf { it.amount.toDouble() }.toFloat()

    fun getEggTrend(flockId: String? = null, days: Int = 14): List<Pair<Long, Int>> {
        val records = if (flockId != null)
            eggRecords.value.filter { it.flockId == flockId }
        else eggRecords.value
        return records.sortedByDescending { it.date }
            .take(days)
            .reversed()
            .groupBy { dateKey(it.date) }
            .map { (date, recs) -> date to recs.sumOf { it.collected } }
    }

    fun getRevenueByWeek(): List<Pair<String, Float>> {
        val cal = Calendar.getInstance()
        return (3 downTo 0).map { weeksAgo ->
            cal.add(Calendar.WEEK_OF_YEAR, if (weeksAgo == 3) -weeksAgo else -1)
            val weekLabel = "W${4 - weeksAgo}"
            val weekStart = getWeekStart(cal)
            val weekEnd = weekStart + 7 * 24L * 60 * 60 * 1000
            val revenue = sales.value.filter { it.date in weekStart until weekEnd }.sumOf { it.total.toDouble() }.toFloat()
            weekLabel to revenue
        }
    }

    // ─── CRUD Delegates ───────────────────────────────────────────────────────

    fun addFlock(flock: Flock) = viewModelScope.launch { repository.addFlock(flock) }
    fun updateFlock(flock: Flock) = viewModelScope.launch { repository.updateFlock(flock) }
    fun deleteFlock(id: String) = viewModelScope.launch { repository.deleteFlock(id) }

    fun addEggRecord(record: EggRecord) = viewModelScope.launch { repository.addEggRecord(record) }

    fun addFeedStock(stock: FeedStock) = viewModelScope.launch { repository.addFeedStock(stock) }
    fun updateFeedStock(stock: FeedStock) = viewModelScope.launch { repository.updateFeedStock(stock) }
    fun deleteFeedStock(id: String) = viewModelScope.launch { repository.deleteFeedStock(id) }

    fun addEvent(event: FarmEvent) = viewModelScope.launch { repository.addEvent(event) }
    fun updateEvent(event: FarmEvent) = viewModelScope.launch { repository.updateEvent(event) }
    fun deleteEvent(id: String) = viewModelScope.launch { repository.deleteEvent(id) }

    fun addSale(sale: SaleRecord) = viewModelScope.launch { repository.addSale(sale) }
    fun deleteSale(id: String) = viewModelScope.launch { repository.deleteSale(id) }

    fun addExpense(expense: Expense) = viewModelScope.launch { repository.addExpense(expense) }
    fun deleteExpense(id: String) = viewModelScope.launch { repository.deleteExpense(id) }

    fun addDiaryEntry(entry: DiaryEntry) = viewModelScope.launch { repository.addDiaryEntry(entry) }
    fun updateDiaryEntry(entry: DiaryEntry) = viewModelScope.launch { repository.updateDiaryEntry(entry) }

    fun addFarmZone(zone: FarmZone) = viewModelScope.launch { repository.addFarmZone(zone) }
    fun deleteFarmZone(id: String) = viewModelScope.launch { repository.deleteFarmZone(id) }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun getTodayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun isThisMonth(timestamp: Long): Boolean {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { timeInMillis = timestamp }
        return now.get(Calendar.MONTH) == then.get(Calendar.MONTH) &&
                now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
    }

    private fun dateKey(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getWeekStart(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}

class FarmViewModelFactory(private val repository: FarmRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        FarmViewModel(repository) as T
}
