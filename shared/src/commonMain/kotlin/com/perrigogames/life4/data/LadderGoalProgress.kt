package com.perrigogames.life4.data

import com.perrigogames.life4.db.ChartResult
import com.perrigogames.life4.db.DetailedChartResult
import com.perrigogames.life4.enums.ClearType

/**
 * Data class representing the local user's current progress towards
 * a particular goal
 */
class LadderGoalProgress(
    val progress: Int,
    val max: Int,
    val showMax: Boolean = true,
    val detail: ProgressDetail? = null,
) {
    val isComplete = progress == max && max > 0
}

fun ChartResult.satisfiesClear(type: ClearType) = clearType.ordinal >= type.ordinal
fun DetailedChartResult.satisfiesClear(type: ClearType) = clearType.ordinal >= type.ordinal
