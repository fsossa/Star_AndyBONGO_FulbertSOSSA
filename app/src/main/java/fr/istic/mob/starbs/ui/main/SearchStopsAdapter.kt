package fr.istic.mob.starbs.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.istic.mob.starbs.data.models.StopSearchResult
import fr.istic.mob.starbs.databinding.ItemStopSearchBinding

class SearchStopsAdapter(
    private var items: List<StopSearchResult>,
    private val onClick: (StopSearchResult) -> Unit
) : RecyclerView.Adapter<SearchStopsAdapter.VH>() {

    inner class VH(val binding: ItemStopSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StopSearchResult) {
            binding.textStopName.text = item.stop_name
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStopSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    fun submit(newItems: List<StopSearchResult>) {
        items = newItems
        notifyDataSetChanged()
    }
}
