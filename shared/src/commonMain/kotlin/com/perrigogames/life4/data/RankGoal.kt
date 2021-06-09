@file:UseSerializers(
    DifficultyClassSerializer::class,
    PlayStyleSerializer::class,
    ClearTypeSerializer::class,
    LadderRankSerializer::class,
    TrialRankSerializer::class,
    DifficultyClassSetSerializer::class,
)

package com.perrigogames.life4.data

import com.perrigogames.life4.PlatformStrings
import com.perrigogames.life4.db.DetailedChartResult
import com.perrigogames.life4.enums.*
import com.perrigogames.life4.logE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * The base rank goal class, describing a single goal of a rank on the LIFE4 ladder.
 * @param type the type of the goal, indicating which subclass it is
 */
@Serializable
sealed class BaseRankGoal {

    open val id: Int = -1
    @SerialName("s") open val playStyle: PlayStyle = PlayStyle.SINGLE

    abstract fun goalString(c: PlatformStrings): String

    open fun getGoalProgress(possible: Int, results: List<DetailedChartResult>): LadderGoalProgress? = null
}

/**
 * A specialized rank goal requiring the player to burn a number of calories in a single session.
 * @param count the number of calories that need to be burned in a single session
 */
@Serializable
@SerialName("calories")
class CaloriesRankGoal(
    val count: Int,
): BaseRankGoal() {

    override fun goalString(c: PlatformStrings) = c.rank.getCalorieCountString(count)
}

/**
 * A specialized rank goal requiring the player to clear a special set of songResults in one full round of play.
 * @param difficulties the set of difficulty numbers (ordered) that must be cleared
 */
@Serializable
@SerialName("set")
class SongSetGoal(
    @SerialName("diff_nums") val difficulties: IntArray,
    @SerialName("clear_type") private val mClearType: ClearType? = null,
): BaseRankGoal() {

    val clearType: ClearType
        get() = mClearType ?: ClearType.CLEAR

    override fun goalString(c: PlatformStrings) = c.rank.getSongSetString(clearType, difficulties)
}

/**
 * A specialized rank goal requiring the player to clear a Trial with a certain rank.
 * @param rank the [TrialRank] that the user needs to earn
 * @param count the number of trials that need to be cleared with [rank]
 */
@Serializable
@SerialName("trial")
class TrialGoal(
    val rank: TrialRank,
    val count: Int = 1,
    @SerialName("restrict") val restrictDifficulty: Boolean = false,
): BaseRankGoal() {

    override fun goalString(c: PlatformStrings) = c.rank.getTrialCountString(rank, count)
}

/**
 * A specialized goal requiring the user to obtain a certain number of "MFC Points"
 * @param points the number of MFC Points the player is required to obtain
 */
@Serializable
@SerialName("mfc_points")
data class MFCPointsGoal(
    val points: Int,
): BaseRankGoal() {

    override fun goalString(c: PlatformStrings) = c.rank.getMFCPointString(points)
}

/**
 * A specialized rank goal requiring players to clear songResults of a particular difficulty in special ways.
 * For example,
 * - clearing 3 different 12's with LIFE4 enabled
 * - clearing all 17's with 950k or more points
 * - PFC-ing all 15's with the exception of 5 songResults
 */
@Serializable
@SerialName("songs")
class SongsClearGoal(
    @SerialName("d") val diffNum: Int? = null,
    @SerialName("higher_diff") val allowsHigherDiffNum: Boolean = false,
    @SerialName("diff_class") val diffClassSet: DifficultyClassSet? = null,
    val songs: List<String>? = null,
    val folder: String? = null,

    @SerialName("folder_count") val folderCount: Int? = null,
    @SerialName("song_count") val songCount: Int? = null,
    val exceptions: Int? = null,
    @SerialName("song_exceptions") val songExceptions: List<String>? = null,

    val score: Int? = null,
    @SerialName("average_score") val averageScore: Int? = null,
    @SerialName("clear_type") private val mClearType: ClearType? = null,
): BaseRankGoal() {

    val clearType: ClearType
        get() = mClearType ?: ClearType.CLEAR

    fun validate(): Boolean {
        if (
            (diffNum == null && allowsHigherDiffNum) || // diff num required to specify higher diffs allowed
            (folder != null && diffClassSet == null) || // using folder requires diffClass
            (songs != null && diffClassSet == null) // using songs requires diffClass
        ) {
            return false
        }

        // Cannot specify more than 1 of
        var selectionCount = 0
        if (folder != null) selectionCount += 1
        if (songs != null) selectionCount += 1
        if (diffNum != null) selectionCount += 1

        var resultCount = 0
        if (score != null) resultCount += 1
        if (averageScore != null) resultCount += 1
        if (mClearType != null) resultCount += 1
        return resultCount <= 1
    }

    override fun goalString(c: PlatformStrings): String = when {
        score != null -> c.rank.scoreString(score, songGroupString(c))
        averageScore != null -> c.rank.averageScoreString(averageScore, songGroupString(c))
        else -> c.rank.clearString(clearType, shouldUseLamp, songGroupString(c))
    }

    private val shouldUseLamp =
        diffClassSet != null && folderCount != null

    private fun songGroupString(c: PlatformStrings): String = when {
        folderCount != null -> c.rank.folderString(folderCount)
        folder != null -> c.rank.folderString(folder)
        songs != null -> c.rank.songListString(songs)
        diffNum != null -> if (songCount != null) {
            c.rank.diffNumCount(songCount, diffNum, allowsHigherDiffNum)
        } else {
            c.rank.diffNumAll(diffNum, allowsHigherDiffNum)
        }
        songCount != null -> c.rank.songCountString(songCount)
        else -> {
            logE("GoalValidation", "Goal $id has no song group string")
            "???????"
        }
    }
        .difficultySection(c)
        .exceptionSection(c)

    private fun String.difficultySection(c: PlatformStrings) = when {
        diffClassSet != null -> c.rank.difficultyClassSetModifier(this, diffClassSet, playStyle)
        else -> this
    }

    private fun String.exceptionSection(c: PlatformStrings) = when {
        exceptions != null -> c.rank.exceptionsModifier(this, exceptions)
        songExceptions != null -> c.rank.songExceptionsModifier(this, songExceptions)
        else -> this
    }

    override fun getGoalProgress(possible: Int, results: List<DetailedChartResult>): LadderGoalProgress {
        return LadderGoalProgress( // TODO
            progress = 1,
            max = 2,
        )
    }
}

/**
 * A composite goal requiring the user to complete one of a small series of goals
 * @param options the goals from which the player is allowed to choose
 */
@Serializable
@SerialName("multiple")
data class MultipleChoiceGoal(
    val options: List<BaseRankGoal>,
): BaseRankGoal() {

    override fun goalString(c: PlatformStrings) = c.toListString(
        options.map { it.goalString(c).replace(".", "") },
        useAnd = false,
        caps = true
    ) + "."
}
