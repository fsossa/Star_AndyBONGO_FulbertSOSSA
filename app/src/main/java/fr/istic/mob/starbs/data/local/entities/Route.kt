package fr.istic.mob.starbs.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bus_route")
data class Route(
    @PrimaryKey val route_id: String,
    val route_short_name: String?,
    val route_long_name: String?,
    val route_type: Int?,
    val route_color: String?,
    val route_text_color: String?
)

