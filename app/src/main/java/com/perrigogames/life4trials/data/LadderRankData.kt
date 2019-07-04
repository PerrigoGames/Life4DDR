package com.perrigogames.life4trials.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class for deserializing the ranks.json file. Describes all of the ranks that can
 * be earned in LIFE4 and the goals required to obtain each.
 */
class LadderRankData(@SerializedName("unlock_requirement") val unlockRequirement: LadderRank,
                     @SerializedName("rank_requirements") val rankRequirements: List<RankEntry>): Serializable

/**
 * Describes a single rank in [LadderRankData] and the goals required to obtain it.
 */
class RankEntry(val rank: LadderRank,
                val goals: List<BaseRankGoal>): Serializable {

    val difficultyGoals: List<DifficultyClearGoal> get() = goals.mapNotNull { it as? DifficultyClearGoal }
}