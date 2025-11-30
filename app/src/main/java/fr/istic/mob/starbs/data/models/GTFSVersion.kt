package fr.istic.mob.starbs.data.models

data class GTFSFile(
    val filename: String?,
    val url: String?,
    val format: String?
)

data class GTFSVersion(
    val id: String,
    val description: String,
    val debutvalidite: String,
    val finvalidite: String,
    val fichier: GTFSFile?,
    val url: String
)
