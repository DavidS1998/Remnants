package com.falls.remnants.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Anime(
    val id: Int,
    val idMAL: Int = 0,
    val engTitle: String = "",
    val japTitle: String = "",

    val coverPath: String = "",
    val bannerPath: String = "",
    val color: String = "",

    val format: String = "",
    val episodes: String = "",
    val latestEpisode: String = "",
    val nextEpisode: String = "",
    val nextEpisodeCountdown: String = "",
    val season: String = "",
    val year: String = "",

    val start: String = "",
    val end: String = "",

    val description: String = "",
    val score: String = "",
    val popularity: String = "",
    val status: String = "",

    val isOnList: Boolean = false,
    val userProgress: String = "",
    val userStatus: String = "",
    val userScore: String = "",

    val genres: String = "",
    val studios: String = "",

    val relations: List<Anime> = listOf(),
    val relationType: String = "",
) : Parcelable