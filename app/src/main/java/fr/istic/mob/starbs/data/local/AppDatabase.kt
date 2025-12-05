package fr.istic.mob.starbs.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.istic.mob.starbs.data.local.dao.*
import fr.istic.mob.starbs.data.local.entities.*

@Database(
    entities = [
        Route::class,
        Trip::class,
        Stop::class,
        StopTime::class,
        Calendar::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun routeDao(): RouteDao
    abstract fun tripDao(): TripDao
    abstract fun stopDao(): StopDao
    abstract fun stopTimeDao(): StopTimeDao
    abstract fun calendarDao(): CalendarDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gtfs_star.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
