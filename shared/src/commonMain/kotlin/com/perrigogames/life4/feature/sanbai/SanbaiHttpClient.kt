package com.perrigogames.life4.feature.sanbai

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json

fun sanbaiHttpClient(log: co.touchlab.kermit.Logger) = HttpClient {
    install(ContentNegotiation) {
        json()
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                log.v { message }
            }
        }

        level = LogLevel.INFO
    }
    install(HttpTimeout) {
        val timeout = 30000L
        connectTimeoutMillis = timeout
        requestTimeoutMillis = timeout
        socketTimeoutMillis = timeout
    }
    // FIXME build the service for holding this data
//    install(Auth) {
//        bearer {
//            loadTokens {
//                BearerTokens(
//                    accessToken = localService.getBearerToken(),
//                    refreshToken = localService.getRefreshToken()
//                )
//            }
//            refreshTokens {
//                val tokenResponse = client.post<TokenResponse>("https://3icecream.com/oauth/token") {
//                    parameter("client_id", "<your client ID>")
//                    parameter("client_secret", "<your client secret>")
//                    parameter("grant_type", "refresh_token")
//                    parameter("refresh_token", it.refreshToken)
//                    parameter("redirect_uri", "<your URL>")
//                }
//                BearerTokens(
//                    accessToken = tokenResponse.access_token,
//                    refreshToken = tokenResponse.refresh_token
//                )
//            }
//        }
//    }
}