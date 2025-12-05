package fr.istic.mob.starbs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.istic.mob.starbs.data.local.entities.Route

@Dao
interface RouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routes: List<Route>)

    @Query("SELECT * FROM bus_route ORDER BY route_short_name")
    suspend fun getAll(): List<Route>

    @Query("SELECT COUNT(*) FROM bus_route")
    fun countRoutes(): Int

}
