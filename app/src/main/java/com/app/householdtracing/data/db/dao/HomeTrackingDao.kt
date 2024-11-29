package app.household.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.app.householdtracing.data.model.HomeTrackingLocations

@Dao
interface HomeTrackingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HomeTrackingLocations)

    @Query("SELECT * FROM home_tracking_locations")
    suspend fun getAll(): List<HomeTrackingLocations>

    @Query("DELETE FROM home_tracking_locations")
    suspend fun deleteAll()

    @Query("SELECT * FROM home_tracking_locations ORDER BY milliseconds ASC LIMIT 1")
    suspend fun getOldestRecord(): HomeTrackingLocations?

    @Query("DELETE FROM home_tracking_locations WHERE milliseconds = :id")
    suspend fun deleteRecordById(id: Long)

    @Transaction
    suspend fun deleteAndInsertNew(entity: HomeTrackingLocations) {
        val record = getOldestRecord()
        record?.let {
            deleteRecordById(it.milliseconds)
        }
        insert(entity)
    }
}