package com.perrigogames.life4.api

import com.perrigogames.life4.api.base.*
import com.perrigogames.life4.data.SongList
import com.perrigogames.life4.ktor.GithubDataAPI
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SongListRemoteData(
    reader: LocalDataReader,
): CompositeData<SongList>(), KoinComponent {

    private val githubKtor: GithubDataAPI by inject()

    private val converter = SongListConverter()

    override val rawData = LocalData(reader, converter)
    override val cacheData = CachedData(reader, converter, converter)
    override val remoteData = object: RemoteData<SongList>() {
        override suspend fun getRemoteResponse() = SongList.parse(githubKtor.getSongList())
    }

    private inner class SongListConverter: Converter<SongList> {
        override fun create(data: SongList) = data.toString()
        override fun create(s: String) = SongList.parse(s)
    }
}
