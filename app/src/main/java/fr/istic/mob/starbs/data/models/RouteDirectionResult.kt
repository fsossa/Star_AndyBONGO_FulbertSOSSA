package fr.istic.mob.starbs.data.models

data class RouteDirectionResult(
    val route_id: String,
    val route_short_name: String,
    val route_long_name: String?,
    val route_color: String?,
    val route_text_color: String?,
    val trip_headsign: String?
)
