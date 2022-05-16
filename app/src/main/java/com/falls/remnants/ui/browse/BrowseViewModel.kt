package com.falls.remnants.ui.browse

import androidx.lifecycle.*
import com.apollographql.apollo3.exception.ApolloException
import com.falls.remnants.data.AnilistQueries
import com.falls.remnants.data.Anime
import com.falls.remnants.recycler.MediaViewType
import com.falls.remnants.type.MediaSeason
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class BrowseViewModel : ViewModel() {

    // Data list
    private val _animeSeasonal = MutableLiveData<List<Anime>>()
    val animeSeasonal: LiveData<List<Anime>>
        get() = _animeSeasonal

    private val _animeTop = MutableLiveData<List<Anime>>()
    val animeTop: LiveData<List<Anime>>
        get() = _animeTop

    private val _animeUpcoming = MutableLiveData<List<Anime>>()
    val animeUpcoming: LiveData<List<Anime>>
        get() = _animeUpcoming

    // Seasonal filters
    private var _year = 2000
    private var _setSeason = MediaSeason.WINTER

    // Paging
    private var _seasonalPagesLoaded = 0
    private var _seasonalHasNextPage = true
    private var _topPagesLoaded = 0
    private var _topHasNextPage = true
    private var _upcomingPagesLoaded = 0
    private var _upcomingHasNextPage = true

    // Display style
    var columns = MutableLiveData(1)

    init {
        currentSeason()
    }

    fun getMedia(type: MediaViewType) {
        when (type) {
            MediaViewType.SEASONAL -> getSeasonal()
            MediaViewType.TOP -> getTop()
            MediaViewType.UPCOMING -> getUpcoming()
        }
    }

    // Fetches the season list from the server
    private fun getSeasonal() {

        viewModelScope.launch {
            // Don't run if no more pages exist
            if (!_seasonalHasNextPage) return@launch
            _seasonalPagesLoaded++

            try {
                val (titles, nextPageExists) = AnilistQueries.seasonal(
                    _setSeason,
                    _year,
                    _seasonalPagesLoaded
                )

                // Append data to any pre-existing data
                _animeSeasonal.value = _animeSeasonal.value?.plus(titles) ?: titles
                _seasonalHasNextPage = nextPageExists
            } catch (e: ApolloException) {
                // TODO: Show that query failed (likely 429)
                Timber.e(e)
                return@launch
            }
        }
    }

    private fun getTop() {

        viewModelScope.launch {
            // Don't run if no more pages exist
            if (!_topHasNextPage) return@launch
            _topPagesLoaded++

            try {
                val (titles, nextPageExists) = AnilistQueries.top(
                    _topPagesLoaded
                )

                // Append data to any pre-existing data
                _animeTop.value = _animeTop.value?.plus(titles) ?: titles
                _topHasNextPage = nextPageExists
            } catch (e: ApolloException) {
                // TODO: Show that query failed (likely 429)
                Timber.e(e)
                return@launch
            }
        }
    }


    private fun getUpcoming() {

        viewModelScope.launch {

            // Don't run if no more pages exist
            if (!_upcomingHasNextPage) return@launch
            _upcomingPagesLoaded++

            try {
                val (titles, nextPageExists) = AnilistQueries.upcoming(
                    _upcomingPagesLoaded
                )

                // Append data to any pre-existing data
                _animeUpcoming.value = _animeUpcoming.value?.plus(titles) ?: titles
                _upcomingHasNextPage = nextPageExists
            } catch (e: ApolloException) {
                // TODO: Show that query failed (likely 429)
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
                getSeasonal()
            }
            MediaViewType.UPCOMING -> {
                _animeUpcoming.value = emptyList()
                _upcomingPagesLoaded = 0
                _upcomingHasNextPage = true
                getUpcoming()
            }
            MediaViewType.TOP -> {
                _animeTop.value = emptyList()
                _topPagesLoaded = 0
                _topHasNextPage = true
                getTop()
            }
        }
    }

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

