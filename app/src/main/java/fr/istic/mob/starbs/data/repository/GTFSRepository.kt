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
}
