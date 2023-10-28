package com.perrigogames.life4.model

import com.perrigogames.life4.LadderDialogs
import com.perrigogames.life4.api.LadderRemoteData
import com.perrigogames.life4.api.base.CompositeData
import com.perrigogames.life4.api.base.LocalDataReader
import com.perrigogames.life4.data.BaseRankGoal
import com.perrigogames.life4.data.LadderRankData
import com.perrigogames.life4.data.LadderVersion
import com.perrigogames.life4.db.GoalDatabaseHelper
import com.perrigogames.life4.db.GoalState
import com.perrigogames.life4.enums.GoalStatus
import com.perrigogames.life4.enums.LadderRank
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.RANKS_FILE_NAME
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.inject
import org.koin.core.qualifier.named

/**
 * Manager class that deals with
 */
class LadderDataManager: BaseModel() {

    private val ignoreListManager: IgnoreListManager by inject()
    private val goalDBHelper: GoalDatabaseHelper by inject()
    private val ladderDialogs: LadderDialogs by inject()
    private val dataReader: LocalDataReader by inject(named(RANKS_FILE_NAME))

    //
    // Ladder Data
    //
    private val ladderDataRemote = LadderRemoteData(dataReader, object : CompositeData.NewDataListener<LadderRankData> {
        override fun onDataVersionChanged(data: LadderRankData) {
            ladderDialogs.showLadderUpdateToast()
            // FIXME eventBus.post(LadderRanksReplacedEvent())
        }

        override fun onMajorVersionBlock() {
            // FIXME eventBus.postSticky(DataRequiresAppUpdateEvent())
        }
    }).apply { start() }

    val dataVersionString get() = ladderDataRemote.versionString

    val ladderData: LadderRankData get() = ladderDataRemote.data
    val currentRequirements: LadderVersion
        get() = ignoreListManager.selectedIgnoreList.baseVersion.let { version ->
            ladderData.gameVersions[version] ?: error("Rank requirements not found for version $version")
        }
    private val rankRequirements get() = currentRequirements.rankRequirements

    //
    // Rank Navigation
    //
    fun findRankEntry(rank: LadderRank?) = rankRequirements.firstOrNull { it.rank == rank }

    fun previousEntry(rank: LadderRank?) = previousEntry(rankRequirements.indexOfFirst { it.rank == rank })

    fun previousEntry(index: Int) = rankRequirements.getOrNull(index - 1)

    fun nextEntry(rank: LadderRank?) = nextEntry(rankRequirements.indexOfFirst { it.rank == rank })

    fun nextEntry(index: Int) = rankRequirements.getOrNull(index + 1)

    //
    // Goal State
    //
    fun getGoalState(id: Long): GoalState? = goalDBHelper.stateForId(id)
    fun getGoalState(goal: BaseRankGoal): GoalState? = getGoalState(goal.id.toLong())

    fun getOrCreateGoalState(id: Long): GoalState = getGoalState(id)
        ?: GoalState(id, GoalStatus.INCOMPLETE, Clock.System.now().toString())
    fun getOrCreateGoalState(goal: BaseRankGoal): GoalState = getOrCreateGoalState(goal.id.toLong())

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
            // FIXME ladderProgressManager.clearAllResults()
            // FIXME eventBus.post(LadderRankUpdatedEvent())
        }
    }
}
