package fr.istic.mob.starbs.services

import android.Manifest
import android.app.IntentService
import android.content.Intent
import androidx.annotation.RequiresPermission
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.data.local.entities.*
import fr.istic.mob.starbs.utils.NotificationUtils
import kotlinx.coroutines.runBlocking
import java.util.zip.ZipFile

class GTFSParserService : IntentService("GTFSParserService") {

    companion object {
        const val ACTION_PROGRESS = "fr.istic.mob.starbs.GTFS_PROGRESS"
        const val EXTRA_PERCENT = "percent"
        const val EXTRA_MESSAGE = "message"
    }

    private fun sendProgress(percent: Int, msg: String) {
        val intent = Intent(ACTION_PROGRESS)
        intent.putExtra(EXTRA_PERCENT, percent)
        intent.putExtra(EXTRA_MESSAGE, msg)
        sendBroadcast(intent)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onHandleIntent(intent: Intent?) {

        NotificationUtils.createChannel(this)

        val zipPath = intent?.getStringExtra("zip_path") ?: return
        val zipFile = ZipFile(zipPath)

        val db = MainApp.database

        runBlocking {
            db.clearAllTables()

            var routesLines: List<String>? = null
            var tripsLines: List<String>? = null
            var stopsLines: List<String>? = null
            var stopTimesLines: List<String>? = null
            var calendarLines: List<String>? = null

            zipFile.entries().asSequence().forEach { entry ->
                val name = entry.name.lowercase()
                val content = zipFile.getInputStream(entry)
                    .bufferedReader()
                    .readLines()

                when {
                    name.contains("routes") -> routesLines = content
                    name.contains("trips") -> tripsLines = content
                    name.contains("stops") && !name.contains("stop_times") -> stopsLines = content
                    name.contains("stop_times") -> stopTimesLines = content
                    name.contains("calendar") -> calendarLines = content
                }
            }

            try {
                sendProgress(40, "Remplissage des routes…")
                routesLines?.let { parseRoutes(it) }

                sendProgress(60, "Remplissage des voyages…")
                tripsLines?.let { parseTrips(it) }

                sendProgress(70, "Remplissage des arrêts…")
                stopsLines?.let { parseStops(it) }

                sendProgress(90, "Remplissage des horaires…")
                stopTimesLines?.let { parseStopTimes(it) }

                calendarLines?.let {
                    parseCalendar(it)
                }
                sendProgress(100, "Base GTFS prête ✔")

                NotificationUtils.notify(
                    this@GTFSParserService,
                    "Données prête",
                    "Base de données mise à jour",
                    6
                )

            } catch (e: Exception) {
                val err = "Erreur parsing : ${e.message}"
                sendProgress(0, err)
                NotificationUtils.notify(
                    this@GTFSParserService,
                    "Erreur parsing",
                    err,
                    97
                )
            }
        }
    }

    // --- PARSERS ---

    private suspend fun parseRoutes(lines: List<String>) {
        val dao = MainApp.database.routeDao()
        if (lines.isEmpty()) return

        // Regex pour parser un CSV correctement
        val csvSplitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

        // Lire le header
        val header = lines.first().split(csvSplitRegex)
        val idxRouteId = header.indexOf("route_id")
        val idxShort = header.indexOf("route_short_name")
        val idxLong = header.indexOf("route_long_name")
        val idxType = header.indexOf("route_type")
        val idxColor = header.indexOf("route_color")
        val idxTextColor = header.indexOf("route_text_color")

        val routes = lines.drop(1).mapNotNull { line ->
            val p = line.split(csvSplitRegex)

            fun col(idx: Int): String? =
                if (idx in p.indices) p[idx].trim().trim('"') else null

            val routeId = col(idxRouteId) ?: return@mapNotNull null

            Route(
                route_id = routeId,
                route_short_name = col(idxShort),
                route_long_name = col(idxLong),
                route_type = col(idxType)?.toIntOrNull(),
                route_color = col(idxColor)?.replace("#", "").takeIf { !it.isNullOrBlank() },
                route_text_color = col(idxTextColor)?.replace("#", "").takeIf { !it.isNullOrBlank() }
            )
        }

        dao.insertAll(routes)
    }



    private suspend fun parseTrips(lines: List<String>) {
        val dao = MainApp.database.tripDao()
        val trips = lines.drop(1).mapNotNull { line ->
            val p = line.split(',')
            if (p.size < 4) return@mapNotNull null

            Trip(
                trip_id = p[2],
                route_id = p[0],
                service_id = p[1],
                trip_headsign = p[3]
            )
        }
        dao.insertAll(trips)
    }

    private suspend fun parseStops(lines: List<String>) {
        val dao = MainApp.database.stopDao()
        val stops = lines.drop(1).mapNotNull { line ->
            val p = line.split(',')
            if (p.size < 6) return@mapNotNull null

            Stop(
                stop_id = p[0],
                stop_name = p[2],
                stop_lat = p[4].toDoubleOrNull() ?: 0.0,
                stop_lon = p[5].toDoubleOrNull() ?: 0.0
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
                stop_sequence = p[4].toIntOrNull() ?: 0
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
    }
}
