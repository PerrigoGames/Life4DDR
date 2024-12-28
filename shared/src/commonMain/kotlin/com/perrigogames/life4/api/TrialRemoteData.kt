package com.perrigogames.life4.api

import com.perrigogames.life4.api.base.*
import com.perrigogames.life4.data.TrialData
import com.perrigogames.life4.ktor.GithubDataAPI
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.TRIALS_FILE_NAME
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class TrialRemoteData: CompositeData<TrialData>(), KoinComponent {

    private val json: Json by inject()
    private val githubKtor: GithubDataAPI by inject()
    private val reader: LocalDataReader by inject(named(TRIALS_FILE_NAME))

    private val converter = TrialDataConverter()

    override val rawData = LocalData(reader, converter)
    override val cacheData = CachedData(reader, converter, converter)
    override val remoteData = object: RemoteData<TrialData>() {
        override suspend fun getRemoteResponse() = githubKtor.getTrials()
    }

    private fun validateTrialData(data: TrialData) {
        data.trials
            .firstOrNull { !it.isExValid }
            ?.let { trial ->
                val exScores = trial.songs.map { it.ex }.joinToString()
                throw Exception(
                    "Trial ${trial.name} (${trial.totalEx}) has improper EX scores: $exScores"
                )
            }
    }

    private inner class TrialDataConverter: Converter<TrialData> {
        override fun create(s: String): TrialData {
            val data = json.decodeFromString(TrialData.serializer(), s)
            validateTrialData(data)
            //FIXME debug data
            return data
        }

        override fun create(data: TrialData) = json.encodeToString(TrialData.serializer(), data)
    }
}
