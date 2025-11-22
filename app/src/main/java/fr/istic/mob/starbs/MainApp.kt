package fr.istic.mob.starbs

import android.app.Application

class MainApp : Application() {

    companion object {
        lateinit var database: AppDatabase
        lateinit var repository: GTFSRepository
    }

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getDatabase(this)
        repository = GTFSRepository(database)
    }
}