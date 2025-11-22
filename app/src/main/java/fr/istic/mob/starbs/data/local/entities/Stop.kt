package fr.istic.mob.starbs.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stop")
data class Stop(
    @PrimaryKey val stop_id: String,
    val stop_name: String,
    val stop_lat: Double,
    val stop_lon: Double
)

