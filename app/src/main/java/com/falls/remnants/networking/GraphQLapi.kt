package com.falls.remnants.networking

import com.apollographql.apollo3.ApolloClient
import com.falls.remnants.data.Configs
import timber.log.Timber

open class GraphQLapi {
    // Singleton instance of the ApolloClient for handling GraphQL queries
    companion object {
        @Volatile
        private var INSTANCE: ApolloClient? = null

        fun getInstance(): ApolloClient {

            return INSTANCE ?: synchronized(this) {
                val created: ApolloClient =
                    ApolloClient.Builder()
                        .addHttpHeader("Content-Type", "application/json")
                        .addHttpHeader("Accept", "application/json")
                        .serverUrl("https://graphql.anilist.co/")
                        .build()

                INSTANCE = created
                created
            }
        }

        @Volatile
        private var INSTANCE2: ApolloClient? = null
        // The other instance may already have been initialized. Temporary solution?
        fun getLoggedInInstance(token: String): ApolloClient {
            Timber.d("Token: $token")

            return INSTANCE2 ?: synchronized(this) {
                val created = ApolloClient.Builder()
                    .addHttpHeader("Authorization", "Bearer $token")
                    .addHttpHeader("Content-Type", "application/json")
                    .addHttpHeader("Accept", "application/json")
                    .serverUrl("https://graphql.anilist.co/")
                    .build()

                INSTANCE2 = created
                created
            }
        }

        fun resetInstances() {
            INSTANCE = null
            INSTANCE2 = null
        }
    }
}