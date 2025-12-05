package fr.istic.mob.starbs.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.istic.mob.starbs.databinding.ItemDirectionBinding

class DirectionRecyclerAdapter(
    private val directions: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<DirectionRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDirectionBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(direction: String) {
            binding.textDirection.text = direction
            binding.root.setOnClickListener { onClick(direction) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDirectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = directions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(directions[position])
    }
}
