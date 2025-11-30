package fr.istic.mob.starbs.utils

import fr.istic.mob.starbs.data.models.GTFSFile
import fr.istic.mob.starbs.data.models.GTFSVersion
import org.json.JSONArray
import java.io.File
import java.net.URL

object GTFSUtils {

    fun downloadRawJson(): String =
        URL(GTFSConstants.GTFS_JSON_URL).readText()

    fun parseVersions(json: String): List<GTFSVersion> {
        val arr = JSONArray(json)
        val list = mutableListOf<GTFSVersion>()

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)

            val fichier = if (!o.isNull("fichier")) {
                val f = o.getJSONObject("fichier")
                GTFSFile(
                    filename = f.optString("filename", null),
                    url = f.optString("url", null),
                    format = f.optString("format", null)
                )
            } else null

            list.add(
                GTFSVersion(
                    id = o.getString("id"),
                    description = o.getString("description"),
                    debutvalidite = o.getString("debutvalidite"),
                    finvalidite = o.getString("finvalidite"),
                    fichier = fichier,
                    url = o.getString("url")
                )
            )
        }

        return list
    }

    fun saveLocalJson(file: File, text: String) {
        file.writeText(text)
    }

    fun loadLocalJson(file: File): String? =
        if (file.exists()) file.readText() else null

    fun extractFinValidite(json: String?): String? {
        if (json == null) return null
        val arr = JSONArray(json)
        if (arr.length() == 0) return null
        return arr.getJSONObject(0).getString("finvalidite")
    }

    fun isGtfsExpired(finValidite: String?): Boolean {
        if (finValidite == null) return true
        val today = java.time.LocalDate.now()
        val end = java.time.LocalDate.parse(finValidite)
        return today.isAfter(end)
    }
}
