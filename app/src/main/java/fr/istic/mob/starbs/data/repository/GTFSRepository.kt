package fr.istic.mob.starbs.data.repository

import fr.istic.mob.starbs.data.local.AppDatabase
import fr.istic.mob.starbs.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GTFSRepository(private val db: AppDatabase) {

    // ROUTES
    suspend fun getAllRoutes(): List<Route> =
        withContext(Dispatchers.IO) {
            db.routeDao().getAll()
        }

    // DIRECTIONS
    suspend fun getDirectionsForRoute(routeId: String): List<String> =
        withContext(Dispatchers.IO) {
            db.tripDao().getDirectionsForRoute(routeId)
        }

    // HORAIRES
    suspend fun getHoraires(
        routeId: String,
        direction: String,
        date: String,
        time: String
    ): List<String> =
        withContext(Dispatchers.IO) {
            db.stopTimeDao().getFilteredTimes(routeId, direction, date, time)
        }

    // VIDER LA BASE
    suspend fun clearDatabase() =
        withContext(Dispatchers.IO) {
            db.clearAllTables()
        }

    // BASE VIDE ?
    suspend fun isDatabaseEmpty(): Boolean =
        withContext(Dispatchers.IO) {
            db.routeDao().countRoutes() == 0 && db.tripDao().getDirectionsForRoute("1").isEmpty()
        }
}
