package com.perrigogames.life4.feature.sanbai

//class SanbaiAPIImpl(private val log: co.touchlab.kermit.Logger) : SanbaiAPI, KoinComponent {
//
//    private val client: HttpClient = sanbaiHttpClient(log)
//
//    override suspend fun getScores(): List<SanbaiScoreResult> {
//        val response: HttpResponse = client.get("https://3icecream.com/dev/api/v1/get_scores") {
//            parameter("access_token", "FIXME")
//        }
//
//        if (!response.status.isSuccess()) {
//            throw RuntimeException("Failed to fetch scores: ${response.status}")
//        }
//
//        return response.body<List<SanbaiScoreResult>>()
//    }
//
//    override suspend fun getPlayerId(): String {
//        val response: HttpResponse = client.get("https://3icecream.com/dev/api/v1/get_player_id") {
//            parameter("access_token", "FIXME")
//        }
//
//        if (!response.status.isSuccess()) {
//            throw RuntimeException("Failed to fetch player id: ${response.status}")
//        }
//
//        return response.body<String>()
//    }
//
//    override suspend fun getPartialDifficulties(): PartialDifficultyResponse {
//        val response: HttpResponse = client.get("https://3icecream.com/dev/api/v1/get_partial_difficulties") {
//            parameter("access_token", "FIXME")
//        }
//
//        if (!response.status.isSuccess()) {
//            throw RuntimeException("Failed to fetch partial difficulties: ${response.status}")
//        }
//
//        return response.body<PartialDifficultyResponse>()
//    }
//}