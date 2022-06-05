package com.falls.remnants.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.falls.remnants.DetailsQuery
import com.falls.remnants.data.AnilistQueries
import com.falls.remnants.data.Anime
import com.falls.remnants.databinding.FragmentAnimeDetailsBinding
import com.falls.remnants.networking.GraphQLapi
import kotlinx.coroutines.launch
import timber.log.Timber

class AnimeDetailsViewModel(
    private val binding: FragmentAnimeDetailsBinding,
    application: Application
) : AndroidViewModel(application) {

    // List of entries
    private val _anime = MutableLiveData<Anime>()
    val anime: LiveData<Anime>
        get() = _anime

    private val _relatedAnime = MutableLiveData<List<Anime>>()
    val relatedAnime: LiveData<List<Anime>>
        get() = _relatedAnime

    // Fetches the details from the server
    fun getAnimeDetails(id: Int) {
        viewModelScope.launch {
            try {
                val response = AnilistQueries.details(id)
                _anime.value = response

                // Filter out MANGA, NOVEL, and ONE_SHOT from relations formats
                val relations = response.relations.filter {
                    it.format != "MANGA" && it.format != "NOVEL" && it.format != "ONE_SHOT"
                }.sortedBy { it.id }

//                // Sort by ID
//                response.relations.sortedBy { it.id }.forEach { Timber.d("${it.id} ${it.engTitle}")}

                // Add relations
                _relatedAnime.value = relations

            } catch (e: ApolloException) {
                Timber.e(e)
            }
        }
    }
}
