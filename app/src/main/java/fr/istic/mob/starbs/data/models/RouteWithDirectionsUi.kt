package fr.istic.mob.starbs.data.models

data class RouteWithDirectionsUi(
    val routeId: String,
    val shortName: String,
    val longName: String?,
    val color: String?,
    val textColor: String?,
    val directions: List<String>
)
