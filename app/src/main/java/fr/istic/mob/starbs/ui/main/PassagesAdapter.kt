package fr.istic.mob.starbs.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.istic.mob.starbs.data.models.PassageRow
import fr.istic.mob.starbs.databinding.ItemPassageBinding

class PassagesAdapter(
    private val rows: List<PassageRow>
) : RecyclerView.Adapter<PassagesAdapter.VH>() {

    inner class VH(private val binding: ItemPassageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: PassageRow) {
            binding.textTime.text = row.departure_time
            binding.textStop.text = row.stop_name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPassageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(rows[position])
    override fun getItemCount(): Int = rows.size
}
