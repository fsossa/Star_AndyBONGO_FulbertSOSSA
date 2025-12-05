package fr.istic.mob.starbs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.istic.mob.starbs.data.local.entities.Stop
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

    @Query("""
        SELECT s.stop_id, s.stop_name, s.stop_lat, s.stop_lon
        FROM stop s
        JOIN (
            SELECT st.stop_id, MIN(st.stop_sequence) AS seq
            FROM stop_times st
            JOIN trip t ON t.trip_id = st.trip_id
            WHERE t.route_id = :routeId
              AND t.trip_headsign = :direction
            GROUP BY st.stop_id
        ) x ON x.stop_id = s.stop_id
        ORDER BY x.seq ASC
    """)
    suspend fun getStopsForRouteAndDirection(
        routeId: String,
        direction: String
    ): List<Stop>



}
