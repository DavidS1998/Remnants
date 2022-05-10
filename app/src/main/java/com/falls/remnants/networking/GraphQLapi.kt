package com.falls.remnants.networking

import com.apollographql.apollo3.ApolloClient

open class GraphQLapi {
    // Singleton instance of the ApolloClient for handling GraphQL queries
    companion object {
        @Volatile
        private var INSTANCE: ApolloClient? = null

        fun getInstance(): ApolloClient {
            // TODO: Rate limiter

            return INSTANCE ?: synchronized(this) {
                val created = ApolloClient.Builder()
                    .serverUrl("https://graphql.anilist.co/")
                    .build()
                INSTANCE = created
                created
            }
        }
    }
}