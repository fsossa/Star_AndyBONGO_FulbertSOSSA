package fr.istic.mob.starbs.ui.components

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

fun Spinner.setOnItemSelectedListener(onSelected: (parent: AdapterView<*>, view: View?, position: Int, id: Long) -> Unit) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            onSelected(parent, view, position, id)
        }
        override fun onNothingSelected(parent: AdapterView<*>) {}
    }
}
