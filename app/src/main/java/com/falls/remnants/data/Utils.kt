package com.falls.remnants.data

import android.app.Activity

object Utils {

    // Format date and episode data to a single string
    fun formatDate(timestamp: Int, episode: Int, totalEpisodes: Int, status: String) : String {
        val totalEpisodesCounter: String = if (totalEpisodes < 0) "?" else totalEpisodes.toString()
        var episodeCounter: String = if (episode < 0) "?" else episode.toString()
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
        remaining = " in $minutes min"
        if (hours > 1) remaining = " in $hours hours"
        if (days > 1) remaining = " in $days days"

        // Both airing date and episode counter are returned
        // Ep. 5/12 in 3 days
        return episodeCounter + remaining
    }

    // Get and save data from SharedSettings
    fun getSharedSettings(activity: Activity, key: String): String {
        val sharedPref = activity.getSharedPreferences("settings", 0)
        val value = sharedPref.getString(key, "")
        return value?: ""
    }

    // Save data to SharedSettings
    fun saveSharedSettings(activity: Activity, key: String, value: String) {
        val sharedPref = activity.getSharedPreferences("settings", 0)
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.apply()
    }
}