package fr.istic.mob.starbs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.istic.mob.starbs.data.local.entities.Stop
import fr.istic.mob.starbs.data.local.entities.StopTime
import fr.istic.mob.starbs.data.models.PassageRow


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


    @Query("""
        SELECT DISTINCT st.departure_time
        FROM stop_times st
        JOIN trip t ON st.trip_id = t.trip_id
        WHERE t.route_id = :routeId
          AND t.trip_headsign = :direction
          AND st.stop_id = :stopId
          AND st.departure_time >= :afterTime
          AND st.departure_time <= :endOfDay
        ORDER BY st.departure_time ASC
    """)
    suspend fun getTimesForStopAfterTime(
        routeId: String,
        direction: String,
        stopId: String,
        afterTime: String,
        endOfDay: String = "23:59:59"
    ): List<String>


    @Query("""
    SELECT st.departure_time
    FROM stop_times st
    JOIN trip t ON st.trip_id = t.trip_id
    JOIN calendar c ON t.service_id = c.service_id
    WHERE t.route_id = :routeId
      AND t.trip_headsign = :direction
      AND st.stop_id = :stopId
      AND :date BETWEEN c.start_date AND c.end_date
      AND st.departure_time >= :afterTime
      AND st.departure_time <= :endOfDay
    ORDER BY st.departure_time ASC
""")
    suspend fun getTimesForStopOnDate(
        routeId: String,
        direction: String,
        stopId: String,
        date: String,
        afterTime: String,
        endOfDay: String
    ): List<String>

    @Query("""
        SELECT st.trip_id
        FROM stop_times st
        JOIN trip t ON st.trip_id = t.trip_id
        WHERE t.route_id = :routeId
          AND t.trip_headsign = :direction
          AND st.stop_id = :stopId
          AND st.departure_time = :departureTime
        LIMIT 1
    """)
    suspend fun findTripIdForStopAndTime(
        routeId: String,
        direction: String,
        stopId: String,
        departureTime: String
    ): String?

    @Query("""
        SELECT stop_sequence
        FROM stop_times
        WHERE trip_id = :tripId
          AND stop_id = :stopId
        LIMIT 1
    """)
    suspend fun getStopSequenceInTrip(
        tripId: String,
        stopId: String
    ): Int?

    @Query("""
        SELECT s.stop_name AS stop_name,
               st.departure_time AS departure_time,
               st.stop_sequence AS stop_sequence
        FROM stop_times st
        JOIN stop s ON s.stop_id = st.stop_id
        WHERE st.trip_id = :tripId
          AND st.stop_sequence >= :fromSeq
        ORDER BY st.stop_sequence ASC
    """)
    suspend fun getPassagesFromSequence(
        tripId: String,
        fromSeq: Int
    ): List<PassageRow>

}
