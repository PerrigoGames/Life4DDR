package com.perrigogames.life4.data

import com.perrigogames.life4.db.DetailedChartResult
import com.perrigogames.life4.enums.TrialRank

/**
 * A sealed class defining the different possible types of extra details that can be
 * shown beneath a ladder goal.
 */
sealed class ProgressDetail {

    /**
     * A [ProgressDetail] that can show a list of songs beneath the ladder goal.
     */
    data class SongBreakdownDetail(
        val results: List<DetailedChartResult>
    ) : ProgressDetail()

    /**
     * A [ProgressDetail] that can show a series of Trials beneath the ladder goal.
     * @param trialMap a mapping of [TrialRank]s to lists of Trials. Intended to
     * express that Trials that the player has achieved a certain rank in would be
     * good candidates to play to achieve the associated goal.
     * A null [TrialRank] indicates Trials that have not been attempted.
     */
    data class TrialSuggestionDetail(
        val trialMap: Map<TrialRank?, List<Trial>>
    ) : ProgressDetail()
}