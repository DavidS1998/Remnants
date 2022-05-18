package com.falls.remnants.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.falls.remnants.data.Anime
import com.falls.remnants.databinding.ItemMediaEntrySeasonalBinding
import com.falls.remnants.databinding.ItemMediaEntrySearchBinding
import com.falls.remnants.databinding.ItemMediaEntryUpcomingBinding
import timber.log.Timber

// Ways to display media
enum class MediaViewType {
    SEASONAL,
    SEARCH,
    UPCOMING
}

// Generic RecyclerAdapter class for displaying a list of media objects
class MediaListAdapter(private val clickListener: AdapterClickListener, viewType: MediaViewType) :
    ListAdapter<Anime, MediaListAdapter.ViewHolder>(DiffCallback()) {
    private val mediaViewType = viewType

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Timber.d(viewType.toString() + parent.toString())
         if (mediaViewType == MediaViewType.SEASONAL) {
             return ViewHolder(
                 ItemMediaEntrySeasonalBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), mediaViewType
            )
        }
        else if (mediaViewType == MediaViewType.UPCOMING) {
            return ViewHolder(
                ItemMediaEntryUpcomingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), mediaViewType
            )
        } else {
             return ViewHolder(
                 ItemMediaEntrySearchBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), mediaViewType
            )
        }
    }

    class ViewHolder constructor(private val binding: ViewDataBinding, private val viewType: MediaViewType) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Anime, clickListener: AdapterClickListener) {
            when (viewType) {
                MediaViewType.SEARCH -> {
                    (binding as ItemMediaEntrySearchBinding).anime = item
                    binding.clickListener = clickListener
                }
                MediaViewType.SEASONAL -> {
                    (binding as ItemMediaEntrySeasonalBinding).anime = item
                    binding.clickListener = clickListener
                }
                MediaViewType.UPCOMING -> {
                    (binding as ItemMediaEntryUpcomingBinding).anime = item
                    binding.clickListener = clickListener
                }
            }
            binding.executePendingBindings()
        }
    }
}

class DiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Anime>() {
    override fun areItemsTheSame(oldItem: Anime, newItem: Anime): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Anime, newItem: Anime): Boolean {
        return oldItem == newItem
    }
}

class AdapterClickListener(val clickListener: (anime: Anime) -> Unit) {
    fun onClick(anime: Anime) = clickListener(anime)
}