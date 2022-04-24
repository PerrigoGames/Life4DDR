package com.perrigogames.life4.ktor

import co.touchlab.stately.ensureNeverFrozen
import com.perrigogames.life4.api.baseHttpClient
import com.perrigogames.life4.data.IgnoreListData
import com.perrigogames.life4.data.LadderRankData
import com.perrigogames.life4.data.MessageOfTheDay
import com.perrigogames.life4.data.TrialData
import com.perrigogames.life4.isDebug
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.koin.core.component.KoinComponent

class GithubDataImpl(private val log: co.touchlab.kermit.Logger): GithubDataAPI, KoinComponent {

    private val client = baseHttpClient(log)

    init {
        ensureNeverFrozen()
    }

    override suspend fun getLadderRanks(): LadderRankData =
        client.get { github(GithubDataAPI.RANKS_FILE_NAME) }.body()

    override suspend fun getSongList(): String =
        client.get { github(GithubDataAPI.SONGS_FILE_NAME) }.body()

    override suspend fun getIgnoreLists(): IgnoreListData =
        client.get { github(GithubDataAPI.IGNORES_FILE_NAME) }.body()

    override suspend fun getTrials(): TrialData =
        client.get { github(GithubDataAPI.TRIALS_FILE_NAME) }.body()

    override suspend fun getMotd(): MessageOfTheDay =
        client.get { github(GithubDataAPI.MOTD_FILE_NAME) }.body()

    private fun HttpRequestBuilder.github(filename: String) {
        val githubTarget = if (isDebug) "remote-data-test" else "remote-data"
        url {
            takeFrom("https://raw.githubusercontent.com/")
            encodedPath = "PerrigoGames/Life4DDR-Trials/$githubTarget/app/src/main/res/raw/$filename"
        }
    }
}
