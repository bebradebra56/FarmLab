package com.farmlab.labfarmis.data.db

import androidx.room.TypeConverter
import com.farmlab.labfarmis.data.model.*

class FarmTypeConverters {

    @TypeConverter fun flockTypeToString(v: FlockType): String = v.name
    @TypeConverter fun stringToFlockType(v: String): FlockType = FlockType.valueOf(v)

    @TypeConverter fun feedTypeToString(v: FeedType): String = v.name
    @TypeConverter fun stringToFeedType(v: String): FeedType = FeedType.valueOf(v)

    @TypeConverter fun eventTypeToString(v: EventType): String = v.name
    @TypeConverter fun stringToEventType(v: String): EventType = EventType.valueOf(v)

    @TypeConverter fun saleTypeToString(v: SaleType): String = v.name
    @TypeConverter fun stringToSaleType(v: String): SaleType = SaleType.valueOf(v)

    @TypeConverter fun expenseCategoryToString(v: ExpenseCategory): String = v.name
    @TypeConverter fun stringToExpenseCategory(v: String): ExpenseCategory = ExpenseCategory.valueOf(v)

    @TypeConverter fun farmMoodToString(v: FarmMood): String = v.name
    @TypeConverter fun stringToFarmMood(v: String): FarmMood = FarmMood.valueOf(v)

    @TypeConverter fun zoneTypeToString(v: ZoneType): String = v.name
    @TypeConverter fun stringToZoneType(v: String): ZoneType = ZoneType.valueOf(v)
}
