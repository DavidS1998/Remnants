package com.falls.remnants.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.falls.remnants.data.Anime
import com.falls.remnants.data.Configs
import com.falls.remnants.databinding.*
import com.falls.remnants.type.MediaListStatus
import timber.log.Timber

// Ways to display media
enum class MediaViewType {
    SEASONAL,
    SEARCH,
    UPCOMING,
    PERSONAL,
    RELATED
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
        } else if (mediaViewType == MediaViewType.UPCOMING) {
            return ViewHolder(
                ItemMediaEntryUpcomingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), mediaViewType
            )
        } else if (mediaViewType == MediaViewType.PERSONAL) {
            return ViewHolder(
                ItemMediaEntryPersonalBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), mediaViewType
            )
        } else if (mediaViewType == MediaViewType.RELATED) {
            return ViewHolder(
                ItemMediaEntryRelatedBinding.inflate(
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

    class ViewHolder constructor(
        private val binding: ViewDataBinding,
        private val viewType: MediaViewType
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Anime, clickListener: AdapterClickListener) {
            when (viewType) {
                MediaViewType.SEARCH -> {
                    (binding as ItemMediaEntrySearchBinding).anime = item
                    binding.clickListener = clickListener

                    if (item.isOnList) {
                        binding.stripe.visibility = View.VISIBLE
                        // Color based on user status
                        when (item.userStatus) {
                            "CURRENT" -> binding.stripe.setBackgroundColor(Color.parseColor("#64DD17"))
                            "PLANNING" -> binding.stripe.setBackgroundColor(Color.parseColor("#FFEB3B"))
                            "COMPLETED" -> binding.stripe.setBackgroundColor(Color.parseColor("#42A5F5"))
                            "DROPPED" -> binding.stripe.setBackgroundColor(Color.parseColor("#F44336"))
                            else -> binding.stripe.setBackgroundColor(Color.parseColor("#FFFFFF"))
                        }
                        // No progress is the same as planning
                        if (item.userProgress == "0") {
                            binding.stripe.setBackgroundColor(Color.parseColor("#FFEB3B"))
                        }
                    } else {
                        binding.stripe.visibility = View.GONE
                    }
                }
                MediaViewType.SEASONAL -> {
                    (binding as ItemMediaEntrySeasonalBinding).anime = item
                    binding.clickListener = clickListener
                    if (item.isOnList) {
                        binding.stripe.visibility = View.VISIBLE
                        // Color based on user status
                        when (item.userStatus) {
                            "CURRENT" -> binding.stripe.setBackgroundColor(Color.parseColor("#64DD17"))
                            "PLANNING" -> binding.stripe.setBackgroundColor(Color.parseColor("#FFEB3B"))
                            "COMPLETED" -> binding.stripe.setBackgroundColor(Color.parseColor("#42A5F5"))
                            "DROPPED" -> binding.stripe.setBackgroundColor(Color.parseColor("#F44336"))
                            else -> binding.stripe.setBackgroundColor(Color.parseColor("#FFFFFF"))
                        }
                        // No progress is the same as planning
                        if (item.userProgress == "0") {
                            binding.stripe.setBackgroundColor(Color.parseColor("#FFEB3B"))
                        }
                    } else {
                        binding.stripe.visibility = View.GONE
                    }
                }
                MediaViewType.UPCOMING -> {
                    (binding as ItemMediaEntryUpcomingBinding).anime = item
                    binding.clickListener = clickListener
                    // Adjust binding.stripe color intensity based on percentage of interest
                    val interest = item.popularity.toFloat() / Configs.mostPopular
                    val color = Color.parseColor("#FF5F1F")
                    val red = Color.red(color)
                    val green = Color.green(color)
                    val blue = Color.blue(color)
                    val alpha = Color.alpha(color)
                    val newColor = Color.argb(
                        alpha,
                        (red * interest).toInt(),
                        (green * interest).toInt(),
                        (blue * interest).toInt()
                    )
                    binding.stripe.setBackgroundColor(newColor)
                }
                MediaViewType.PERSONAL -> {
                    (binding as ItemMediaEntryPersonalBinding).anime = item
                    binding.clickListener = clickListener

                    // Color stripe by next episode countdown, the lower the brighter, with the brightest at 604800 (1 week)
                    val color = Color.parseColor("#42A5F5")
                    val red = Color.red(color)
                    val green = Color.green(color)
                    val blue = Color.blue(color)
                    val alpha = Color.alpha(color)
                    val newColor = Color.argb(
                        alpha,
                        (red * (604800 - item.nextEpisodeCountdown.toInt()) / 604800),
                        (green * (604800 - item.nextEpisodeCountdown.toInt()) / 604800),
                        (blue * (604800 - item.nextEpisodeCountdown.toInt()) / 604800)
                    )
                    binding.stripe.setBackgroundColor(newColor)

                    // Color text based on whether the user has caught up
                    if ((item.latestEpisode.toIntOrNull()
                            ?.minus(1)).toString() == item.userProgress
                    ) {
                        binding.type.setTextColor(Color.parseColor("#64DD17"))
                    } else {
                        // If watched episode is above 0, color yellow
                        if (item.userProgress.toIntOrNull() != null && item.userProgress.toInt() > 0) {
                            binding.type.setTextColor(Color.parseColor("#FFEB3B"))
                        } else {
                            binding.type.setTextColor(Color.parseColor("#FF5252"))
                        }
                    }
                }
                MediaViewType.RELATED -> {
                    (binding as ItemMediaEntryRelatedBinding).anime = item
                    binding.clickListener = clickListener

                    if (item.isOnList) {
                        binding.stripe.visibility = View.VISIBLE
                        // Color based on user status
                        when (item.userStatus) {
                            "CURRENT" -> binding.stripe.setBackgroundColor(Color.parseColor("#64DD17"))
                            "PLANNING" -> binding.stripe.setBackgroundColor(Color.parseColor("#FFEB3B"))
                            "COMPLETED" -> binding.stripe.setBackgroundColor(Color.parseColor("#42A5F5"))
                            "DROPPED" -> binding.stripe.setBackgroundColor(Color.parseColor("#F44336"))
                            else -> binding.stripe.setBackgroundColor(Color.parseColor("#FFFFFF"))
                        }
                        // No progress is the same as planning
                        if (item.userProgress == "0") {
                            binding.stripe.setBackgroundColor(Color.parseColor("#FFEB3B"))
                        }
                    } else {
                        binding.stripe.visibility = View.GONE
                    }
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