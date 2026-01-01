package fr.istic.mob.starbs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.istic.mob.starbs.data.local.entities.Stop
import fr.istic.mob.starbs.data.models.RouteDirectionResult
import fr.istic.mob.starbs.data.models.StopSearchResult

@Dao
interface StopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stops: List<Stop>)

    @Query("SELECT * FROM stop WHERE stop_id = :id")
    suspend fun getStop(id: String): Stop?

    @Query("""
        SELECT 
            s.stop_name AS stop_name,
            MIN(s.stop_id) AS stop_id,
            MIN(s.stop_lat) AS stop_lat,
            MIN(s.stop_lon) AS stop_lon
        FROM stop s
        WHERE s.stop_name LIKE '%' || :query || '%'
        GROUP BY s.stop_name
        ORDER BY s.stop_name
        LIMIT 50
    """)
    suspend fun searchStopsByName(query: String): List<StopSearchResult>

    @Query("""
        SELECT DISTINCT
            r.route_id AS route_id,
            r.route_short_name AS route_short_name,
            r.route_long_name AS route_long_name,
            r.route_color AS route_color,
            r.route_text_color AS route_text_color,
            t.trip_headsign AS trip_headsign
        FROM stop_times st
        JOIN trip t ON t.trip_id = st.trip_id
        JOIN bus_route r ON r.route_id = t.route_id
        JOIN stop s ON s.stop_id = st.stop_id
        WHERE s.stop_name = :stopName
        ORDER BY r.route_short_name, t.trip_headsign
    """)
    suspend fun getRoutesAndDirectionsForStopName(stopName: String): List<RouteDirectionResult>


}
