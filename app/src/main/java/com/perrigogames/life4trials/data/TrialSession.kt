package com.perrigogames.life4trials.data

import android.net.Uri
import java.io.Serializable

data class TrialSession(val trial: Trial,
                        var goalRank: TrialRank,
                        val results: Array<SongResult?> = arrayOfNulls(TrialData.TRIAL_LENGTH),
                        var finalPhotoUriString: String? = null): Serializable {

    var finalPhotoUri: Uri
        get() = Uri.parse(finalPhotoUriString)
        set(value) { finalPhotoUriString = value.toString() }

    val availableRanks: Array<TrialRank> = trial.goals[0].rank.andUp

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

    val goalSet: GoalSet?
        get() = trial.goalSet(goalRank)
}

data class SongResult(var song: Song,
                      var photoUriString: String?,
                      var score: Int? = null,
                      var exScore: Int? = null,
                      var misses: Int? = null,
                      var badJudges: Int? = null): Serializable {

    var photoUri: Uri
        get() = Uri.parse(photoUriString)
        set(value) { photoUriString = value.toString() }

    fun randomize() {
        score = (Math.random() * 70000).toInt() + 930000
        exScore = (Math.random() * 1024).toInt()
        misses = (Math.random() * 6).toInt()
        badJudges = misses!! + (Math.random() * 14).toInt()
    }
}