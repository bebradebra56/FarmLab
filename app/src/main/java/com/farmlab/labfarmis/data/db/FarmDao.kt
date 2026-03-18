package com.farmlab.labfarmis.data.db

import androidx.room.*
import com.farmlab.labfarmis.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FlockDao {
    @Query("SELECT * FROM flocks ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Flock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flock: Flock)

    @Update
    suspend fun update(flock: Flock)

    @Query("DELETE FROM flocks WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface EggRecordDao {
    @Query("SELECT * FROM egg_records ORDER BY date DESC")
    fun getAll(): Flow<List<EggRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: EggRecord)
}

@Dao
interface FeedStockDao {
    @Query("SELECT * FROM feed_stocks ORDER BY lastRestocked DESC")
    fun getAll(): Flow<List<FeedStock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: FeedStock)

    @Update
    suspend fun update(stock: FeedStock)

    @Query("DELETE FROM feed_stocks WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface FarmEventDao {
    @Query("SELECT * FROM farm_events ORDER BY date ASC")
    fun getAll(): Flow<List<FarmEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: FarmEvent)

    @Update
    suspend fun update(event: FarmEvent)

    @Query("DELETE FROM farm_events WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface SaleRecordDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAll(): Flow<List<SaleRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: SaleRecord)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAll(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface DiaryEntryDao {
    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun getAll(): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntry)

    @Update
    suspend fun update(entry: DiaryEntry)
}

@Dao
interface FarmZoneDao {
    @Query("SELECT * FROM farm_zones ORDER BY name ASC")
    fun getAll(): Flow<List<FarmZone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(zone: FarmZone)

    @Query("DELETE FROM farm_zones WHERE id = :id")
    suspend fun deleteById(id: String)
}
