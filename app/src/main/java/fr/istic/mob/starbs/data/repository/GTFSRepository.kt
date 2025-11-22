package fr.istic.mob.starbs.data.repository

import fr.istic.mob.starbs.data.local.AppDatabase

class GTFSRepository(private val db: AppDatabase) {

    val routeDao = db.routeDao()
    val tripDao = db.tripDao()
    val stopDao = db.stopDao()
    val stopTimeDao = db.stopTimeDao()
    val calendarDao = db.calendarDao()

    suspend fun clearDatabase() {
        db.clearAllTables()
    }

    suspend fun getAllRoutes() = routeDao.getAll()

    suspend fun getDirectionsForRoute(routeId: String): List<String> {
        val trips = tripDao.getTripsForRoute(routeId)
        return trips.mapNotNull { it.trip_headsign }.distinct()
    }

    suspend fun getHoraires(routeId: String, direction: String, date: String): List<String> {
        val trips = tripDao.getTripsForRoute(routeId)

        // Filtrer trips valides à cette date
        val validTrips = trips.filter { isServiceActive(it.service_id, date) }

        val trip = validTrips.firstOrNull { it.trip_headsign == direction } ?: return emptyList()

        val stopTimes = stopTimeDao.getForTrip(trip.trip_id)

        return stopTimes.map { it.arrival_time }
    }


    suspend fun isServiceActive(serviceId: String, date: String): Boolean {
        val cal = calendarDao.get(serviceId) ?: return false
        val dayOfWeek = getDayOfWeek(date)

        return when (dayOfWeek) {
            1 -> cal.monday == 1
            2 -> cal.tuesday == 1
            3 -> cal.wednesday == 1
            4 -> cal.thursday == 1
            5 -> cal.friday == 1
            6 -> cal.saturday == 1
            7 -> cal.sunday == 1
            else -> false
        }
    }

    private fun getDayOfWeek(date: String): Int {
        val sdf = java.text.SimpleDateFormat("yyyyMMdd")
        val d = sdf.parse(date)
        val cal = java.util.Calendar.getInstance()
        cal.time = d
        return cal.get(java.util.Calendar.DAY_OF_WEEK)
    }

}
