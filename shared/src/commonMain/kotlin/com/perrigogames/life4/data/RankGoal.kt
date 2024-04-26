@file:UseSerializers(
    DifficultyClassSerializer::class,
    PlayStyleSerializer::class,
    ClearTypeSerializer::class,
    LadderRankSerializer::class,
    TrialRankSerializer::class,
    DifficultyClassSetSerializer::class,
    RankGoalUserTypeSerializer::class,
)

package com.perrigogames.life4.data

import co.touchlab.kermit.Logger
import com.perrigogames.life4.*
import com.perrigogames.life4.GameConstants.HIGHEST_DIFFICULTY
import com.perrigogames.life4.enums.*
import dev.icerock.moko.resources.desc.Composition
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.serialization.*
import org.koin.core.component.KoinComponent

/**
 * The base rank goal class, describing a single goal of a rank on the LIFE4 ladder.
 * @param type the type of the goal, indicating which subclass it is
 */
@Serializable
sealed class BaseRankGoal {

    @Transient var isMandatory: Boolean = false

    abstract val id: Int
    @SerialName("s") open val playStyle: PlayStyle = PlayStyle.SINGLE

    abstract fun goalString(): StringDesc
}

/**
 * An extension of the [BaseRankGoal] that groups similar statuses with one or more "stacked"
 * values, which can stand in for multiple ranks that inherit the base values.
 */
@Serializable
sealed class StackedRankGoal : BaseRankGoal() {

    val stacks: Map<String, List<Double?>> = emptyMap()

    @Transient val expandedGoalCount: Int = stacks.values.maxOf { it.size }

    open val expandedGoals: List<BaseRankGoal> by lazy {
        List(expandedGoalCount) { idx ->
            StackedRankGoalWrapper(this, idx)
        }
    }

    fun getIntValue(index: Int, key: String) = stacks[key]?.getOrNull(index)?.toInt()
    fun getDoubleValue(index: Int, key: String) = stacks[key]?.getOrNull(index)

    override fun goalString() = error("Cannot call standard goalString on stacked goal")
    abstract fun goalString(index: Int): StringDesc
}

/**
 * A wrapper around a [StackedRankGoal] to allow a single part of that stack to be utilized in
 * main app functions.
 */
class StackedRankGoalWrapper(
    val mainGoal: StackedRankGoal,
    val index: Int,
) : BaseRankGoal() {

    override val id = mainGoal.id + index

    fun getIntValue(key: String) = mainGoal.getIntValue(index, key)
    fun getDoubleValue(key: String) = mainGoal.getDoubleValue(index, key)

    override fun goalString() = mainGoal.goalString(index)
}

/**
 * A specialized rank goal requiring the player to burn a number of calories in a single session.
 * @param count the number of calories that need to be burned in a single session
 */
@Serializable
@SerialName("calories")
class CaloriesStackedRankGoal(
    override val id: Int,
): StackedRankGoal() {

    override fun goalString(index: Int) =
        RankStrings.getCalorieCountString(getIntValue(index, KEY_CALORIES)!!)

    companion object {
        const val KEY_CALORIES = "calories"
    }
}

/**
 * A specialized rank goal requiring the player to clear a special set of songResults in one full round of play.
 * @param difficulties the set of difficulty numbers (ordered) that must be cleared
 */
@Serializable
@SerialName("set")
class DifficultySetGoal(
    override val id: Int,
    @SerialName("diff_nums") val difficulties: IntArray,
    @SerialName("clear_type") private val mClearType: ClearType? = null,
): BaseRankGoal() {

    val clearType: ClearType
        get() = mClearType ?: ClearType.CLEAR

    override fun goalString() = RankStrings.getSongSetString(clearType, difficulties)
}

/**
 * A specialized rank goal requiring the player to clear a Trial with a certain rank.
 * @param rank the [TrialRank] that the user needs to earn
 * @param count the number of trials that need to be cleared with [rank]
 */
@Serializable
@SerialName("trial")
class TrialStackedGoal(
    override val id: Int,
    val rank: TrialRank,
    @SerialName("restrict") val restrictDifficulty: Boolean = false,
): StackedRankGoal() {

    override fun goalString(index: Int) =
        RankStrings.getTrialCountString(rank, getIntValue(index, KEY_TRIALS_COUNT)!!)

    companion object {
        const val KEY_TRIALS_COUNT = "count"
    }
}

/**
 * A specialized goal requiring the user to obtain a certain number of "MFC Points"
 * @param points the number of MFC Points the player is required to obtain
 */
@Serializable
@SerialName("mfc_points")
class MFCPointsStackedGoal(
    override val id: Int,
): StackedRankGoal() {

    override fun goalString(index: Int) =
        RankStrings.getMFCPointString(getDoubleValue(index, KEY_MFC_POINTS)!!)

    companion object {
        const val KEY_MFC_POINTS = "mfc_points"
    }
}

/**
 * A specialized rank goal requiring players to clear songResults of a particular difficulty in special ways.
 * For example,
 * - clearing 3 different 12's with LIFE4 enabled
 * - clearing all 17's with 950k or more points
 * - PFC-ing all 15's except for 5 songResults
 */
@Serializable
@SerialName("songs")
class SongsClearGoal(
    override val id: Int,
    @SerialName("user_type") val userType: RankGoalUserType? = null,
    @SerialName("d") val diffNum: Int? = null,
    @SerialName("allow_higher") val allowsHigherDiffNum: Boolean = false,
    @SerialName("diff_class") val diffClassSet: DifficultyClassSet? = null,
    val songs: List<String>? = null,
    private val folder: String? = null,

    @SerialName("folder_count") val folderCount: Int? = null,
    @SerialName("song_count") val songCount: Int? = null,
    val exceptions: Int? = null,
    @SerialName("song_exceptions") val songExceptions: List<String>? = null,

    val score: Int? = null,
    @SerialName("average_score") val averageScore: Int? = null,
    @SerialName("clear_type") private val mClearType: ClearType? = null,
): BaseRankGoal(), KoinComponent {

    val safeExceptions: Int = exceptions ?: 0
    private val logger: Logger by injectLogger("RankGoal")

    @Transient
    val folderType: FolderType? = folder.let {
        val version = GameVersion.parse(folder)
        return@let when {
            it == null -> null
            version != null -> FolderType.Version(version)
            it.length == 1 -> FolderType.Letter(it[0])
            else -> error("Illegal folder type")
        }
    }

    val clearType: ClearType
        get() = mClearType ?: ClearType.CLEAR

    fun validate(): Boolean {
        if (averageScore != null && allowsHigherDiffNum) {
            return false // averages only supported for a single difficulty
        }

        var count = 0
        if (score != null) count += 1
        if (averageScore != null) count += 1
        if (mClearType != null) count += 1
        return count <= 1
    }

    val diffNumRange: IntRange? by lazy {
        if (diffNum == null) null
        else {
            val maxLevel = if (allowsHigherDiffNum) HIGHEST_DIFFICULTY else diffNum
            (diffNum..maxLevel)
        }
    }

    inline fun forEachDiffNum(block: (Int) -> Unit) {
        diffNumRange?.forEachIndexed { _, diff -> block(diff) }
    }

    override fun goalString(): StringDesc = when {
        score != null -> RankStrings.scoreString(score, songGroupString())
        averageScore != null -> RankStrings.averageScoreString(averageScore, songGroupString())
        else -> RankStrings.clearString(clearType, shouldUseLamp, songGroupString())
    }

    private val shouldUseLamp =
        diffClassSet != null && folderCount != null

    private fun songGroupString(): StringDesc = when {
        folderCount != null -> RankStrings.folderString(folderCount)
        folder != null -> RankStrings.folderString(folder)
        songs != null -> RankStrings.songListString(songs)
        diffNum != null -> if (songCount != null) {
            RankStrings.diffNumCount(songCount, diffNum, allowsHigherDiffNum)
        } else {
            RankStrings.diffNumAll(diffNum, allowsHigherDiffNum)
        }
        songCount != null -> RankStrings.songCountString(songCount)
        else -> {
            logger.e { "Goal $id has no song group string" }
            StringDesc.Raw("???????")
        }
    }
        .difficultySection()
        .exceptionSection()

    private fun StringDesc.difficultySection() = when {
        diffClassSet != null -> RankStrings.difficultyClassSetModifier(this, diffClassSet, playStyle)
        else -> this
    }

    private fun StringDesc.exceptionSection() = when {
        exceptions != null -> RankStrings.exceptionsModifier(this, exceptions)
        songExceptions != null -> RankStrings.songExceptionsModifier(this, songExceptions.toStringDescs())
        else -> this
    }

    sealed class FolderType {
        class Letter(val letter: Char): FolderType()
        class Version(val version: GameVersion): FolderType()
    }
}

/**
 * A specialized rank goal requiring players to clear songResults of a particular difficulty in special ways.
 * For example,
 * - clearing 3 different 12's with LIFE4 enabled
 * - clearing all 17's with 950k or more points
 * - PFC-ing all 15's except for 5 songResults
 *
 * This is the stacked variant of [SongsClearGoal] and supports stacks for the keys "diff_num", "folder_count",
 * "song_count", "exceptions", "score", and "average_score".
 */
@Serializable
@SerialName("songs_stack")
data class SongsClearStackedGoal(
    override val id: Int,
    @SerialName("user_type") val userType: RankGoalUserType? = null,
    @SerialName("d") val diffNum: Int? = null,
    @SerialName("allow_higher") val allowsHigherDiffNum: Boolean = false,
    @SerialName("diff_class") val diffClassSet: DifficultyClassSet? = null,
    val songs: List<String>? = null,
    private val folder: String? = null,

    @SerialName("folder_count") val folderCount: Int? = null,
    @SerialName("song_count") val songCount: Int? = null,
    val exceptions: Int? = null,
    @SerialName("song_exceptions") val songExceptions: List<String>? = null,

    val score: Int? = null,
    @SerialName("average_score") val averageScore: Int? = null,
    @SerialName("clear_type") private val mClearType: ClearType? = null,
) : StackedRankGoal() {

    override val expandedGoals: List<BaseRankGoal>
        get() = List(expandedGoalCount) { idx ->
            SongsClearGoal(
                id = id + idx,
                userType = userType,
                diffNum = getIntValue(idx, KEY_DIFFICULTY_NUM) ?: diffNum,
                allowsHigherDiffNum,
                diffClassSet,
                songs,
                folder,
                folderCount = getIntValue(idx, KEY_FOLDER_COUNT) ?: folderCount,
                songCount = getIntValue(idx, KEY_SONG_COUNT) ?: songCount,
                exceptions = getIntValue(idx, KEY_EXCEPTIONS) ?: exceptions,
                songExceptions,
                score = getIntValue(idx, KEY_SCORE) ?: score,
                averageScore = getIntValue(idx, KEY_AVERAGE_SCORE) ?: averageScore,
                mClearType,
            )
        }

    override fun goalString(index: Int) = expandedGoals[index].goalString()

    companion object {
        const val KEY_DIFFICULTY_NUM = "diff_num"
        const val KEY_FOLDER_COUNT = "folder_count"
        const val KEY_SONG_COUNT = "song_count"
        const val KEY_EXCEPTIONS = "exceptions"
        const val KEY_SCORE = "score"
        const val KEY_AVERAGE_SCORE = "average_score"
    }
}

/**
 * A composite goal requiring the user to complete one of a small series of goals
 * @param options the goals from which the player is allowed to choose
 */
@Serializable
@SerialName("multiple")
data class MultipleChoiceGoal(
    override val id: Int,
    val options: List<BaseRankGoal>,
): BaseRankGoal() {

    override fun goalString() = StringDesc.Composition(
        options.map { it.goalString() } // FIXME period substitution
            .toListString(
                useAnd = false,
                caps = true
            ) + StringDesc.Raw(".")
    )
}
