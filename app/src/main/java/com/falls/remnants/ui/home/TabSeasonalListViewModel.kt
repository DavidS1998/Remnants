package com.falls.remnants.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.falls.remnants.SeasonalQuery
import com.falls.remnants.data.Anime
import com.falls.remnants.databinding.FragmentSeasonalListBinding
import com.falls.remnants.networking.GraphQLapi
import com.falls.remnants.type.MediaSeason
import kotlinx.coroutines.launch
import timber.log.Timber

class TabSeasonalListViewModel(
    private val binding: FragmentSeasonalListBinding, application: Application
) : AndroidViewModel(application) {

    // List of entries
    private val _anime = MutableLiveData<List<Anime>>()
    val anime: LiveData<List<Anime>>
        get() = _anime

    // Filters
    private var _setYear = 2022
    private var _setSeason = MediaSeason.SPRING
    private var _pagesLoaded = 0
    private var _hasNextPage = true

    init {
        getMedia()
    }

    fun getMedia() {
        viewModelScope.launch {

            // TODO: Implement rate limiter (max 90/min)

            // Don't run if no more pages exist
            if (!_hasNextPage) return@launch
            _pagesLoaded++

            // Execute and post query
            val response = GraphQLapi.getInstance().query(
                SeasonalQuery(_setSeason, _setYear, _pagesLoaded)
//                TopAllQuery(_pagesLoaded)
            ).execute()

            Timber.d("QUERY SENT")

            // If called again, load next page
            _hasNextPage = response.data?.page?.pageInfo?.hasNextPage == true

            // Formatting
            val titles = response.data?.page?.media?.map {
                Anime(
                    id = it?.id!!,
                    engTitle = it.title?.english?: it.title?.romaji?: "Unknown", // If no english title, use romaji
                    japTitle = it.title?.romaji?: it.title?.english?: "Unknown", // If no romaji title, use english
                    episodes = it.episodes?: 0,
                    nextEpisode = ("Ep. " + (it.nextAiringEpisode?.episode?: "unknown")),
                    nextEpisodeCountdown = formatData(it.nextAiringEpisode?.timeUntilAiring?: -1),
                    coverPath = it.coverImage?.extraLarge?: it.coverImage?.large?: it.coverImage?.medium?: "", // XL > L > M > None
                    color = it.coverImage?.color?: "#000000",
                    format = if (it.format.toString() == "TV_SHORT") "SHORT" else it.format.toString(),
                )
            } ?: listOf()

            // Append data to any pre-existing data
            _anime.value = _anime.value?.plus(titles) ?: titles
        }
    }

    fun emptyList() {
        _anime.value = listOf()
        _pagesLoaded = 0
        _hasNextPage = true
    }

    fun formatData(timestamp: Int) : String {
        if (timestamp < 0) return ""

        val days = timestamp / (60 * 60 * 24)
        val hours = (timestamp % (60 * 60 * 24)) / (60 * 60)
        val minutes = (timestamp % (60 * 60)) / 60
        val seconds = timestamp % 60

        if (days > 1) return " in ${days} days"
        if (hours > 1) return " in ${hours} hours"
        return " in ${minutes} minutes"
    }
}

