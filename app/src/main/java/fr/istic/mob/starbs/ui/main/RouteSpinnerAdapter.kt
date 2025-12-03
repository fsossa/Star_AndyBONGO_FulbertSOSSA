package fr.istic.mob.starbs.ui.main

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import fr.istic.mob.starbs.R
import fr.istic.mob.starbs.data.local.entities.Route
import fr.istic.mob.starbs.databinding.ItemRouteBinding

class RouteSpinnerAdapter(
    context: Context,
    private val routes: List<Route>
) : ArrayAdapter<Route>(context, 0, routes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding: ItemRouteBinding = if (convertView == null) {
            ItemRouteBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            ItemRouteBinding.bind(convertView)
        }

        val route = routes[position]

        // Nom court
        binding.textShortName.text = route.route_short_name ?: ""

        // Nom long
        binding.textLongName.text = route.route_long_name ?: ""

        // Couleur
        val colorHex = route.route_color ?: "CCCCCC"
        try {
            (binding.viewColor.background).setTint(Color.parseColor("#$colorHex"))
        } catch (e: Exception) {
            (binding.viewColor.background).setTint(Color.GRAY)
        }

        return binding.root
    }
}
