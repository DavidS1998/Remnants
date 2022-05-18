package com.falls.remnants.data

import androidx.lifecycle.MutableLiveData

object Configs {
    const val perPage: Int = 20 // TODO: Make adjustable
    var columns = MutableLiveData(1)

    val client_id = 8347
    var loggedIn = false
    var tempLoggedIn = true
    var key = ""
}