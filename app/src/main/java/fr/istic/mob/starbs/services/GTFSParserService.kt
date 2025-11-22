package fr.istic.mob.starbs.services

import android.Manifest
import android.app.IntentService
import android.content.Intent
import androidx.annotation.RequiresPermission
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.data.local.entities.*
import fr.istic.mob.starbs.utils.NotificationUtils
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.zip.ZipFile

class GTFSParserService : IntentService("GTFSParserService") {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onHandleIntent(intent: Intent?) {
        NotificationUtils.createChannel(this)

        val zipPath = intent?.getStringExtra("zip_path") ?: return
        val zipFile = ZipFile(zipPath)

        val db = MainApp.database

        db.clearAllTables()

        NotificationUtils.notify(
            this, "Parsing GTFS", "Décompression en cours…", 3
        )

        CoroutineScope(Dispatchers.IO).launch {

            try {
                zipFile.entries().asSequence().forEach { entry ->
                    val name = entry.name.lowercase()

                    val content = zipFile.getInputStream(entry)
                        .bufferedReader()
                        .readLines()

                    when {
                        name.contains("routes") -> parseRoutes(content)
                        name.contains("trips") -> parseTrips(content)
                        name.contains("stops") -> parseStops(content)
                        name.contains("stop_times") -> parseStopTimes(content)
                        name.contains("calendar") -> parseCalendar(content)
                    }
                }

                NotificationUtils.notify(
                    this@GTFSParserService,
                    "GTFS prêt",
                    "Base mise à jour",
                    4
                )

            } catch (e: Exception) {
                NotificationUtils.notify(
                    this@GTFSParserService,
                    "Erreur parsing",
                    e.message ?: "Erreur inconnue",
                    98
                )
            }
        }

    }

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
    }

    private suspend fun parseTrips(lines: List<String>) {
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
    }

    private suspend fun parseStops(lines: List<String>) {
        val dao = MainApp.database.stopDao()
        val stops = lines.drop(1).mapNotNull { line ->
            val parts = line.split(',')
            if (parts.size < 4) return@mapNotNull null

            Stop(
                stop_id = parts[0],
                stop_name = parts[2],
                stop_lat = parts[4].toDouble(),
                stop_lon = parts[5].toDouble()
            )
        }
        dao.insertAll(stops)
    }

    private suspend fun parseStopTimes(lines: List<String>) {
        val dao = MainApp.database.stopTimeDao()
        val times = lines.drop(1).mapNotNull { line ->
            val p = line.split(',')
            if (p.size < 5) return@mapNotNull null

            StopTime(
                trip_id = p[0],
                arrival_time = p[1],
                departure_time = p[2],
                stop_id = p[3],
                stop_sequence = p[4].toInt()
            )
        }
        dao.insertAll(times)
    }

    private suspend fun parseCalendar(lines: List<String>) {
        val dao = MainApp.database.calendarDao()
        val cal = lines.drop(1).mapNotNull { line ->
            val p = line.split(',')
            if (p.size < 10) return@mapNotNull null

            Calendar(
                service_id = p[0],
                monday = p[1].toInt(),
                tuesday = p[2].toInt(),
                wednesday = p[3].toInt(),
                thursday = p[4].toInt(),
                friday = p[5].toInt(),
                saturday = p[6].toInt(),
                sunday = p[7].toInt(),
                start_date = p[8],
                end_date = p[9]
            )
        }
        dao.insertAll(cal)
    }
}
