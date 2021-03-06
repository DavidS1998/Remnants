package com.falls.remnants.ui.browse

import android.widget.Toast
import androidx.lifecycle.*
import com.apollographql.apollo3.exception.ApolloException
import com.falls.remnants.data.AnilistQueries
import com.falls.remnants.data.Anime
import com.falls.remnants.adapter.MediaViewType
import com.falls.remnants.data.Configs
import com.falls.remnants.type.MediaSeason
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class BrowseViewModel : ViewModel() {

    // Data list
    private val _animeSeasonal = MutableLiveData<List<Anime>>()
    val animeSeasonal: LiveData<List<Anime>>
        get() = _animeSeasonal

    private val _animeUpcoming = MutableLiveData<List<Anime>>()
    val animeUpcoming: LiveData<List<Anime>>
        get() = _animeUpcoming

    private val _animeSearch = MutableLiveData<List<Anime>>()
    val animeSearch: LiveData<List<Anime>>
        get() = _animeSearch
    var lastQuery = "SEARCH"

    // Seasonal filters
    private var _year = 2000
    var year: Int
        get() = _year
        set(value) {
            _year = value
        }

    private var _setSeason = MediaSeason.WINTER
    var setSeason: MediaSeason
        get() = _setSeason
        set(value) {
            _setSeason = value
        }

    var needsRefresh = MutableLiveData(false)

    // Paging
    private var _seasonalPagesLoaded = 0
    private var _seasonalHasNextPage = true
    private var _upcomingPagesLoaded = 0
    private var _upcomingHasNextPage = true

    // Settings
    var ShowOnlyUserAnime = false
    var ShowOnlyDubs = false

    init {
        currentSeason() // Also calls SEASONAL

        getMedia(MediaViewType.UPCOMING) // Preload
    }

    // TODO: This is likely unnecessary
    fun tempSearch(query: String) {
        viewModelScope.launch {
            try {
                val response = AnilistQueries.search(query)

                // Add % after score response
                val responseWithPercent = response.map {
                    it.copy(score = it.score + "%")
                }

                _animeSearch.value = responseWithPercent
                Timber.d("Search result: $response")
            } catch (e: ApolloException) {
                Timber.e(e)
            }
        }
    }

    fun getMedia(type: MediaViewType, query: String = "") {
        viewModelScope.launch {
            try {
                when (type) {
                    MediaViewType.SEASONAL -> {
                        // Don't run if no more pages exist
                        if (!_seasonalHasNextPage) return@launch
                        _seasonalPagesLoaded++

                        val currentSeason = _setSeason

                        // QUERY
                        val (titles, nextPageExists) = AnilistQueries.seasonal(
                            _setSeason,
                            _year,
                            _seasonalPagesLoaded
                        )

                        // View has changed, throw away the old result
                        if (currentSeason != _setSeason) return@launch

                        // Append data to any pre-existing data
                        _animeSeasonal.value = _animeSeasonal.value?.plus(titles) ?: titles
                        _seasonalHasNextPage = nextPageExists

                        // Remove duplicates
                        // TODO: Figure out why duplicates are created for first page
                        _animeSeasonal.value = _animeSeasonal.value?.distinctBy { it.id } ?: titles

                        // Sort list in case it arrives out of order
                        _animeSeasonal.value = _animeSeasonal.value?.sortedByDescending {
                            it.popularity.toIntOrNull() ?: 0
                        }

                        // Show only listed anime
                        if (ShowOnlyUserAnime) {
                            _animeSeasonal.value = _animeSeasonal.value?.filter { it.isOnList }
                            getMedia(MediaViewType.SEASONAL)
                        }

                        // Filter out non-dubbed entries
                        if (ShowOnlyDubs) {
                            _animeSeasonal.value =
                                _animeSeasonal.value?.filter { it.isDubbed } ?: titles
                        }
                    }
                    MediaViewType.UPCOMING -> {
                        // Don't run if no more pages exist
                        if (!_upcomingHasNextPage) return@launch
                        _upcomingPagesLoaded++

                        // QUERY
                        val (titles, nextPageExists) = AnilistQueries.upcoming(
                            _upcomingPagesLoaded
                        )

                        // Append data to any pre-existing data
                        _animeUpcoming.value = _animeUpcoming.value?.plus(titles) ?: titles
                        _upcomingHasNextPage = nextPageExists

                        // Sort list in case it arrives out of order
                        _animeUpcoming.value = _animeUpcoming.value?.sortedByDescending {
                            it.popularity.toIntOrNull() ?: 0
                        }

                        if (_upcomingPagesLoaded == 1) {
                            Configs.mostPopular = _animeUpcoming.value?.first()?.popularity?.toFloat() ?: 0f
                        }

                    }
                    MediaViewType.SEARCH -> {
                        val response = AnilistQueries.search(query)

                        // Add % after score response
                        val responseWithPercent = response.map {
                            it.copy(score = it.score + "%")
                        }

                        _animeSearch.value = responseWithPercent
                    }
                }
            } catch (e: ApolloException) {
                // TODO: Show that query failed (likely 429) Toast?
                Timber.e(e)
                return@launch
            }
        }
    }

    // Empties list and refreshes
    fun refreshList(type: MediaViewType) {
        when (type) {
            MediaViewType.SEASONAL -> {
                _animeSeasonal.value = emptyList()
                _seasonalPagesLoaded = 0
                _seasonalHasNextPage = true
                getMedia(MediaViewType.SEASONAL)
            }
            MediaViewType.UPCOMING -> {
                _animeUpcoming.value = emptyList()
                _upcomingPagesLoaded = 0
                _upcomingHasNextPage = true
                getMedia(MediaViewType.UPCOMING)
            }
            MediaViewType.SEARCH -> {
                if (lastQuery == "SEARCH") return
                _animeSearch.value = emptyList()
                getMedia(MediaViewType.SEARCH, query = lastQuery)
            }
        }
    }

    // Toggle show only user anime
    fun toggleShowOnlyUserAnime() {
        ShowOnlyUserAnime = !ShowOnlyUserAnime
        refreshList(MediaViewType.SEASONAL)
    }

    // Toggle show only dubbed anime
    fun toggleShowOnlyDubs() {
        ShowOnlyDubs = !ShowOnlyDubs
        refreshList(MediaViewType.SEASONAL)
    }

    // Season handling

    fun getSeasonYear(): String {
        return "$_setSeason $_year"
    }

    fun nextSeason() {
        when (_setSeason) {
            MediaSeason.WINTER -> _setSeason = MediaSeason.SPRING
            MediaSeason.SPRING -> _setSeason = MediaSeason.SUMMER
            MediaSeason.SUMMER -> _setSeason = MediaSeason.FALL
            MediaSeason.FALL -> {
                _setSeason = MediaSeason.WINTER; _year++
            }
            else -> {
                Timber.d("The flow of time has been disrupted")
            }
        }
        refreshList(MediaViewType.SEASONAL)
    }

    fun prevSeason() {
        when (_setSeason) {
            MediaSeason.WINTER -> {
                _setSeason = MediaSeason.FALL; _year--
            }
            MediaSeason.FALL -> _setSeason = MediaSeason.SUMMER
            MediaSeason.SUMMER -> _setSeason = MediaSeason.SPRING
            MediaSeason.SPRING -> _setSeason = MediaSeason.WINTER
            else -> {
                Timber.d("The flow of time has been disrupted")
            }
        }
        refreshList(MediaViewType.SEASONAL)
    }

    // Gets the current season and year and sets it
    fun currentSeason() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        _year = year

        // Month is 0-indexed (0-11)
        when (Calendar.getInstance().get(Calendar.MONTH)) {
            // JAN-MAR
            in 0..2 -> _setSeason = MediaSeason.WINTER
            // APR-JUN
            in 3..5 -> _setSeason = MediaSeason.SPRING
            // JUL-SEP
            in 6..8 -> _setSeason = MediaSeason.SUMMER
            // OCT-DEC
            in 9..11 -> _setSeason = MediaSeason.FALL
        }

        refreshList(MediaViewType.SEASONAL)
    }
}

