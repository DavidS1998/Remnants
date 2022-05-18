package com.falls.remnants

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.falls.remnants.data.Configs
import com.falls.remnants.data.Utils
import com.falls.remnants.databinding.ActivityMainBinding
import com.falls.remnants.networking.GraphQLapi
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("MainActivity start!")

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        navView.setupWithNavController(navController)

        // Apply saved settings
        val value = Utils.getSharedSettings(this, "columns")
        Configs.columns.value = value.toIntOrNull() ?: 1

        // Login attempt
        val intent = intent
        if (intent != null) {
            val action = intent.action
            val data = intent.data
            // Login attempt, redirected from AniList
            if (Intent.ACTION_VIEW == action && data != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    login(data)
                }
            // Launched app normally
            } else if (Intent.ACTION_MAIN == action) {
                // Get login data from storage
                val token = Utils.getSharedSettings(this, "access_token")
                val userName = Utils.getSharedSettings(this, "username")
                if (token.isNotEmpty()) {
                    Configs.loggedIn.value = true
                    Configs.token = token
                    Configs.username.value= userName
                    Toast.makeText(this, "Logged in as $userName", Toast.LENGTH_SHORT).show()
                } else {
                    Configs.loggedIn.value = false
                    Configs.token = ""
                    Configs.username.value = ""
                    Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun login(data: Uri) {

        // Extract token from URI
        val uri = data.toString().split("#")[1]
        val arguments = uri.split("&")
        val argument = arguments[0]
        val token = argument.split("=").toTypedArray()[1]

        if (token.isNotBlank()) {
            try {
                // Extract username
                val response: ApolloResponse<UserInfoQuery.Data> = GraphQLapi.getLoggedInInstance(token)
                    .query(UserInfoQuery())
                    .execute()

                val name = response.data?.user?.name ?: ""

                if (name.isBlank()) {
                    // Something happened, do not save anything
                    Timber.d("Login failed")
                    return
                }

                // Set as logged in
                Configs.loggedIn.value = true
                Configs.token = token

                Utils.saveSharedSettings(this, "access_token", token)
                Utils.saveSharedSettings(this, "username", name)
                Configs.username.value = name

                // Notify user
                Toast.makeText(this, "Login successful: ${Configs.username.value}", Toast.LENGTH_LONG).show()
            } catch (e: ApolloException) {
                Timber.e(e)
                Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}