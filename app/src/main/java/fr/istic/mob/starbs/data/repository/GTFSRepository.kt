package fr.istic.mob.starbs.data.repository

import fr.istic.mob.starbs.data.local.AppDatabase
import fr.istic.mob.starbs.data.local.entities.*
import fr.istic.mob.starbs.data.models.PassageRow
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

    suspend fun getStopsFor(routeId: String, direction: String): List<Stop> =
        withContext(Dispatchers.IO) {
            db.stopTimeDao().getStopsForRouteAndDirection(routeId, direction)
        }

    suspend fun getTimesFor(
        routeId: String,
        direction: String,
        stopId: String,
        afterTime: String
    ): List<String> =
        withContext(Dispatchers.IO) {
            db.stopTimeDao().getTimesForStopAfterTime(routeId, direction, stopId, afterTime)
        }

    suspend fun getPassagesToTerminus(
        routeId: String,
        direction: String,
        stopId: String,
        clickedDepartureTime: String
    ): List<PassageRow> =
        withContext(Dispatchers.IO) {

            val tripId = db.stopTimeDao()
                .findTripIdForStopAndTime(routeId, direction, stopId, clickedDepartureTime)
                ?: return@withContext emptyList()

            val seq = db.stopTimeDao()
                .getStopSequenceInTrip(tripId, stopId)
                ?: return@withContext emptyList()

            db.stopTimeDao().getPassagesFromSequence(tripId, seq)
        }



}
