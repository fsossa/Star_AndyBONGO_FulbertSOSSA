package fr.istic.mob.starbs.data.repository

import fr.istic.mob.starbs.data.local.AppDatabase
import fr.istic.mob.starbs.data.local.entities.*

class GTFSRepository(private val db: AppDatabase) {

    suspend fun getAllRoutes(): List<Route> =
        db.routeDao().getAll()

    suspend fun getDirectionsForRoute(routeId: String): List<String> =
        db.tripDao().getDirectionsForRoute(routeId)

    suspend fun getHoraires(
        routeId: String,
        direction: String,
        date: String,
        time: String
    ): List<String> =
        db.stopTimeDao().getFilteredTimes(routeId, direction, date, time)

    suspend fun clearDatabase() {
        db.clearAllTables()
    }
}
