package com.falls.remnants.data

import androidx.lifecycle.MutableLiveData

object Configs {
    var columns = MutableLiveData(1)

    const val client_id = 8347

    var loggedIn = MutableLiveData(false)
    var username = MutableLiveData("")

    var token = ""

    var mostPopular = 0.0f
}