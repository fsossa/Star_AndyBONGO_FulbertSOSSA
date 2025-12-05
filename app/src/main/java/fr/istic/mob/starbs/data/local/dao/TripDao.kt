package fr.istic.mob.starbs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.istic.mob.starbs.data.local.entities.Trip

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(trips: List<Trip>)

    @Query("""
        SELECT DISTINCT trip_headsign 
        FROM trip 
        WHERE route_id = :routeId 
        ORDER BY trip_headsign
    """)
    suspend fun getDirectionsForRoute(routeId: String): List<String>
}
