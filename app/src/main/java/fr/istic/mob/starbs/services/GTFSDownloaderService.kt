package fr.istic.mob.starbs.services

import android.Manifest
import android.app.IntentService
import android.content.Intent
import androidx.annotation.RequiresPermission
import fr.istic.mob.starbs.utils.NotificationUtils
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class GTFSDownloaderService : IntentService("GTFSDownloaderService") {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onHandleIntent(intent: Intent?) {

        NotificationUtils.createChannel(this)

        val url = "https://data.explore.star.fr/explore/dataset/tco-busmetro-horaires-gtfs-versions-td/export/"

        val outputFile = File(cacheDir, "gtfs.zip")

        NotificationUtils.notify(
            this,
            "Téléchargement GTFS",
            "Téléchargement en cours…",
            1
        )

        try {
            URL(url).openStream().use { input ->
                FileOutputStream(outputFile).use { out ->
                    input.copyTo(out)
                }
            }

            NotificationUtils.notify(
                this,
                "GTFS téléchargé",
                "Début de l’analyse…",
                2
            )

            // Lancer parsing automatique
            val parseIntent = Intent(this, GTFSParserService::class.java)
            parseIntent.putExtra("zip_path", outputFile.absolutePath)
            startService(parseIntent)

        } catch (e: Exception) {
            NotificationUtils.notify(
                this,
                "Erreur",
                "Téléchargement impossible",
                99
            )
        }
    }
}