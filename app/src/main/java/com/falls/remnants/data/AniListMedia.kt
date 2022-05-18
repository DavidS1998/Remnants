package com.falls.remnants.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Anime(
    val id: Int,
    val engTitle: String = "",
    val japTitle: String = "",

    val coverPath: String = "",
    val bannerPath: String = "",
    val color: String = "",

    val format: String = "",
    val episodes: String = "",
    val nextEpisode: String = "",
    val nextEpisodeCountdown: String = "",

    val description: String = "",
    val score: String = "",
    val popularity: String = "",
    val status: String = "",
) : Parcelable