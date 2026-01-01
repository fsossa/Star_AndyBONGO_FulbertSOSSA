package fr.istic.mob.starbs.ui.search

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import fr.istic.mob.starbs.databinding.ItemRouteDirectionsBinding

data class RouteWithDirectionsUi(
    val routeId: String,
    val shortName: String,
    val longName: String?,
    val color: String?,
    val textColor: String?,
    val directions: List<String>
)

class StopRoutesAdapter(
    private val items: List<RouteWithDirectionsUi>,
    private val onDirectionClick: (routeId: String, direction: String) -> Unit
) : RecyclerView.Adapter<StopRoutesAdapter.VH>() {

    inner class VH(private val binding: ItemRouteDirectionsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RouteWithDirectionsUi) {

            // Titre
            binding.textRouteHeader.text =
                "${item.shortName} — ${item.longName ?: ""}".trim()

            // Couleurs
            val bg = parseColorOrNull(item.color) ?: Color.LTGRAY
            val fg = parseColorOrNull(item.textColor) ?: Color.BLACK
            binding.root.setCardBackgroundColor(bg)
            binding.textRouteHeader.setTextColor(fg)

            // Directions (clear puis rebuild)
            binding.containerDirections.removeAllViews()

            item.directions.forEach { dir ->
                val tv = TextView(binding.root.context).apply {
                    text = "• $dir"
                    textSize = 16f
                    setTextColor(fg)
                    setPadding(16)
                    setOnClickListener { onDirectionClick(item.routeId, dir) }
                }
                binding.containerDirections.addView(tv)
            }
        }

        private fun parseColorOrNull(hex: String?): Int? {
            if (hex.isNullOrBlank()) return null
            return try {
                val fixed = if (hex.startsWith("#")) hex else "#$hex"
                Color.parseColor(fixed)
            } catch (_: Exception) {
                null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemRouteDirectionsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
