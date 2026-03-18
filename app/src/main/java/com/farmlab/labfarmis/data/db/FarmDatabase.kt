package com.farmlab.labfarmis.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.farmlab.labfarmis.data.model.*

@Database(
    entities = [
        Flock::class,
        EggRecord::class,
        FeedStock::class,
        FarmEvent::class,
        SaleRecord::class,
        Expense::class,
        DiaryEntry::class,
        FarmZone::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(FarmTypeConverters::class)
abstract class FarmDatabase : RoomDatabase() {

    abstract fun flockDao(): FlockDao
    abstract fun eggRecordDao(): EggRecordDao
    abstract fun feedStockDao(): FeedStockDao
    abstract fun farmEventDao(): FarmEventDao
    abstract fun saleRecordDao(): SaleRecordDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun farmZoneDao(): FarmZoneDao

    companion object {
        @Volatile
        private var INSTANCE: FarmDatabase? = null

        fun getInstance(context: Context): FarmDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FarmDatabase::class.java,
                    "farmlab.db"
                ).build().also { INSTANCE = it }
            }
    }
}
