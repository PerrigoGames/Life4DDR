package com.perrigogames.life4.feature.songresults

import co.touchlab.kermit.Logger
import com.perrigogames.life4.db.ChartResult
import com.perrigogames.life4.db.ResultDatabaseHelper
import com.perrigogames.life4.feature.songlist.Chart
import com.perrigogames.life4.feature.songlist.SongDataManager
import com.perrigogames.life4.feature.songlist.SongLibrary
import com.perrigogames.life4.injectLogger
import com.perrigogames.life4.model.BaseModel
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class SongResultsManager: BaseModel() {

    private val logger: Logger by injectLogger("SongResultsManager")
    private val songDataManager: SongDataManager by inject()
    private val resultDbHelper: ResultDatabaseHelper by inject()

    private val results = MutableStateFlow<List<ChartResult>>(emptyList()).cMutableStateFlow()
    private val _library = MutableStateFlow<List<ChartResultPair>>(emptyList()).cMutableStateFlow()
    val library: CStateFlow<List<ChartResultPair>> = _library.cStateFlow()

    init {
        mainScope.launch {
            combine(
                songDataManager.libraryFlow,
                results
            ) { songData, results ->
                logger.d { "Updating with ${songData.charts.size} charts and ${results.size} results" }
                matchCharts(songData, results)
            }
                .collect(_library)
        }
        refresh()
    }

    fun refresh() {
        logger.d("Refreshing song results")
        mainScope.launch {
            results.emit(resultDbHelper.selectAll())
        }
    }

    internal fun clearAllResults() {
        logger.w("Clearing all saved song results")
        mainScope.launch {
            resultDbHelper.deleteAll()
            results.emit(emptyList())
        }
    }

    private fun matchCharts(
        library: SongLibrary,
        results: List<ChartResult>
    ): List<ChartResultPair> {
        val matches = mutableMapOf<Chart, ChartResult>()
        results.forEach { result ->
            library.charts
                .firstOrNull { chart -> chart.matches(result) }
                ?.let { chart -> matches[chart] = result }
        }
        return library.charts.map { chart ->
            ChartResultPair(
                chart = chart,
                result = matches[chart]
            )
        }
    }
}
