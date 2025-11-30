package fr.istic.mob.starbs.services

import android.Manifest
import android.app.IntentService
import android.content.Intent
import androidx.annotation.RequiresPermission
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.data.local.entities.*
import fr.istic.mob.starbs.utils.NotificationUtils
import kotlinx.coroutines.delay
import java.util.zip.ZipFile

class GTFSParserService : IntentService("GTFSParserService") {

    companion object {
        const val ACTION_PROGRESS = "fr.istic.mob.starbs.GTFS_PROGRESS"
        const val EXTRA_PERCENT = "percent"
        const val EXTRA_MESSAGE = "message"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onHandleIntent(intent: Intent?) {

        NotificationUtils.createChannel(this)

        val zipPath = intent?.getStringExtra("zip_path") ?: return
        val zipFile = ZipFile(zipPath)

        sendProgress(0, "Début du parsing…")

        val db = MainApp.database
        db.clearAllTables()

        // Lancement du parsing en coroutine IO
        kotlinx.coroutines.runBlocking {

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {

                try {
                    var routesLines: List<String>? = null
                    var tripsLines: List<String>? = null
                    var stopsLines: List<String>? = null
                    var stopTimesLines: List<String>? = null
                    var calendarLines: List<String>? = null

                    zipFile.entries().asSequence().forEach { entry ->
                        val name = entry.name.lowercase()
                        val content = zipFile.getInputStream(entry).bufferedReader().readLines()

                        when {
                            name.contains("routes") -> routesLines = content
                            name.contains("trips") -> tripsLines = content
                            name.contains("stops") && !name.contains("stop_times") -> stopsLines = content
                            name.contains("stop_times") -> stopTimesLines = content
                            name.contains("calendar") -> calendarLines = content
                        }
                    }

                    // APPELS SUSPEND → maintenant OK
                    routesLines?.let { parseRoutes(it) }
                    sendProgress(20, "Routes ✔")

                    tripsLines?.let { parseTrips(it) }
                    sendProgress(40, "Trips ✔")

                    stopsLines?.let { parseStops(it) }
                    sendProgress(60, "Stops ✔")

                    stopTimesLines?.let { parseStopTimes(it) }
                    sendProgress(80, "Stop Times ✔")

                    calendarLines?.let { parseCalendar(it) }
                    sendProgress(100, "Calendrier ✔")

                } catch (e: Exception) {
                    NotificationUtils.notify(
                        this@GTFSParserService,
                        "Erreur parsing",
                        "Erreur inconnue ${e.message} ",
                        97
                    )
                }
            }
        }
    }


    private fun sendProgress(percent: Int, msg: String) {
        val intent = Intent(ACTION_PROGRESS)
        intent.putExtra(EXTRA_PERCENT, percent)
        intent.putExtra(EXTRA_MESSAGE, msg)
        sendBroadcast(intent)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private suspend fun parseRoutes(lines: List<String>) {
        val dao = MainApp.database.routeDao()
        val routes = lines.drop(1).mapNotNull { line ->
            val parts = line.split(',')
            if (parts.size < 6) return@mapNotNull null

            Route(
                route_id = parts[0],
                route_short_name = parts[2],
                route_long_name = parts[3],
                route_type = parts[4].toIntOrNull(),
                route_color = parts[5],
                route_text_color = parts.getOrNull(6)
            )
        }
        dao.insertAll(routes)
        NotificationUtils.notify(this, "Routes", "Routes complétées", 10)
        sendProgress(20, "Routes complétées")
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun parseTrips(lines: List<String>) {
        val dao = MainApp.database.tripDao()
        val trips = lines.drop(1).mapNotNull { line ->
            val parts = line.split(',')
            if (parts.size < 4) return@mapNotNull null

            Trip(
                trip_id = parts[2],
                route_id = parts[0],
                service_id = parts[1],
                trip_headsign = parts[3]
            )
        }
        dao.insertAll(trips)
        NotificationUtils.notify(this, "Trips", "Voyages complétés", 11)
        sendProgress(40, "Voyages complétés")
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private suspend fun parseStops(lines: List<String>) {
        val dao = MainApp.database.stopDao()
        val stops = lines.drop(1).mapNotNull { line ->
            val parts = line.split(',')
            if (parts.size < 6) return@mapNotNull null

            Stop(
                stop_id = parts[0],
                stop_name = parts[2],
                stop_lat = parts[4].toDoubleOrNull() ?: 0.0,
                stop_lon = parts[5].toDoubleOrNull() ?: 0.0
            )
        }
        dao.insertAll(stops)
        NotificationUtils.notify(this, "Stops", "Arrêts complétés", 12)
        sendProgress(60, "Arrêts complétés")
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun parseStopTimes(lines: List<String>) {
        val dao = MainApp.database.stopTimeDao()
        val times = lines.drop(1).mapNotNull { line ->
            val p = line.split(',')
            if (p.size < 5) return@mapNotNull null

            StopTime(
                trip_id = p[0],
                arrival_time = p[1],
                departure_time = p[2],
                stop_id = p[3],
                stop_sequence = p[4].toIntOrNull() ?: 0
            )
        }
        dao.insertAll(times)
        NotificationUtils.notify(this, "StopTimes", "Horaires complétés", 13)
        sendProgress(80, "Horaires complétés")
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private suspend fun parseCalendar(lines: List<String>) {
        val dao = MainApp.database.calendarDao()
        val cal = lines.drop(1).mapNotNull { line ->
            val p = line.split(',')
            if (p.size < 10) return@mapNotNull null

            Calendar(
                service_id = p[0],
                monday = p[1].toIntOrNull() ?: 0,
                tuesday = p[2].toIntOrNull() ?: 0,
                wednesday = p[3].toIntOrNull() ?: 0,
                thursday = p[4].toIntOrNull() ?: 0,
                friday = p[5].toIntOrNull() ?: 0,
                saturday = p[6].toIntOrNull() ?: 0,
                sunday = p[7].toIntOrNull() ?: 0,
                start_date = p[8],
                end_date = p[9]
            )
        }
        dao.insertAll(cal)
        NotificationUtils.notify(this, "Calendar", "Calendrier complété", 14)
        sendProgress(100, "Calendrier complété")
        delay(3000)
        NotificationUtils.notify(this, "StarBS", "Téléchargement et insertion en base réussie", 14)
        sendProgress(101, "Téléchargement et insertion en base réussie")

    }
}
