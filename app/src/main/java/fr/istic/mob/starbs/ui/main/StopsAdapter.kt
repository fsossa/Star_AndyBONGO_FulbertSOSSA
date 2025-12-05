package fr.istic.mob.starbs.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.istic.mob.starbs.data.local.entities.Stop
import fr.istic.mob.starbs.databinding.ItemStopBinding


class StopsAdapter(
    private val stops: List<Stop>,
    private val onClick: (Stop) -> Unit
) : RecyclerView.Adapter<StopsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemStopBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(stop: Stop) {
            binding.textStopName.text = stop.stop_name
            binding.root.setOnClickListener { onClick(stop) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStopBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = stops.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stops[position])
    }
}
