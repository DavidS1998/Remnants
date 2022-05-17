package com.falls.remnants.data

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.falls.remnants.SeasonalQuery
import com.falls.remnants.TopAllQuery
import com.falls.remnants.UpcomingQuery
import com.falls.remnants.UserAiringQuery
import com.falls.remnants.networking.GraphQLapi
import com.falls.remnants.type.MediaSeason
import timber.log.Timber

object AnilistQueries {

    suspend fun seasonal(season: MediaSeason, year: Int, page: Int) : Pair<List<Anime>, Boolean> {
        // Execute and post query
        val response: ApolloResponse<SeasonalQuery.Data> = GraphQLapi.getInstance()
                .query(SeasonalQuery(season, year, page))
                .execute()

        Timber.d("QUERY SENT, PAGE: $page")

        // Formatting
        val titles = response.data?.page?.media?.mapNotNull {
            Anime(

                id = it?.id!!,
                engTitle = it.title?.english ?: it.title?.romaji ?: "",
                japTitle =  if (it.title?.romaji.equals(it.title?.english ?: it.title?.romaji)) "" // If same, empty
                    else it.title?.romaji?: "",
                episodes = "/" + (it.episodes?: "?"),
                nextEpisode = Utils.formatDate(
                    it.nextAiringEpisode?.timeUntilAiring?: -1,
                    it.nextAiringEpisode?.episode?: -1,
                    it.episodes?: -1,
                    it.status?.name?: ""),
                coverPath = it.coverImage?.extraLarge?: it.coverImage?.large?: it.coverImage?.medium?: "", // XL > L > M > None
                format = when {
                    it.format.toString() == "TV_SHORT" -> "SHORT"
                    it.format.toString() == "null" -> "?"
                    else -> it.format.toString()
                },
            )
        } ?: listOf()

        return Pair(titles, response.data?.page?.pageInfo?.hasNextPage == true)
    }

    suspend fun upcoming(page: Int) : Pair<List<Anime>, Boolean> {
        // Execute and post query
        val response: ApolloResponse<UpcomingQuery.Data> = GraphQLapi.getInstance()
            .query(UpcomingQuery(page))
            .execute()

        Timber.d("QUERY SENT, PAGE: $page")

        // Formatting
        val titles = response.data?.page?.media?.mapNotNull {
            Anime(

                id = it?.id!!,
                engTitle = it.title?.english ?: it.title?.romaji ?: "",
                japTitle =  if (it.title?.romaji.equals(it.title?.english ?: it.title?.romaji)) "" // If same, empty
                else it.title?.romaji?: "",
                popularity = it.popularity?.toString() ?: "0",

                coverPath = it.coverImage?.extraLarge?: it.coverImage?.large?: it.coverImage?.medium?: "", // XL > L > M > None

                format = when {
                    it.format.toString() == "TV_SHORT" -> "SHORT"
                    it.format.toString() == "null" -> "?"
                    else -> it.format.toString()
                },
            )
        } ?: listOf()

        return Pair(titles, response.data?.page?.pageInfo?.hasNextPage == true)
    }

    suspend fun top(page: Int) : Pair<List<Anime>, Boolean> {
        // Execute and post query
        val response: ApolloResponse<TopAllQuery.Data> = GraphQLapi.getInstance()
            .query(TopAllQuery(page))
            .execute()

        Timber.d("QUERY SENT, PAGE: $page")

        // Formatting
        val titles = response.data?.page?.media?.mapNotNull {
            Anime(

                id = it?.id!!,
                engTitle = it.title?.english ?: it.title?.romaji ?: "",
                japTitle =  if (it.title?.romaji.equals(it.title?.english ?: it.title?.romaji)) "" // If same, empty
                else it.title?.romaji?: "",
                score = it.averageScore?.toString() ?: "N/A",

                coverPath = it.coverImage?.extraLarge?: it.coverImage?.large?: it.coverImage?.medium?: "", // XL > L > M > None

                format = when {
                    it.format.toString() == "TV_SHORT" -> "SHORT"
                    it.format.toString() == "null" -> "?"
                    else -> it.format.toString()
                },
            )
        } ?: listOf()

        return Pair(titles, response.data?.page?.pageInfo?.hasNextPage == true)
    }


    suspend fun current(page: Int) : Pair<List<Anime>, Boolean> {
        // Execute and post query
        val response: ApolloResponse<UserAiringQuery.Data> = GraphQLapi.getInstance()
            .query(UserAiringQuery(page))
            .execute()

        Timber.d("QUERY SENT, PAGE: $page")

        // Formatting
        var titles = response.data?.page?.mediaList?.mapNotNull { it?.media }?.map {
            Anime(
                id = it.id,
                engTitle = it.title?.english ?: it.title?.romaji ?: "",
                japTitle =  if (it.title?.romaji.equals(it.title?.english ?: it.title?.romaji)) "" // If same, empty
                else it.title?.romaji?: "",
                episodes = "/" + (it.episodes?: "?"),
                status = it.status?.toString() ?: "?",
                nextEpisode = Utils.formatDate(
                    it.nextAiringEpisode?.timeUntilAiring?: -1,
                    it.nextAiringEpisode?.episode?: -1,
                    it.episodes?: -1,
                    it.status?.name?: ""),
                coverPath = it.coverImage?.extraLarge?: it.coverImage?.large?: it.coverImage?.medium?: "", // XL > L > M > None
                format = when {
                    it.format.toString() == "TV_SHORT" -> "SHORT"
                    it.format.toString() == "null" -> "?"
                    else -> it.format.toString()
                },
            )
        } ?: listOf()

        // Filter non-releasing anime
        titles = titles.filter { it.status == "RELEASING" }



        return Pair(titles, response.data?.page?.pageInfo?.hasNextPage == true)
    }
}