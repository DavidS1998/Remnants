package com.falls.remnants.data

import android.app.Activity

object Utils {

    // Format date and episode data to a single string
    fun formatEpisodes(timestamp: Int, episode: Int, totalEpisodes: Int, status: String) : String {
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
        if (hours >= 1) remaining = " in $hours hours"
        if (days >= 1) remaining = " in $days days"

        // Both airing date and episode counter are returned
        // Ep. 5/12 in 3 days
        return episodeCounter + remaining
    }

    fun formatRemaining(timestamp: Int) : String? {
        if (timestamp < 0) return null

        val days = timestamp / (60 * 60 * 24)
        val hours = (timestamp % (60 * 60 * 24)) / (60 * 60)
        val minutes = (timestamp % (60 * 60)) / 60

        var remaining: String
        remaining = "Next in $minutes min"
        if (hours >= 1) remaining = "Next in $hours hours"
        if (days >= 1) remaining = "Next in $days days"

        return remaining
    }

    fun formatMonth(month: Int) : String {
        return when (month) {
            1 -> "JAN"
            2 -> "FEB"
            3 -> "MAR"
            4 -> "APR"
            5 -> "MAY"
            6 -> "JUN"
            7 -> "JUL"
            8 -> "AUG"
            9 -> "SEP"
            10 -> "OCT"
            11 -> "NOV"
            12 -> "DEC"
            else -> "?"
        }
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

        if (value == "") {
            editor.remove(key)
        } else {
            editor.putString(key, value)
        }
        editor.apply()
    }
}