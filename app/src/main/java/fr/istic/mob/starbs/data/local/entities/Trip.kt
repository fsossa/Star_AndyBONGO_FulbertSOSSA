package fr.istic.mob.starbs.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip")
data class Trip(
    @PrimaryKey val trip_id: String,
    val route_id: String,
    val service_id: String,
    val trip_headsign: String?
)

