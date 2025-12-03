package fr.istic.mob.starbs.services

import android.Manifest
import android.app.IntentService
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import fr.istic.mob.starbs.utils.GTFSConstants
import fr.istic.mob.starbs.utils.GTFSUtils
import fr.istic.mob.starbs.utils.NotificationUtils
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class GTFSDownloaderService : IntentService("GTFSDownloaderService") {

    private fun sendProgress(percent: Int, msg: String) {
        val intent = Intent(GTFSParserService.ACTION_PROGRESS)
        intent.putExtra(GTFSParserService.EXTRA_PERCENT, percent)
        intent.putExtra(GTFSParserService.EXTRA_MESSAGE, msg)
        sendBroadcast(intent)
    }

    @RequiresPermission(allOf = [Manifest.permission.POST_NOTIFICATIONS])
    override fun onHandleIntent(intent: Intent?) {

        NotificationUtils.createChannel(this)

        val localJsonFile = File(filesDir, GTFSConstants.LOCAL_JSON_FILE)

        try {
            // 0% : téléchargement du JSON
            sendProgress(0, "Téléchargement JSON…")

            val rawJson = GTFSUtils.downloadRawJson()
            val versions = GTFSUtils.parseVersions(rawJson)

            if (versions.isEmpty()) {
                val msg = "Erreur : aucune version GTFS trouvée"
                sendProgress(0, msg)
                NotificationUtils.notify(this, "Erreur JSON", msg, 98)
                return
            }

            // On prend la première version (EN_COURS ou A_VENIR suivant l’API)
            val current = versions.first()

            // Lire JSON local (s’il existe)
            val localJson = GTFSUtils.loadLocalJson(localJsonFile)
            val finLocal = GTFSUtils.extractFinValidite(localJson)
            val needUpdate = GTFSUtils.isGtfsExpired(finLocal)

            if (!needUpdate) {
                // Base déjà à jour
                sendProgress(100, "GTFS déjà à jour ✔")
                // NotificationUtils.notify(this, "GTFS", "Déjà à jour ✔", 2)
                return
            }

            // 20% : téléchargement du ZIP
            sendProgress(20, "Téléchargement du ZIP…")

            val zipUrl = current.fichier?.url ?: current.url
            val zipFile = File(filesDir, "gtfs.zip")

            URL(zipUrl).openStream().use { input ->
                FileOutputStream(zipFile).use { out ->
                    input.copyTo(out)
                }
            }

            // Sauvegarder JSON local
            GTFSUtils.saveLocalJson(localJsonFile, rawJson)

            NotificationUtils.notify(
                this,
                "GTFS téléchargé",
                "Début de l’analyse…",
                3
            )

            // 30% : on enchaîne sur le parsing
            sendProgress(30, "Décompression et remplissage…")

            val parseIntent = Intent(this, GTFSParserService::class.java)
            parseIntent.putExtra("zip_path", zipFile.absolutePath)
            startService(parseIntent)

        } catch (e: Exception) {
            val err = "Erreur téléchargement : ${e.message}"
            Log.e("GTFS", err, e)
            sendProgress(0, err)
            NotificationUtils.notify(this, "Erreur téléchargement", err, 99)
        }
    }
}
