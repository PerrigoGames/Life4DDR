package com.perrigogames.life4.feature.songlist

import co.touchlab.kermit.Logger
import com.perrigogames.life4.api.IgnoreListRemoteData
import com.perrigogames.life4.api.base.LocalDataReader
import com.perrigogames.life4.api.base.unwrapLoaded
import com.perrigogames.life4.data.IgnoreList
import com.perrigogames.life4.data.IgnoreListData
import com.perrigogames.life4.enums.GameVersion
import com.perrigogames.life4.feature.settings.LadderListSelectionSettings
import com.perrigogames.life4.injectLogger
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.IGNORES_FILE_NAME
import com.perrigogames.life4.model.BaseModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class IgnoreListManager: BaseModel() {

    private val json: Json by inject()
    private val logger: Logger by injectLogger("IgnoreList")
    private val settings: Settings by inject()
    private val songDataManager: SongDataManager by inject()
    private val ladderListSelectionSettings: LadderListSelectionSettings by inject()

    private val data = IgnoreListRemoteData()

    val ignoreListsFlow: Flow<IgnoreListData> = data.dataState
        .unwrapLoaded()
        .filterNotNull()
        .map { it.evaluateIgnoreLists() }

    val currentIgnoreListFlow: Flow<IgnoreList> = combine(
        ignoreListsFlow,
        ladderListSelectionSettings.selectedIgnoreList
    ) { ignoreLists, selectedListId ->
        val out = ignoreLists.lists.find { it.id == selectedListId }
        if (out == null) {
            logger.e { "Ignore list with ID $selectedListId does not exist, defaulting to all music" }
        }
        out ?: ignoreLists.lists.last()
    }

    val currentGameVersionFlow: Flow<GameVersion> = currentIgnoreListFlow
        .map { it.baseVersion }

    val dataVersionString: Flow<String> =
        data.versionState.map { it.versionString }

    init {
        mainScope.launch {
            data.start()
        }
    }
}
