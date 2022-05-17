package com.falls.remnants.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.exception.ApolloException
import com.falls.remnants.adapter.MediaViewType
import com.falls.remnants.data.AnilistQueries
import com.falls.remnants.data.Anime
import com.falls.remnants.type.MediaSeason
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class LibraryViewModel : ViewModel() {

    // Data list
    private val _animeCurrent = MutableLiveData<List<Anime>>()
    val animeCurrent: LiveData<List<Anime>>
        get() = _animeCurrent

    // Paging
    private var _currentPagesLoaded = 0
    private var _currentHasNextPage = true

    // Display style
    var columns = MutableLiveData(1)


    fun getMedia(type: MediaViewType) {
        when (type) {
            MediaViewType.SEASONAL -> getCurrent()
        }
    }

    // Fetches the season list from the server
    private fun getCurrent() {

        viewModelScope.launch {
            // Don't run if no more pages exist
            if (!_currentHasNextPage) return@launch
            _currentPagesLoaded++

            try {
                val (titles, nextPageExists) = AnilistQueries.current(
                    _currentPagesLoaded
                )


                // Append data to any pre-existing data
                _animeCurrent.value = _animeCurrent.value?.plus(titles) ?: titles
                _currentHasNextPage = nextPageExists
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
                _animeCurrent.value = emptyList()
                _currentPagesLoaded = 0
                _currentHasNextPage = true
                getCurrent()
            }
//            MediaViewType.UPCOMING -> {
//                _animeUpcoming.value = emptyList()
//                _upcomingPagesLoaded = 0
//                _upcomingHasNextPage = true
//                getUpcoming()
//            }
//            MediaViewType.TOP -> {
//                _animeTop.value = emptyList()
//                _topPagesLoaded = 0
//                _topHasNextPage = true
//                getTop()
//            }
        }
    }
}