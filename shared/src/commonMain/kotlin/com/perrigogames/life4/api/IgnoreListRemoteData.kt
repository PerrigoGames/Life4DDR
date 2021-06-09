package com.perrigogames.life4.api

import com.perrigogames.life4.api.base.*
import com.perrigogames.life4.data.IgnoreListData
import com.perrigogames.life4.ktor.GithubDataAPI
import com.perrigogames.life4.model.SongDataManager
import kotlinx.serialization.json.Json
import org.koin.core.KoinComponent
import org.koin.core.inject

class IgnoreListRemoteData(
    reader: LocalDataReader,
    listener: NewDataListener<IgnoreListData>? = null,
): CompositeData<IgnoreListData>(listener), KoinComponent {

    private val json: Json by inject()
    private val githubKtor: GithubDataAPI by inject()
    private val songDataManager: SongDataManager by inject()

    private val converter = IgnoreListConverter()

    override val rawData = LocalData(reader, converter)
    override val cacheData = CachedData(reader, converter, converter)
    override val remoteData = object: RemoteData<IgnoreListData>() {
        override suspend fun getRemoteResponse() = githubKtor.getIgnoreLists()
    }

    private inner class IgnoreListConverter: Converter<IgnoreListData> {
        override fun create(s: String) = json.decodeFromString(IgnoreListData.serializer(), s)
        override fun create(data: IgnoreListData) = json.encodeToString(IgnoreListData.serializer(), data)
    }

    override fun onNewDataAvailable() {
        data.evaluateIgnoreLists()
        data.groups
    }
}
