package com.falls.remnants.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.falls.remnants.SeasonalQuery
import com.falls.remnants.data.Anime
import com.falls.remnants.data.Utils
import com.falls.remnants.databinding.FragmentSeasonalListBinding
import com.falls.remnants.networking.GraphQLapi
import com.falls.remnants.type.MediaSeason
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class TabSeasonalListViewModel(
    private val binding: FragmentSeasonalListBinding, application: Application
) : AndroidViewModel(application) {

    // List of entries
    private val _anime = MutableLiveData<List<Anime>>()
    val anime: LiveData<List<Anime>>
        get() = _anime

    // Filters
    private var _year = 2000
    private var _setSeason = MediaSeason.WINTER

    private var _navigateToDetail = MutableLiveData<Anime>()
    val navigateToDetail: LiveData<Anime>
        get() = _navigateToDetail

    private var _pagesLoaded = 0
    private var _hasNextPage = true

    init {
        currentSeason()
    }

    // Fetches the season list from the server
    fun getMedia() {
        viewModelScope.launch {

            // TODO: Implement rate limiter (max 90/min)

            // Don't run if no more pages exist
            if (!_hasNextPage) return@launch
            _pagesLoaded++

            // Execute and post query
            val response: ApolloResponse<SeasonalQuery.Data>
            try {
                response = GraphQLapi.getInstance().query(
                    SeasonalQuery(_setSeason, _year, _pagesLoaded)
                ).execute()
            } catch (e: ApolloException) {
                // TODO: Show that query failed (likely 429)
                Timber.e(e)
                return@launch
            }

            Timber.d("QUERY SENT")

            // If called again, load next page
            _hasNextPage = response.data?.page?.pageInfo?.hasNextPage == true

            // Formatting
            val titles = response.data?.page?.media?.map {
                Anime(
                    id = it?.id!!,
                    engTitle = it.title?.english?: it.title?.romaji?: "Unknown", // If no english title, use romaji
                    japTitle = it.title?.romaji?: it.title?.english?: "Unknown", // If no romaji title, use english
                    episodes = "/" + (it.episodes?: "?"),
                    nextEpisode = Utils.formatDate(
                        it.nextAiringEpisode?.timeUntilAiring?: -1,
                        it.nextAiringEpisode?.episode?: -1,
                        it.episodes?: -1,
                            it.status?.name?: ""),
                    coverPath = it.coverImage?.extraLarge?: it.coverImage?.large?: it.coverImage?.medium?: "", // XL > L > M > None
                    color = it.coverImage?.color?: "#000000",
                    format = if (it.format.toString() == "TV_SHORT") "SHORT"
                            else if (it.format.toString() == "null") "?"
                                else it.format.toString(),
                )
            } ?: listOf()

            // Append data to any pre-existing data
            _anime.value = _anime.value?.plus(titles) ?: titles
        }
    }

    // Empties list and refreshes
    fun refreshList() {
        _anime.value = listOf()
        _pagesLoaded = 0
        _hasNextPage = true

        getMedia()
    }

    fun nextSeason() {
        when (_setSeason) {
            MediaSeason.WINTER -> _setSeason = MediaSeason.SPRING
            MediaSeason.SPRING -> _setSeason = MediaSeason.SUMMER
            MediaSeason.SUMMER -> _setSeason = MediaSeason.FALL
            MediaSeason.FALL -> {_setSeason = MediaSeason.WINTER; _year++}
        }
        refreshList()
    }

    fun prevSeason() {
        when (_setSeason) {
            MediaSeason.WINTER -> {_setSeason = MediaSeason.FALL; _year--}
            MediaSeason.FALL -> _setSeason = MediaSeason.SUMMER
            MediaSeason.SUMMER -> _setSeason = MediaSeason.SPRING
            MediaSeason.SPRING -> _setSeason = MediaSeason.WINTER
        }
        refreshList()
    }

    fun getSeasonYear() : String {
        return "$_setSeason $_year"
    }

    // Gets the current season and year and sets it
    fun currentSeason() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        _year = year

        // Month is 0-indexed (0-11)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        when (month) {
            // JAN-MAR
            in 0..2 -> _setSeason = MediaSeason.WINTER
            // APR-JUN
            in 3..5 -> _setSeason = MediaSeason.SPRING
            // JUL-SEP
            in 6..8 -> _setSeason = MediaSeason.SUMMER
            // OCT-DEC
            in 9..11 -> _setSeason = MediaSeason.FALL
        }

        refreshList()
    }


    // ClickHandlers
    fun navigateToDetail(anime: Anime) {
        _navigateToDetail.value = anime
    }

    fun onNavigatedToDetail() {
        _navigateToDetail.value = null
    }
}

