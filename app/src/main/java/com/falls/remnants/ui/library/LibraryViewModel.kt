package com.falls.remnants.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.exception.ApolloException
import com.falls.remnants.adapter.MediaViewType
import com.falls.remnants.data.AnilistQueries
import com.falls.remnants.data.Anime
import com.falls.remnants.type.MediaListSort
import com.falls.remnants.type.MediaListStatus
import kotlinx.coroutines.launch
import timber.log.Timber

class LibraryViewModel : ViewModel() {

    // Data list
    private val _animeCurrent = MutableLiveData<List<Anime>>()
    val animeCurrent: LiveData<List<Anime>>
        get() = _animeCurrent
    private val _animeLibrary = MutableLiveData<List<Anime>>()
    val animeLibrary: LiveData<List<Anime>>
        get() = _animeLibrary

    // Paging
    private var _currentPagesLoaded = 0
    private var _currentHasNextPage = true
    private var _libraryPagesLoaded = 0
    private var _libraryHasNextPage = true
    var _currentList = 0

    fun getMedia(type: MediaViewType, list: Int = 0) {
        viewModelScope.launch {
            try {
                when (type) {
                    MediaViewType.SEASONAL -> {
                        if (!_currentHasNextPage) return@launch
                        _currentPagesLoaded++

                        var (titles, nextPageExists) = AnilistQueries.library(
                            page = _currentPagesLoaded,
                        )

                        // Filter non-releasing anime
                        titles = titles.filter { it.status == "RELEASING" }

                        // Append data to any pre-existing data
                        _animeCurrent.value = _animeCurrent.value?.plus(titles) ?: titles
                        _currentHasNextPage = nextPageExists

                        // Sort list by next episode countdown
                        _animeCurrent.value = _animeCurrent.value?.sortedBy {
                            it.nextEpisodeCountdown.toInt()
                        }

                        // Iterate through whole list
                        getMedia(MediaViewType.SEASONAL)
                    }
                    MediaViewType.SEARCH -> {
                        if (!_libraryHasNextPage) return@launch
                        _libraryPagesLoaded++
                        _currentList = list

                        var specificSortingOrder: MediaListSort = MediaListSort.MEDIA_POPULARITY_DESC
                        if (list == 1) { specificSortingOrder = MediaListSort.SCORE_DESC }

                        val stringOfList = when (list) {
                            0 -> MediaListStatus.CURRENT
                            1 -> MediaListStatus.COMPLETED
                            2 -> MediaListStatus.PAUSED
                            3 -> MediaListStatus.DROPPED
                            4 -> MediaListStatus.REPEATING
                            5 -> MediaListStatus.PLANNING
                            else -> MediaListStatus.UNKNOWN__
                        }

                        val (titles, nextPageExists) = AnilistQueries.library(
                            page = _libraryPagesLoaded,
                            viewList = stringOfList,
                            order = specificSortingOrder
                        )

                        // Append data to any pre-existing data
                        _animeLibrary.value = _animeLibrary.value?.plus(titles) ?: titles
                        _libraryHasNextPage = nextPageExists

                        // Remove duplicates
                        // TODO: Figure out why duplicates are created for first page
                        _animeLibrary.value = _animeLibrary.value?.distinctBy { it.id } ?: titles

                        // Sort list in case it arrives out of order
                        _animeLibrary.value = _animeLibrary.value?.sortedByDescending {
                            it.score.toIntOrNull() ?: 0
                        }
                    }
                }
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
                getMedia(MediaViewType.SEASONAL)
            }
            MediaViewType.SEARCH -> {
                _animeLibrary.value = emptyList()
                _libraryPagesLoaded = 0
                _libraryHasNextPage = true
                getMedia(MediaViewType.SEARCH, _currentList)
            }
//            MediaViewType.TOP -> {
//                _animeTop.value = emptyList()
//                _topPagesLoaded = 0
//                _topHasNextPage = true
//                getTop()
//            }
        }
    }

    fun listChanged() {
        _animeLibrary.value = emptyList()
        _libraryPagesLoaded = 0
        _libraryHasNextPage = true
    }
}