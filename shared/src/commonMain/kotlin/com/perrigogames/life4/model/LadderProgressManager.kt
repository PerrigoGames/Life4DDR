package com.perrigogames.life4.model

import com.perrigogames.life4.GameConstants
import com.perrigogames.life4.data.*
import com.perrigogames.life4.data.TrialData.Companion.HIGHEST_DIFFICULTY
import com.perrigogames.life4.db.ChartInfo
import com.perrigogames.life4.db.ResultDatabaseHelper
import com.perrigogames.life4.db.SongDatabaseHelper
import org.koin.core.inject

class LadderProgressManager: BaseModel() {

    private val caloriesManager: CaloriesManager by inject()
    private val ignoreListManager: IgnoreListManager by inject()
    private val ladderManager: LadderManager by inject()
    private val songDataManager: SongDataManager by inject()
    private val dbHelper: SongDatabaseHelper by inject()
    private val trialManager: TrialManager by inject()
    private val ladderResults: ResultDatabaseHelper by inject()

    fun getGoalProgress(goal: BaseRankGoal): LadderGoalProgress? = when (goal) {
        is CaloriesRankGoal -> getCaloriesGoalProgress(goal)
        is SongSetGoal -> null
        is SongsClearGoal -> getSongsClearGoalProgress(goal)
        is TrialGoal -> getTrialGoalProgress(goal)
        is MFCPointsGoal -> getMFCGoalProgress(goal)
        is MultipleChoiceGoal -> getMultipleGoalProgress(goal)
    }

    private fun getCaloriesGoalProgress(goal: CaloriesRankGoal): LadderGoalProgress {
        return LadderGoalProgress(
            progress = caloriesManager.highestCaloriesBurned,
            max = goal.count,
        )
    }

    private fun getSongsClearGoalProgress(goal: SongsClearGoal): LadderGoalProgress? {
        val charts = songDataManager.createChartList(goal)
        val progress = ladderResults.selectResultsForCharts(
            charts.map { ResultDatabaseHelper.ResultQueryItem(it.skillId, it.difficultyClass) }
        )

        val totalCharts = charts.count()
        val completedCharts = progress.count {
            true // TODO
        }
        return LadderGoalProgress(
            progress = completedCharts,
            max = totalCharts,
            detail = null,
        )
    }

    private fun getTrialGoalProgress(goal: TrialGoal): LadderGoalProgress {
        val bestSessions = trialManager.bestSessions()
        val qualifyingSessions = bestSessions.filter {
            if (goal.restrictDifficulty) {
                it.goalRank.stableId == goal.rank.stableId
            } else {
                it.goalRank.stableId >= goal.rank.stableId
            }
        }
        return LadderGoalProgress(qualifyingSessions.size, goal.count)
    }

    private fun getMFCGoalProgress(goal: MFCPointsGoal): LadderGoalProgress =
        LadderGoalProgress(
            ladderResults.selectMFCs()
                .sumBy { GameConstants.mfcPointsForDifficulty(it.difficultyNumber.toInt()) },
            goal.points
        )

    private fun getMultipleGoalProgress(goal: MultipleChoiceGoal): LadderGoalProgress? {
        return goal.options
            .mapNotNull { getGoalProgress(it) }
            .maxByOrNull { it.progress / it.max.toFloat() }
    }
}