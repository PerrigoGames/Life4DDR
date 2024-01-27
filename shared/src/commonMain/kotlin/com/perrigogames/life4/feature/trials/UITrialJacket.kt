package com.perrigogames.life4.feature.trials

import com.perrigogames.life4.data.TrialState
import com.perrigogames.life4.db.SelectBestSessions
import com.perrigogames.life4.enums.TrialJacketCorner
import com.perrigogames.life4.enums.TrialRank
import com.perrigogames.life4.enums.TrialType

data class UITrialJacket(
    val trial: com.perrigogames.life4.data.Trial,
    val session: SelectBestSessions? = null,
    val overrideCorner: TrialJacketCorner? = null,
    val rank: TrialRank? = null,
    val exScore: Int? = null,
    val tintOnRank: TrialRank? = null,
    val showExRemaining: Boolean = false,
) {

    val viewAlpha: Float = if (trial.isRetired) 0.5f else 1f

    val cornerType: TrialJacketCorner = overrideCorner ?: when {
        trial.state == TrialState.NEW && session == null -> TrialJacketCorner.NEW
        trial.type == TrialType.EVENT -> TrialJacketCorner.EVENT
        else -> TrialJacketCorner.NONE
    }

    val shouldTint = rank != null && rank == tintOnRank
}