package com.falls.remnants.ui.tools

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.exception.ApolloException
import com.falls.remnants.adapter.MediaViewType
import com.falls.remnants.data.AnilistQueries
import com.falls.remnants.data.Anime
import com.falls.remnants.data.Configs
import com.falls.remnants.type.MediaListStatus
import com.falls.remnants.type.MediaRelation
import com.falls.remnants.type.MediaSeason
import kotlinx.coroutines.launch
import timber.log.Timber

class ToolsViewModel : ViewModel() {

    // Data list
    var status = MutableLiveData(false)
    private var relationsIDList = mutableListOf<Int>()
    private var watchedIDList = mutableListOf<Int>()
    private var blockedIDList = mutableListOf<Int>()

    private val _animeRemnants = MutableLiveData<List<Anime>>()
    val animeRemnants: LiveData<List<Anime>>
        get() = _animeRemnants

    // Paging
    private var _remnantsPagesLoaded = 0
    private var _remnantsHasNextPage = true

    // Filters
    // List of MediaRelation
    private var _blockedMediaRelations = mutableListOf(MediaRelation.CHARACTER, MediaRelation.SUMMARY)

    // OTHER, ADAPTATION, SOURCE, COMPILATION, CONTAINS
    // SEQUEL, PREQUEL
    // PARENT, SIDE_STORY
    // CHARACTER
    // SUMMARY
    // ALTERNATIVE
    // SPIN_OFF

    init {
        getMedia() // Preload
    }

    // Apply filters on downloaded list
    private fun filterMedia() {
        status.value = true

        var tempList = mutableListOf<Anime>()

        // Insert relations into list
        _animeRemnants.value?.forEach {
            it.relations.forEach { it2 ->
                relationsIDList.add(it2.id)
            }
        }

        // Insert watched into list
        _animeRemnants.value?.forEach {
            watchedIDList.add(it.id)
        }

//        // Iterate through remnants, filter if its relations with the format TV are in idList
//        _animeRemnants.value?.forEach {
//            var hasUnwatched = false
//            for (rel in it.relations) {
//                // Show only finished anime
//                if (rel.type != "ANIME") continue
//                if (rel.status != "FINISHED") continue
//                // Filter certain types of relations
//                if (_blockedMediaRelations.toString().contains(rel.relationType)) continue
//                if (watchedIDList.contains(rel.id)) continue
//                // Timber.d("Found unwatched anime: ${rel.engTitle}")
//                hasUnwatched = true
//            }
//            if (hasUnwatched) tempList.add(it)
//        }
//
//        _animeRemnants.value = tempList

        // Iterate through remnants,
        _animeRemnants.value?.forEach {
            for (rel in it.relations) {
                // Show only finished anime
                if (rel.type != "ANIME") continue
                if (rel.status != "FINISHED") continue
                if (rel.userStatus == "DROPPED") continue

                // Filter certain types of relations
                if (_blockedMediaRelations.toString().contains(rel.relationType)) continue
                if (watchedIDList.contains(rel.id)) continue
                // Timber.d("Found unwatched anime: ${rel.engTitle}")

                tempList.add(Anime(
                    id = rel.id,
                    engTitle = rel.engTitle,
                    coverPath = rel.coverPath,
                    type = rel.type,
                    status = rel.status,
                    relationType = rel.relationType,
                    format = rel.format,
                ))
            }
        }
        _animeRemnants.value = tempList
    }


    fun getMedia() {
        viewModelScope.launch {
            try {
                // Don't run if no more pages exist
                if (!_remnantsHasNextPage) {
                    filterMedia()
                    return@launch
                }
                _remnantsPagesLoaded++

                // QUERY
                val (titles, nextPageExists) = AnilistQueries.remnants(
                    _remnantsPagesLoaded
                )

                // Append data to any pre-existing data
                _animeRemnants.value = _animeRemnants.value?.plus(titles) ?: titles
                _remnantsHasNextPage = nextPageExists

                // Remove duplicates
                // TODO: Figure out why duplicates are created for first page
                _animeRemnants.value = _animeRemnants.value?.distinctBy { it.id } ?: titles

                // Sort list in case it arrives out of order
                _animeRemnants.value = _animeRemnants.value?.sortedByDescending {
                    it.popularity.toIntOrNull() ?: 0
                }

                // Iterate through whole list
                getMedia()

            } catch (e: ApolloException) {
                // TODO: Show that query failed (likely 429) Toast?
                Timber.e(e)
                return@launch
            }
        }
    }

    fun refreshList() {
        _animeRemnants.value = emptyList()
        _remnantsPagesLoaded = 0
        _remnantsHasNextPage = true
        status.value = false
        getMedia()
    }
}