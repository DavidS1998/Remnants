package com.falls.remnants.networking

import com.apollographql.apollo3.ApolloClient

open class GraphQLapi {
    // Singleton instance of the ApolloClient for handling GraphQL queries
    companion object {
        @Volatile
        private var INSTANCE: ApolloClient? = null
        // TODO: Fetch secret

        fun getInstance(token: String = ""): ApolloClient {

            return INSTANCE ?: synchronized(this) {
                val created = ApolloClient.Builder()
//                    .addHttpHeader("Authorization", "Bearer ${Auth.getInstance().getToken()}")
//                    .addHttpHeader("Authorization", "Bearer $SECRET")
                    .addHttpHeader("Content-Type", "application/json")
                    .addHttpHeader("Accept", "application/json")
                    .serverUrl("https://graphql.anilist.co/")
                    .build()
                INSTANCE = created
                created
            }
        }
    }
}