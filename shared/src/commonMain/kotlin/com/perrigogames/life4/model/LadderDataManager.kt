package com.perrigogames.life4.model

import com.perrigogames.life4.LadderDialogs
import com.perrigogames.life4.api.LadderRemoteData
import com.perrigogames.life4.api.base.LocalDataReader
import com.perrigogames.life4.api.base.unwrapLoaded
import com.perrigogames.life4.data.LadderRankData
import com.perrigogames.life4.data.LadderVersion
import com.perrigogames.life4.data.RankEntry
import com.perrigogames.life4.enums.LadderRank
import com.perrigogames.life4.feature.songlist.IgnoreListManager
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.RANKS_FILE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import org.koin.core.qualifier.named

/**
 * Manager class that deals with
 */
class LadderDataManager: BaseModel() {

    private val ignoreListManager: IgnoreListManager by inject()
    private val ladderDialogs: LadderDialogs by inject()

    //
    // Ladder Data
    //
    private val data = LadderRemoteData()

    val dataVersionString: Flow<String> =
        data.versionState.map { it.versionString }

    private val _ladderData: Flow<LadderRankData?> =
        data.dataState.unwrapLoaded()

    private val _ladderDataForGameVersion: Flow<LadderVersion?> =
        combine(
            _ladderData.filterNotNull(),
            ignoreListManager.currentGameVersionFlow
        ) { ladderData, currentGameVersion ->
            ladderData.gameVersions[currentGameVersion]
        }

    init {
        mainScope.launch {
            data.start()
        }
    }

    fun requirementsForRank(rank: LadderRank?): Flow<RankEntry?> =
        _ladderDataForGameVersion.map {
            it?.rankRequirements?.firstOrNull { reqs -> reqs.rank == rank }
        }
}
