package fr.istic.mob.starbs.services

import android.Manifest
import android.app.IntentService
import android.content.Intent
import android.widget.Toast
import androidx.annotation.RequiresPermission
import fr.istic.mob.starbs.utils.GTFSConstants
import fr.istic.mob.starbs.utils.GTFSUtils
import fr.istic.mob.starbs.utils.NotificationUtils
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import android.util.Log


class GTFSDownloaderService : IntentService("GTFSDownloaderService") {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onHandleIntent(intent: Intent?) {

        NotificationUtils.createChannel(this)

        val localJsonFile = File(filesDir, GTFSConstants.LOCAL_JSON_FILE)

        try {
            // Télécharger JSON brut
            NotificationUtils.notify(this, "GTFS", "Téléchargement JSON…", 1)
            val rawJson = GTFSUtils.downloadRawJson()
            val versions = GTFSUtils.parseVersions(rawJson)

            if (versions.isEmpty()) {
                NotificationUtils.notify(this, "Erreur JSON", "Aucune version trouvée", 98)
                return
            }

            // On prend la première version (EN_COURS)
            val current = versions.first()

            // Lire JSON local (si existe)
            val localJson = GTFSUtils.loadLocalJson(localJsonFile)
            val finLocal = GTFSUtils.extractFinValidite(localJson)

            val needUpdate = GTFSUtils.isGtfsExpired(finLocal)

            if (!needUpdate) {
                NotificationUtils.notify(this, "GTFS", "Déjà à jour ✔", 2)
                return
            }

            // Télécharger le ZIP GTFS depuis l’URL du JSON
            NotificationUtils.notify(this, "GTFS", "Téléchargement ZIP…", 3)

            val zipUrl = current.fichier?.url ?: current.url
            val zipFile = File(filesDir, "gtfs_star.zip")

            URL(zipUrl).openStream().use { input ->
                FileOutputStream(zipFile).use { out ->
                    input.copyTo(out)
                }
            }

            // Sauvegarder le JSON localement
            GTFSUtils.saveLocalJson(localJsonFile, rawJson)

            NotificationUtils.notify(this, "GTFS", "ZIP téléchargé ✔", 4)

            // Lancer le parsing
            val parseIntent = Intent(this, GTFSParserService::class.java)
            parseIntent.putExtra("zip_path", zipFile.absolutePath)
            startService(parseIntent)

        } catch (e: Exception) {

            val fullMessage = e.stackTraceToString()  // message COMPLET

            Log.e("GTFS", "Erreur complète: $fullMessage")

            NotificationUtils.notify(
                this,
                "Erreur téléchargement",
                fullMessage.take(200), // notification max 200 chars
                99
            )

            Toast.makeText(this, e.message ?: "Erreur inconnue", Toast.LENGTH_LONG).show()
        }

    }
}
