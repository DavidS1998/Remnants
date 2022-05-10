package com.falls.remnants.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.falls.remnants.data.Anime
import com.falls.remnants.databinding.ItemMediaEntryBinding

// Boilerplate adapter code
class SeasonalListAdapter() : ListAdapter<Anime, SeasonalListAdapter.SeasonalListViewHolder>(SeasonalListDiffCallback()) {

    class SeasonalListViewHolder(private var binding: ItemMediaEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Anime) {
            binding.anime = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): SeasonalListViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemMediaEntryBinding.inflate(layoutInflater, parent, false)
                return SeasonalListViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: SeasonalListViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonalListViewHolder {
        return SeasonalListViewHolder.from(parent)
    }
}

class SeasonalListDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Anime>() {
    override fun areItemsTheSame(oldItem: Anime, newItem: Anime): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Anime, newItem: Anime): Boolean {
        return oldItem == newItem
    }
}