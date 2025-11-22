package fr.istic.mob.starbs.ui.main

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import fr.istic.mob.starbs.data.local.entities.Route

class RouteSpinnerAdapter(
    context: Context,
    private val routes: List<Route>
) : ArrayAdapter<Route>(context, android.R.layout.simple_spinner_item, routes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return styleView(super.getView(position, convertView, parent), position)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return styleView(super.getDropDownView(position, convertView, parent), position)
    }

    private fun styleView(view: View, position: Int): View {
        val textView = view as TextView
        val r = routes[position]

        // Couleur de fond
        textView.setBackgroundColor(Color.parseColor("#${r.route_color ?: "FFFFFF"}"))

        // Couleur du texte
        val textColor = r.route_text_color ?: "000000"
        textView.setTextColor(Color.parseColor("#$textColor"))

        textView.text = r.route_short_name ?: "?"

        return textView
    }
}
