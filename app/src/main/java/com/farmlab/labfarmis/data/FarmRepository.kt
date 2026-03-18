package com.farmlab.labfarmis.data

import com.farmlab.labfarmis.data.db.FarmDatabase
import com.farmlab.labfarmis.data.model.*
import kotlinx.coroutines.flow.Flow

class FarmRepository(private val db: FarmDatabase) {

    val flocks: Flow<List<Flock>> = db.flockDao().getAll()
    val eggRecords: Flow<List<EggRecord>> = db.eggRecordDao().getAll()
    val feedStocks: Flow<List<FeedStock>> = db.feedStockDao().getAll()
    val events: Flow<List<FarmEvent>> = db.farmEventDao().getAll()
    val sales: Flow<List<SaleRecord>> = db.saleRecordDao().getAll()
    val expenses: Flow<List<Expense>> = db.expenseDao().getAll()
    val diaryEntries: Flow<List<DiaryEntry>> = db.diaryEntryDao().getAll()
    val farmZones: Flow<List<FarmZone>> = db.farmZoneDao().getAll()

    // ─── Flocks ───────────────────────────────────────────────────────────────
    suspend fun addFlock(flock: Flock) = db.flockDao().insert(flock)
    suspend fun updateFlock(flock: Flock) = db.flockDao().update(flock)
    suspend fun deleteFlock(id: String) = db.flockDao().deleteById(id)

    // ─── Eggs ─────────────────────────────────────────────────────────────────
    suspend fun addEggRecord(record: EggRecord) = db.eggRecordDao().insert(record)

    // ─── Feed ─────────────────────────────────────────────────────────────────
    suspend fun addFeedStock(stock: FeedStock) = db.feedStockDao().insert(stock)
    suspend fun updateFeedStock(stock: FeedStock) = db.feedStockDao().update(stock)
    suspend fun deleteFeedStock(id: String) = db.feedStockDao().deleteById(id)

    // ─── Events ───────────────────────────────────────────────────────────────
    suspend fun addEvent(event: FarmEvent) = db.farmEventDao().insert(event)
    suspend fun updateEvent(event: FarmEvent) = db.farmEventDao().update(event)
    suspend fun deleteEvent(id: String) = db.farmEventDao().deleteById(id)

    // ─── Sales ────────────────────────────────────────────────────────────────
    suspend fun addSale(sale: SaleRecord) = db.saleRecordDao().insert(sale)
    suspend fun deleteSale(id: String) = db.saleRecordDao().deleteById(id)

    // ─── Expenses ─────────────────────────────────────────────────────────────
    suspend fun addExpense(expense: Expense) = db.expenseDao().insert(expense)
    suspend fun deleteExpense(id: String) = db.expenseDao().deleteById(id)

    // ─── Diary ────────────────────────────────────────────────────────────────
    suspend fun addDiaryEntry(entry: DiaryEntry) = db.diaryEntryDao().insert(entry)
    suspend fun updateDiaryEntry(entry: DiaryEntry) = db.diaryEntryDao().update(entry)

    // ─── Map Zones ────────────────────────────────────────────────────────────
    suspend fun addFarmZone(zone: FarmZone) = db.farmZoneDao().insert(zone)
    suspend fun deleteFarmZone(id: String) = db.farmZoneDao().deleteById(id)
}
