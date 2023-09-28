package com.perrigogames.life4trials.data

import android.net.Uri
import java.io.Serializable

data class TrialSession(val trial: Trial,
                        var goalRank: TrialRank?,
                        val results: Array<SongResult?> = arrayOfNulls(TrialData.TRIAL_LENGTH),
                        var finalPhotoUriString: String? = null): Serializable {

    var goalObtained: Boolean = false

    var finalPhotoUri: Uri
        get() = Uri.parse(finalPhotoUriString)
        set(value) { finalPhotoUriString = value.toString() }

    val hasFinalPhoto get() = finalPhotoUriString != null

    val availableRanks: Array<TrialRank>? = trial.goals?.map { it.rank }?.toTypedArray()

    val shouldShowAdvancedSongDetails
        get() = trialGoalSet?.let { it.miss != null || it.judge != null } ?: false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrialSession

        if (trial != other.trial) return false
        if (!results.contentEquals(other.results)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = trial.hashCode()
        result = 31 * result + results.contentHashCode()
        return result
    }

    val trialGoalSet: TrialGoalSet?
        get() = trial.goalSet(goalRank)

    val totalExScore: Int
        get() = results.sumBy { it?.exScore ?: 0 }

    val missingExScore: Int
        get() = totalExScore - trial.total_ex!!
}

data class SongResult(var song: Song,
                      var photoUriString: String? = null,
                      var score: Int? = null,
                      var exScore: Int? = null,
                      var misses: Int? = null,
                      var badJudges: Int? = null,
                      var perfects: Int? = null,
                      var passed: Boolean = true): Serializable {

    val hasAdvancedStats: Boolean get() = misses != null || badJudges != null

    var photoUri: Uri
        get() = Uri.parse(photoUriString)
        set(value) { photoUriString = value.toString() }

    fun randomize() {
        score = (Math.random() * 70000).toInt() + 930000
        exScore = (song.ex ?: 1000) - (Math.random() * 100).toInt()
    }
}