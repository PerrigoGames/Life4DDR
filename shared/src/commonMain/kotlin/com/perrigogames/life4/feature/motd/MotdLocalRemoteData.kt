package com.perrigogames.life4.feature.motd

import com.perrigogames.life4.api.base.CachedData
import com.perrigogames.life4.api.base.CompositeData
import com.perrigogames.life4.api.base.Converter
import com.perrigogames.life4.api.base.LocalData
import com.perrigogames.life4.api.base.LocalDataReader
import com.perrigogames.life4.api.base.RemoteData
import com.perrigogames.life4.data.MessageOfTheDay
import com.perrigogames.life4.ktor.GithubDataAPI
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class MotdLocalRemoteData: CompositeData<MessageOfTheDay>(), KoinComponent {

    private val json: Json by inject()
    private val githubKtor: GithubDataAPI by inject()
    private val reader: LocalDataReader by inject(named(GithubDataAPI.MOTD_FILE_NAME))

    private val converter = MessageOfTheDayConverter()

    override val rawData = LocalData(reader, converter)
    override val cacheData = CachedData(reader, converter, converter)
    override val remoteData = object: RemoteData<MessageOfTheDay>() {
        override suspend fun getRemoteResponse() = githubKtor.getMotd()
    }

    private inner class MessageOfTheDayConverter: Converter<MessageOfTheDay> {
        override fun create(s: String) = json.decodeFromString(MessageOfTheDay.serializer(), s)
        override fun create(data: MessageOfTheDay) = json.encodeToString(MessageOfTheDay.serializer(), data)
    }
}