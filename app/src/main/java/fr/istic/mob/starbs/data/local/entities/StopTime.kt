package fr.istic.mob.starbs.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stop_times")
data class StopTime(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trip_id: String,
    val arrival_time: String,
    val departure_time: String,
    val stop_id: String,
    val stop_sequence: Int
)
