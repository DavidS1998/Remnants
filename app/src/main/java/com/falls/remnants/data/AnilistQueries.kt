package com.falls.remnants.data

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.falls.remnants.*
import com.falls.remnants.networking.GraphQLapi
import com.falls.remnants.type.*
import timber.log.Timber

object AnilistQueries {

    // Standardized formatting for queries
    private fun formatAnime(response: ApolloResponse<BrowseQuery.Data>): List<Anime> {

        Timber.d("QUERY SENT, PAGE: ${response.data?.page?.pageInfo?.currentPage}")
        Timber.d(response.data?.toString())

        return response.data?.page?.media?.mapNotNull {
            Anime(
                id = it?.id!!,
                engTitle = it.title?.english ?: it.title?.romaji ?: "",
                japTitle = if (it.title?.romaji.equals(
                        it.title?.english ?: it.title?.romaji
                    )
                ) "" // If same, empty
                else it.title?.romaji ?: "",

                score = it.averageScore?.toString() ?: "N/A",
                popularity = it.popularity?.toString() ?: "N/A",
                status = it.status?.toString() ?: "?",

                episodes = "/" + (it.episodes ?: "?"),
                nextEpisode = Utils.formatEpisodes(
                    it.nextAiringEpisode?.timeUntilAiring ?: -1,
                    it.nextAiringEpisode?.episode ?: -1,
                    it.episodes ?: -1,
                    it.status?.name ?: ""
                ),
                nextEpisodeCountdown = it.nextAiringEpisode?.timeUntilAiring?.toString() ?: "-1",
                color = it.coverImage?.color ?: "#FFFFFF",

                isOnList = it.mediaListEntry != null,
                userStatus = it.mediaListEntry?.status?.toString() ?: "N/A",
                userProgress = it.mediaListEntry?.progress?.toString() ?: "N/A",

                coverPath = it.coverImage?.extraLarge ?: it.coverImage?.large
                ?: it.coverImage?.medium ?: "", // XL > L > M > None
                format = when {
                    it.format.toString() == "TV_SHORT" -> "SHORT"
                    it.format.toString() == "null" -> "?"
                    else -> it.format.toString()
                },
            )
        } ?: listOf()
    }

    suspend fun seasonal(season: MediaSeason, year: Int, page: Int): Pair<List<Anime>, Boolean> {
        // Execute and post query
        val response: ApolloResponse<BrowseQuery.Data> = GraphQLapi.getInstance()
            .query(
                BrowseQuery(
                    pageNumber = page,
                    season = Optional.presentIfNotNull(season),
                    year = Optional.presentIfNotNull(year),
                    sort = listOf(MediaSort.POPULARITY_DESC)
                )
            )
            .execute()

        return Pair(formatAnime(response), response.data?.page?.pageInfo?.hasNextPage == true)
    }

    suspend fun upcoming(page: Int): Pair<List<Anime>, Boolean> {
        // Execute and post query
        val response: ApolloResponse<BrowseQuery.Data> = GraphQLapi.getInstance()
            .query(
                BrowseQuery(
                    pageNumber = page,
                    sort = listOf(MediaSort.POPULARITY_DESC),
                    season = Optional.Present(null),
                    year = Optional.Present(null),
                    status = Optional.presentIfNotNull(MediaStatus.NOT_YET_RELEASED)
                )
            )
            .execute()

        return Pair(formatAnime(response), response.data?.page?.pageInfo?.hasNextPage == true)
    }

    suspend fun search(search: String): List<Anime> {
        // Execute and post query
        val response: ApolloResponse<BrowseQuery.Data> = GraphQLapi.getInstance()
            .query(
                BrowseQuery(
                    pageNumber = 1,
                    sort = listOf(MediaSort.POPULARITY_DESC),
                    search = Optional.presentIfNotNull(search)
                )
            )
            .execute()

        return formatAnime(response)
    }

    //////////////////////////////////////////////////////////////

    // Details
    suspend fun details(id: Int): Anime {
        // Execute and post query
        val response: ApolloResponse<DetailsQuery.Data> = GraphQLapi.getInstance()
            .query(DetailsQuery(id))
            .execute()

        Timber.d("DETAILS QUERY SENT")

        // Formatting
        val data = response.data?.anime

        return Anime(
            id = data?.id!!,
            idMAL = data.idMal ?: 0,
            engTitle = data.title?.english ?: data.title?.romaji ?: "",
            japTitle = if (data.title?.romaji.equals(
                    data.title?.english ?: data.title?.romaji
                )
            ) "" // If same, empty
            else data.title?.romaji ?: "",

            format = when {
                data.format.toString() == "TV_SHORT" -> "SHORT"
                data.format.toString() == "null" -> "?"
                else -> data.format.toString()
            },
            status = when {
                data.status?.toString() == "NOT_YET_RELEASED" -> "UNRELEASED"
                else -> data.status?.toString() ?: "?"
            },

            season = data.season?.toString() ?: "?",
            year = data.seasonYear?.toString() ?: "?",
            episodes = data.episodes?.toString() ?: "?",

            userProgress = if (data.mediaListEntry?.progress == null) "-" else data.mediaListEntry.progress.toString() + "/" + (data.episodes?.toString() ?: "?"),
            userScore = if (data.mediaListEntry?.score == null) "-" else if (data.mediaListEntry.score == 0.0) "-" else data.mediaListEntry.score.toString() + "%",
            userStatus = (data.mediaListEntry?.status)?.toString() ?: "-",

            nextEpisode = Utils.formatRemaining(
                data.nextAiringEpisode?.timeUntilAiring ?: -1,
            ) ?: "",
            nextEpisodeCountdown = data.nextAiringEpisode?.timeUntilAiring?.toString() ?: "-1",


            start = ((data.startDate?.month?.let { Utils.formatMonth(it) })?: "?") +
                " " + (data.startDate?.day?.toString() ?: "?") +
                ", " + (data.startDate?.year?.toString() ?: "?"),
            end = ((data.endDate?.month?.let { Utils.formatMonth(it) })?: "?") +
                " " + (data.endDate?.day?.toString() ?: "?") +
                ", " + (data.endDate?.year?.toString() ?: "?"),

            description = data.description ?: "",
            score = if ((data.averageScore).toString() == "null") "N/A" else data.averageScore.toString() + "%",

            color = data.coverImage?.color ?: "#FFFFFF",
            coverPath = data.coverImage?.extraLarge ?: data.coverImage?.large ?: data.coverImage?.medium ?: "",
            bannerPath = data.bannerImage ?: data.coverImage?.extraLarge ?: data.coverImage?.large ?: data.coverImage?.medium ?: "",

            genres = data.genres?.joinToString(separator = "\n") ?: "",

            // Only get animation studio names if there are any
            studios = data.studios?.nodes?.filter { it?.isAnimationStudio == true }?.joinToString(separator = "\n") { it?.name ?: "" } ?: "",

            relations = data.relations?.edges?.mapNotNull {
                it?.node?.let { it1 ->
                    Anime(
                        id = it1.id,
                        engTitle = it1.title?.english ?: it1.title?.romaji ?: "",
                        japTitle = if (it1.title?.romaji.equals(it1.title?.english ?: it1.title?.romaji)) "" // If same, empty
                        else it1.title?.romaji ?: "",

                        coverPath = it1.coverImage?.extraLarge ?: it1.coverImage?.large ?: it1.coverImage?.medium ?: "",

                        format = when {
                            it1.format.toString() == "TV_SHORT" -> "SHORT"
                            it1.format.toString() == "null" -> "?"
                            else -> it1.format.toString()
                        },
                        status = when {
                            it1.status?.toString() == "NOT_YET_RELEASED" -> "UNRELEASED"
                            else -> it1.status?.toString() ?: "?"
                        },

                        isOnList = it1.mediaListEntry != null,
                        userStatus = (it1.mediaListEntry?.status)?.toString() ?: "-",
                        userProgress = (it1.mediaListEntry?.progress)?.toString() ?: "N/A",

                        // Convert underscore to space in relationType
                        relationType = it.relationType.toString().replace("_", " "),
                    )

                }
            } ?: listOf(),
        )
    }


    // User list query with unique formatting
    suspend fun library(
        page: Int,
        viewList: MediaListStatus = MediaListStatus.CURRENT,
        order: MediaListSort = MediaListSort.MEDIA_POPULARITY_DESC
    ): Pair<List<Anime>, Boolean> {

        // Return if not logged in
        if (Configs.loggedIn.value == false) {
            return Pair(listOf(), false)
        }

        var actualList: MediaListStatus? = viewList
        if (viewList == MediaListStatus.UNKNOWN__) {
            actualList = null
        }

        // Execute and post query
        val response: ApolloResponse<UserAiringQuery.Data> =
            GraphQLapi.getLoggedInInstance(Configs.token)
                .query(
                    UserAiringQuery(
                        pageNumber = page,
                        list = Optional.presentIfNotNull(actualList),
                        user = Configs.username.value.toString(),
                        sort = listOf(order)
                    )
                )
                .execute()

        Timber.d("CURRENT QUERY SENT")

        // Formatting
        val titles = response.data?.page?.mediaList?.mapNotNull { it2 ->
            it2?.media?.let { it ->
                Anime(
                    id = it.id,
                    engTitle = it.title?.english ?: it.title?.romaji ?: "",
                    japTitle = if (it.title?.romaji.equals(
                            it.title?.english ?: it.title?.romaji
                        )
                    ) "" // If same, empty
                    else it.title?.romaji ?: "",

                    episodes = "/" + (it.episodes ?: "?"),
                    status = it.status?.toString() ?: "?",
                    score = it2.score?.toInt()?.toString() ?: "?",

                    userProgress = it2.progress?.toString() ?: "?",
                    userStatus = it2.status?.toString() ?: "?",
                    userScore = it2.score?.toInt()?.toString() ?: "?",
                    updatedTime = Utils.formatDate(it2.media.mediaListEntry?.updatedAt ?: 0),

                    nextEpisodeCountdown = it.nextAiringEpisode?.timeUntilAiring?.toString()
                        ?: "-1",
                    nextEpisode = Utils.formatEpisodes(
                        it.nextAiringEpisode?.timeUntilAiring ?: -1,
                        it.nextAiringEpisode?.episode ?: -1,
                        it.episodes ?: -1,
                        it.status?.name ?: ""
                    ),
                    latestEpisode = if (it.nextAiringEpisode?.episode.toString() != "null") it.nextAiringEpisode?.episode.toString() else "-1",

                    coverPath = it.coverImage?.extraLarge ?: it.coverImage?.large
                    ?: it.coverImage?.medium ?: "", // XL > L > M > None
                    format = when {
                        it.format.toString() == "TV_SHORT" -> "SHORT"
                        it.format.toString() == "null" -> "?"
                        else -> it.format.toString()
                    },
                )
            }
        } ?: listOf()

        return Pair(titles, response.data?.page?.pageInfo?.hasNextPage == true)
    }
}