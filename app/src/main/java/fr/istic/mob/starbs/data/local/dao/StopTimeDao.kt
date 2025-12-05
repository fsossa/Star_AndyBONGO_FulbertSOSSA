package fr.istic.mob.starbs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.istic.mob.starbs.data.local.entities.StopTime

@Dao
interface StopTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(times: List<StopTime>)

    @Query("""
        SELECT stop_times.departure_time FROM stop_times
        JOIN trip ON stop_times.trip_id = trip.trip_id
        JOIN calendar ON trip.service_id = calendar.service_id
        WHERE trip.route_id = :routeId
          AND trip.trip_headsign = :direction
          AND calendar.start_date <= :date
          AND calendar.end_date >= :date
          AND stop_times.departure_time >= :time
        ORDER BY stop_times.departure_time ASC
    """)
    suspend fun getFilteredTimes(
        routeId: String,
        direction: String,
        date: String,
        time: String
    ): List<String>
}
