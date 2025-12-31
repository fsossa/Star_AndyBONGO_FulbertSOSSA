package fr.istic.mob.starbs.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.istic.mob.starbs.databinding.ItemTimeBinding

class TimesAdapter(
    private val times: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<TimesAdapter.VH>() {

    inner class VH(private val binding: ItemTimeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(time: String) {
            binding.textTime.text = time
            binding.root.setOnClickListener { onClick(time) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(times[position])
    override fun getItemCount(): Int = times.size
}
