package com.falls.remnants.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.falls.remnants.DetailsQuery
import com.falls.remnants.SeasonalQuery
import com.falls.remnants.data.Anime
import com.falls.remnants.data.Utils
import com.falls.remnants.databinding.FragmentAnimeDetailsBinding
import com.falls.remnants.networking.GraphQLapi
import kotlinx.coroutines.launch
import timber.log.Timber

class AnimeDetailsViewModel(private val binding: FragmentAnimeDetailsBinding, application: Application) : AndroidViewModel(application) {

    // List of entries
    private val _anime = MutableLiveData<Anime>()
    val anime: LiveData<Anime>
        get() = _anime

    // Fetches the season list from the server
    fun getAnimeDetails(id: Int) {
        viewModelScope.launch {

            // Execute and post query
            val response: ApolloResponse<DetailsQuery.Data>
            try {
                response = GraphQLapi.getInstance().query(
                    DetailsQuery(id)
                ).execute()
            } catch (e: ApolloException) {
                Timber.e(e)
                return@launch
            }

            Timber.d("DETAILS QUERY SENT")

            // Formatting
            val data = response.data?.anime
            val formatted = Anime(
                    id = data?.id ?: -1,
                    engTitle = data?.title?.english ?: "",
                    japTitle = data?.title?.romaji ?: "",
                    score = if ((data?.averageScore).toString() == "null") "N/A" else data?.averageScore.toString(),
                    bannerPath = data?.bannerImage ?: "",
                    coverPath = data?.coverImage?.extraLarge?: data?.coverImage?.large ?: data?.coverImage?.medium ?: "",
                    description = data?.description ?: "",
                )

            // Append data to any pre-existing data
            _anime.value = formatted
        }
    }

}
