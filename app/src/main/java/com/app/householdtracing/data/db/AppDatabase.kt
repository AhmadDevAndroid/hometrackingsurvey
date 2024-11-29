package com.app.householdtracing.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.household.data.db.dao.HomeTrackingDao
import com.app.householdtracing.data.model.HomeTrackingLocations

const val DB_NAME = "householdtracking.db"

@Database(
    entities = [HomeTrackingLocations::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun homeTrackingDao(): HomeTrackingDao

}

