package com.falls.remnants.networking

import com.apollographql.apollo3.ApolloClient
import com.falls.remnants.data.Configs

open class GraphQLapi {
    // Singleton instance of the ApolloClient for handling GraphQL queries
    companion object {
        @Volatile
        private var INSTANCE: ApolloClient? = null

        fun getInstance(token: String = ""): ApolloClient {

            return INSTANCE ?: synchronized(this) {
                val created: ApolloClient = if (Configs.tempLoggedIn) {
                    ApolloClient.Builder()
                        // TODO: Dynamically get key
                        .addHttpHeader("Authorization", "Bearer ${Configs.tempKey}")
                        .addHttpHeader("Content-Type", "application/json")
                        .addHttpHeader("Accept", "application/json")
                        .serverUrl("https://graphql.anilist.co/")
                        .build()
                } else {
                    ApolloClient.Builder()
                        .addHttpHeader("Content-Type", "application/json")
                        .addHttpHeader("Accept", "application/json")
                        .serverUrl("https://graphql.anilist.co/")
                        .build()
                }
                INSTANCE = created
                created
            }
        }
    }
}