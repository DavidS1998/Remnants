package com.falls.remnants.data

object Utils {

    fun formatDate(timestamp: Int, episode: Int, totalEpisodes: Int, status: String) : String {
        var episodeCounter: String
        var totalEpisodesCounter: String
        if (totalEpisodes < 0) totalEpisodesCounter = "?" else totalEpisodesCounter = totalEpisodes.toString()
        if (episode < 0) episodeCounter = "?" else episodeCounter = episode.toString()
        episodeCounter = "Ep. ${episodeCounter}/${totalEpisodesCounter}"

        if (status == "FINISHED") return "Finished ${totalEpisodesCounter}/${totalEpisodesCounter}"

        // Airing date unknown
        // ?/25 in ?
        // ?/? in ?
        if (timestamp < 0 && status == "RELEASING") return "$episodeCounter in ?"
        // No info known
        if (timestamp < 0 && status == "NOT_YET_RELEASED") return "TBD"

        // If airing date exists, calculate it
        val days = timestamp / (60 * 60 * 24)
        val hours = (timestamp % (60 * 60 * 24)) / (60 * 60)
        val minutes = (timestamp % (60 * 60)) / 60
        val seconds = timestamp % 60

        var remaining: String
        remaining = " in ${minutes} minutes"
        if (hours > 1) remaining = " in ${hours} hours"
        if (days > 1) remaining = " in ${days} days"

        // Both airing date and episode counter are returned
        // Ep. 5/12 in 3 days
        return episodeCounter + remaining
    }
}