package fr.istic.mob.starbs

import android.app.Application
import fr.istic.mob.starbs.data.local.AppDatabase
import fr.istic.mob.starbs.data.repository.GTFSRepository

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