package com.perrigogames.life4.api

import com.perrigogames.life4.api.base.CachedData
import com.perrigogames.life4.api.base.CompositeData
import com.perrigogames.life4.api.base.Converter
import com.perrigogames.life4.api.base.LocalData
import com.perrigogames.life4.api.base.LocalDataReader
import com.perrigogames.life4.api.base.RemoteData
import com.perrigogames.life4.data.LadderRankData
import com.perrigogames.life4.ktor.GithubDataAPI
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.RANKS_FILE_NAME
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class LadderRemoteData: CompositeData<LadderRankData>(), KoinComponent {

    private val json: Json by inject()
    private val githubKtor: GithubDataAPI by inject()
    private val reader: LocalDataReader by inject(named(RANKS_FILE_NAME))

    private val converter = LadderRankDataConverter()

    override val rawData = LocalData(reader, converter)
    override val cacheData = CachedData(reader, converter, converter)
    override val remoteData = object: RemoteData<LadderRankData>() {
        override suspend fun getRemoteResponse() = githubKtor.getLadderRanks()
    }

    private inner class LadderRankDataConverter: Converter<LadderRankData> {
        override fun create(s: String) = json.decodeFromString(LadderRankData.serializer(), s)
        override fun create(data: LadderRankData) = json.encodeToString(LadderRankData.serializer(), data)
    }
}
