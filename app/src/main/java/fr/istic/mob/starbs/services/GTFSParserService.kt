package fr.istic.mob.starbs.services

import android.Manifest
import android.app.IntentService
import android.content.Intent
import androidx.annotation.RequiresPermission
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.data.local.entities.Calendar
import fr.istic.mob.starbs.data.local.entities.Route
import fr.istic.mob.starbs.data.local.entities.Stop
import fr.istic.mob.starbs.data.local.entities.StopTime
import fr.istic.mob.starbs.data.local.entities.Trip
import fr.istic.mob.starbs.utils.NotificationUtils
import kotlinx.coroutines.runBlocking
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class GTFSParserService : IntentService("GTFSParserService") {

    companion object {
        const val ACTION_PROGRESS = "fr.istic.mob.starbs.GTFS_PROGRESS"
        const val EXTRA_PERCENT = "percent"
        const val EXTRA_MESSAGE = "message"
    }

    private fun sendProgress(percent: Int, msg: String) {
        val intent = Intent(ACTION_PROGRESS).apply {
            setPackage(packageName) // broadcast explicite vers app
            putExtra(EXTRA_PERCENT, percent)
            putExtra(EXTRA_MESSAGE, msg)
        }
        sendBroadcast(intent)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onHandleIntent(intent: Intent?) {
        NotificationUtils.createChannel(this)

        val zipPath = intent?.getStringExtra("zip_path") ?: return
        val zipFile = ZipFile(zipPath)

        val db = MainApp.database

        runBlocking {
            try {
                db.clearAllTables()

                // 1) Repérer les entries dans le ZIP sans tout charger en mémoire
                val entries = zipFile.entries().asSequence().toList()

                fun findEntryContains(keyword: String): ZipEntry? =
                    entries.firstOrNull { it.name.lowercase().contains(keyword) }

                val routesEntry = findEntryContains("routes")
                val tripsEntry = findEntryContains("trips")
                val stopsEntry = entries.firstOrNull {
                    val n = it.name.lowercase()
                    n.contains("stops") && !n.contains("stop_times")
                }
                val stopTimesEntry = findEntryContains("stop_times")
                val calendarEntry = findEntryContains("calendar")

                // 2) Parse en streaming + batch inserts
                sendProgress(35, "Remplissage des routes…")
                routesEntry?.let { parseRoutes(zipFile, it) }

                sendProgress(55, "Remplissage des voyages…")
                tripsEntry?.let { parseTrips(zipFile, it) }

                sendProgress(70, "Remplissage des arrêts…")
                stopsEntry?.let { parseStops(zipFile, it) }

                sendProgress(82, "Remplissage du calendrier…")
                calendarEntry?.let { parseCalendar(zipFile, it) }

                sendProgress(90, "Remplissage des horaires…")
                stopTimesEntry?.let { parseStopTimes(zipFile, it) }

                sendProgress(100, "Base GTFS prête")

                NotificationUtils.notify(
                    this@GTFSParserService,
                    "Données prêtes",
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
            } finally {
                try {
                    zipFile.close()
                } catch (_: Exception) {}
            }
        }
    }

    // -------------------------
    // PARSERS (STREAMING)
    // -------------------------

    /**
     * Split CSV qui gère les champs entre guillemets (utile pour routes).
     * NOTE: pour stop_times / trips / stops / calendar, le split simple suffit.
     */
    private val csvSplitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

    private suspend fun parseRoutes(zip: ZipFile, entry: ZipEntry) {
        val dao = MainApp.database.routeDao()

        zip.getInputStream(entry).bufferedReader().useLines { lines ->
            val it = lines.iterator()
            if (!it.hasNext()) return@useLines

            // header
            val header = it.next().split(csvSplitRegex)
            val idxRouteId = header.indexOf("route_id")
            val idxShort = header.indexOf("route_short_name")
            val idxLong = header.indexOf("route_long_name")
            val idxType = header.indexOf("route_type")
            val idxColor = header.indexOf("route_color")
            val idxTextColor = header.indexOf("route_text_color")

            fun col(p: List<String>, idx: Int): String? =
                if (idx in p.indices) p[idx].trim().trim('"').takeIf { it.isNotBlank() } else null

            val buffer = ArrayList<Route>(500)
            val batchSize = 500

            while (it.hasNext()) {
                val line = it.next()
                if (line.isBlank()) continue

                val p = line.split(csvSplitRegex)
                val routeId = col(p, idxRouteId) ?: continue

                buffer.add(
                    Route(
                        route_id = routeId,
                        route_short_name = col(p, idxShort),
                        route_long_name = col(p, idxLong),
                        route_type = col(p, idxType)?.toIntOrNull(),
                        route_color = col(p, idxColor)?.replace("#", ""),
                        route_text_color = col(p, idxTextColor)?.replace("#", "")
                    )
                )

                if (buffer.size >= batchSize) {
                    dao.insertAll(buffer)
                    buffer.clear()
                }
            }

            if (buffer.isNotEmpty()) dao.insertAll(buffer)
        }
    }

    private suspend fun parseTrips(zip: ZipFile, entry: ZipEntry) {
        val dao = MainApp.database.tripDao()

        zip.getInputStream(entry).bufferedReader().useLines { lines ->
            val it = lines.iterator()
            if (it.hasNext()) it.next() // skip header

            val buffer = ArrayList<Trip>(1000)
            val batchSize = 1000

            while (it.hasNext()) {
                val line = it.next()
                if (line.isBlank()) continue

                val p = line.split(',')
                if (p.size < 4) continue

                buffer.add(
                    Trip(
                        trip_id = p[2],
                        route_id = p[0],
                        service_id = p[1],
                        trip_headsign = p[3]
                    )
                )

                if (buffer.size >= batchSize) {
                    dao.insertAll(buffer)
                    buffer.clear()
                }
            }

            if (buffer.isNotEmpty()) dao.insertAll(buffer)
        }
    }

    private suspend fun parseStops(zip: ZipFile, entry: ZipEntry) {
        val dao = MainApp.database.stopDao()

        zip.getInputStream(entry).bufferedReader().useLines { lines ->
            val it = lines.iterator()
            if (it.hasNext()) it.next() // skip header

            val buffer = ArrayList<Stop>(1000)
            val batchSize = 1000

            while (it.hasNext()) {
                val line = it.next()
                if (line.isBlank()) continue

                val p = line.split(',')
                if (p.size < 6) continue

                buffer.add(
                    Stop(
                        stop_id = p[0],
                        stop_name = p[2],
                        stop_lat = p[4].toDoubleOrNull() ?: 0.0,
                        stop_lon = p[5].toDoubleOrNull() ?: 0.0
                    )
                )

                if (buffer.size >= batchSize) {
                    dao.insertAll(buffer)
                    buffer.clear()
                }
            }

            if (buffer.isNotEmpty()) dao.insertAll(buffer)
        }
    }

    private suspend fun parseStopTimes(zip: ZipFile, entry: ZipEntry) {
        val dao = MainApp.database.stopTimeDao()

        zip.getInputStream(entry).bufferedReader().useLines { lines ->
            val it = lines.iterator()
            if (it.hasNext()) it.next() // skip header

            val buffer = ArrayList<StopTime>(2000)
            val batchSize = 2000

            while (it.hasNext()) {
                val line = it.next()
                if (line.isBlank()) continue

                // stop_times GTFS est "simple" => split(',') OK et beaucoup plus rapide que regex
                val p = line.split(',')
                if (p.size < 5) continue

                buffer.add(
                    StopTime(
                        trip_id = p[0],
                        arrival_time = p[1],
                        departure_time = p[2],
                        stop_id = p[3],
                        stop_sequence = p[4].toIntOrNull() ?: 0
                    )
                )

                if (buffer.size >= batchSize) {
                    dao.insertAll(buffer)
                    buffer.clear()
                }
            }

            if (buffer.isNotEmpty()) dao.insertAll(buffer)
        }
    }

    private suspend fun parseCalendar(zip: ZipFile, entry: ZipEntry) {
        val dao = MainApp.database.calendarDao()

        zip.getInputStream(entry).bufferedReader().useLines { lines ->
            val it = lines.iterator()
            if (it.hasNext()) it.next() // skip header

            val buffer = ArrayList<Calendar>(500)
            val batchSize = 500

            while (it.hasNext()) {
                val line = it.next()
                if (line.isBlank()) continue

                val p = line.split(',')
                if (p.size < 10) continue

                buffer.add(
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
                )

                if (buffer.size >= batchSize) {
                    dao.insertAll(buffer)
                    buffer.clear()
                }
            }

            if (buffer.isNotEmpty()) dao.insertAll(buffer)
        }
    }
}
