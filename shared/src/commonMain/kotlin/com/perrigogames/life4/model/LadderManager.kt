package com.perrigogames.life4.model

import com.perrigogames.life4.*
import com.perrigogames.life4.SettingsKeys.KEY_INFO_RANK
import com.perrigogames.life4.SettingsKeys.KEY_INFO_TARGET_RANK
import com.perrigogames.life4.api.LadderRemoteData
import com.perrigogames.life4.api.base.CompositeData
import com.perrigogames.life4.api.base.LocalDataReader
import com.perrigogames.life4.data.BaseRankGoal
import com.perrigogames.life4.data.LadderRankData
import com.perrigogames.life4.data.LadderVersion
import com.perrigogames.life4.db.GoalDatabaseHelper
import com.perrigogames.life4.db.GoalState
import com.perrigogames.life4.db.ResultDatabaseHelper
import com.perrigogames.life4.enums.GoalStatus
import com.perrigogames.life4.enums.LadderRank
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.RANKS_FILE_NAME
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.inject
import org.koin.core.qualifier.named

class LadderManager: BaseModel() {

    private val ignoreListManager: IgnoreListManager by inject()
    private val songDataManager: SongDataManager by inject()
    private val settings: Settings by inject()
    private val eventBus: EventBusNotifier by inject()
    private val goalDBHelper: GoalDatabaseHelper by inject()
    private val resultDbHelper: ResultDatabaseHelper by inject()
    private val ladderDialogs: LadderDialogs by inject()
    private val dataReader: LocalDataReader by inject(named(RANKS_FILE_NAME))

    //
    // Ladder Data
    //
    private val ladderDataRemote = LadderRemoteData(dataReader, object: CompositeData.NewDataListener<LadderRankData> {
        override fun onDataVersionChanged(data: LadderRankData) {
            ladderDialogs.showLadderUpdateToast()
            eventBus.post(LadderRanksReplacedEvent())
        }

        override fun onMajorVersionBlock() = eventBus.postSticky(DataRequiresAppUpdateEvent())
    }).apply { start() }

    val dataVersionString get() = ladderDataRemote.versionString

    val ladderData: LadderRankData get() = ladderDataRemote.data
    val currentRequirements: LadderVersion
        get() = ignoreListManager.selectedIgnoreList.baseVersion.let { version ->
            ladderData.gameVersions[version] ?: error("Rank requirements not found for version $version")
        }

    //
    // Local User Rank
    //
    fun getUserRank(): LadderRank? =
        LadderRank.parse(settings.getStringOrNull(KEY_INFO_RANK)?.toLongOrNull())

    fun getUserGoalRank(): LadderRank? =
        settings.getStringOrNull(KEY_INFO_TARGET_RANK)?.toLongOrNull()?.let { LadderRank.parse(it) }
            ?: getUserRank()?.let { return LadderRank.values().getOrNull(it.ordinal + 1) }
            ?: LadderRank.COPPER1

    fun setUserRank(rank: LadderRank?) {
        settings[KEY_INFO_RANK] = rank?.stableId.toString()
        settings[KEY_INFO_TARGET_RANK] = ""
        eventBus.post(LadderRankUpdatedEvent())
    }

    fun setUserTargetRank(rank: LadderRank?) {
        settings[KEY_INFO_TARGET_RANK] = rank?.stableId.toString()
        eventBus.post(LadderRankUpdatedEvent())
    }

    //
    // Rank Navigation
    //
    fun findRankEntry(rank: LadderRank?) = currentRequirements.rankRequirements.firstOrNull { it.rank == rank }

    fun previousEntry(rank: LadderRank?) = previousEntry(currentRequirements.rankRequirements.indexOfFirst { it.rank == rank })

    fun previousEntry(index: Int) = currentRequirements.rankRequirements.getOrNull(index - 1)

    fun nextEntry(rank: LadderRank?) = nextEntry(currentRequirements.rankRequirements.indexOfFirst { it.rank == rank })

    fun nextEntry(index: Int) = currentRequirements.rankRequirements.getOrNull(index + 1)

    //
    // Goal State
    //
    fun getGoalState(goal: BaseRankGoal): GoalState? =
        goalDBHelper.stateForId(goal.id.toLong())

    fun getOrCreateGoalState(goal: BaseRankGoal): GoalState =
        getGoalState(goal) ?: GoalState(goal.id.toLong(), GoalStatus.INCOMPLETE, Clock.System.now().toString())

    fun getGoalStateList(goals: List<BaseRankGoal>): List<GoalState> =
        goalDBHelper.statesForIdList(goals.map { it.id.toLong() }).executeAsList()

    fun setGoalState(id: Long, status: GoalStatus) {
        goalDBHelper.insertGoalState(id, status)
    }

    fun clearGoalStates() {
        ladderDialogs.onClearGoalStates {
            mainScope.launch {
                goalDBHelper.deleteAll()
            }
            resultDbHelper.deleteAll()
            eventBus.post(LadderRankUpdatedEvent())
        }
    }

    fun clearSongResults() {
        ladderDialogs.onClearSongResults {
            resultDbHelper.deleteAll()
            eventBus.post(SongResultsUpdatedEvent())
        }
    }

    fun refreshSongDatabase() {
        ladderDialogs.onRefreshSongDatabase {
            resultDbHelper.deleteAll()
            songDataManager.initializeSongDatabase()
            eventBus.post(SongResultsUpdatedEvent())
        }
    }
}
