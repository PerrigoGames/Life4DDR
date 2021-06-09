package com.perrigogames.life4.db

import com.perrigogames.life4.enums.ClearType
import com.perrigogames.life4.enums.DifficultyClass
import com.perrigogames.life4.enums.PlayStyle
import com.perrigogames.life4.model.LadderImporter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResultDatabaseHelper(sqlDriver: SqlDriver): DatabaseHelper(sqlDriver) {

    private val queries = dbRef.songResultQueries

    suspend fun insertResult(
        skillId: String,
        difficultyClass: DifficultyClass,
        playStyle: PlayStyle,
        clearType: ClearType,
        score: Int,
        exScore: Int? = null
    ) = withContext(Dispatchers.Default) {
        queries.insertResult(skillId, difficultyClass, playStyle, clearType, score.toLong(), exScore?.toLong())
    }

    suspend fun insertResult(
        song: SongInfo,
        chart: ChartInfo,
        clearType: ClearType,
        score: Int,
        exScore: Int? = null
    ) = insertResult(song.skillId, chart.difficultyClass, chart.playStyle, clearType, score, exScore)

    fun insertSAResults(entries: List<LadderImporter.SASongEntry>) {
        queries.transaction {
            entries.forEach {
                queries.insertResult(it.skillId!!, it.difficultyClass, it.playStyle, it.clearType, it.score, null)
            }
        }
    }

    fun selectResultsForCharts(items: List<ResultQueryItem>): List<DetailedChartResult> {
        val results = queries.selectAll()
            .executeAsList()
            .groupBy { it.skillId }
        return items.mapNotNull { queryItem ->
            results[queryItem.skillId]?.firstOrNull { chartResult ->
                queryItem.difficultyClass == chartResult.difficultyClass
            }
        }
    }

    fun selectMFCs() = queries.selectMFCs().executeAsList()

    fun deleteAll() = queries.deleteAll()

    class ResultQueryItem(
        val skillId: String,
        val difficultyClass: DifficultyClass,
    )
}