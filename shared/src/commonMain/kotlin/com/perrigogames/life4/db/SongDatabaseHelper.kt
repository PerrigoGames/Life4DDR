package com.perrigogames.life4.db

import com.perrigogames.life4.enums.DifficultyClass
import com.perrigogames.life4.enums.GameVersion
import com.perrigogames.life4.enums.PlayStyle
import com.perrigogames.life4.log
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SongDatabaseHelper(sqlDriver: SqlDriver): DatabaseHelper(sqlDriver) {

    private val queries = dbRef.songDataQueries

    suspend fun insertSong(
        id: Long,
        skillId: String,
        title: String,
        artist: String?,
        version: GameVersion,
        preview: Boolean,
    ) = withContext(Dispatchers.Default) {
        queries.insertSong(skillId, id, title, artist, version, preview)
    }

    suspend fun insertChart(
        skillId: String,
        difficultyClass: DifficultyClass,
        difficultyNumber: Long,
        playStyle: PlayStyle,
    ) = withContext(Dispatchers.Default) {
        queries.insertChart(skillId, difficultyClass, difficultyNumber, playStyle)
    }

    suspend fun insertSongsAndCharts(
        songs: List<SongInfo>,
        charts: List<ChartInfo>,
    ) = withContext(Dispatchers.Default) {
        queries.transaction {
            songs.forEach { song ->
                queries.insertSong(song.skillId, song.id, song.title, song.artist, song.version, song.preview)
            }
            charts.forEach { chart ->
                queries.insertChart(chart.skillId, chart.difficultyClass, chart.difficultyNumber, chart.playStyle)
            }
        }
        log("SongImport", "Import committed")
    }

    fun selectSongBySkillID(title: String) = queries.selectSongBySkillId(title)
        .executeAsOneOrNull()
    fun selectSongsBySkillID(titles: List<String>) = queries.selectSongBySkillIdList(titles)
        .executeAsList()

    fun selectSongByTitle(title: String) = queries.selectSongByTitle(title)
        .executeAsOneOrNull()
    fun selectSongsByTitle(titles: List<String>) = queries.selectSongByTitleList(titles)
        .executeAsList()

    fun allSongs() = queries.allSongs().executeAsList()

    fun selectChart(songId: String, playStyle: PlayStyle, difficultyClass: DifficultyClass) =
        queries.selectChart(songId, playStyle, difficultyClass)
            .executeAsOneOrNull()
    fun selectChartsForSong(songId: String) =
        queries.selectChartsForSong(songId)
            .executeAsList()
    fun selectChartsForSongList(songIds: List<String>) =
        queries.selectChartsForSongList(songIds)
            .executeAsList()
    fun selectChartsForSong(songId: String, playStyle: PlayStyle) =
        queries.selectChartsForSongAndPlayStyle(songId, playStyle)
            .executeAsList()
    fun selectChartsForDifficulty(songId: String, difficultyClass: DifficultyClass) =
        queries.selectChartsForSongAndDifficulty(songId, difficultyClass)
    fun selectChartsByDifficultyNumber(difficultyNumber: Int) =
        queries.selectChartsByDifficultyNumber(difficultyNumber.toLong())
            .executeAsList()
    fun selectChartsByDifficultyNumbers(difficultyNumbers: List<Int>) =
        queries.selectChartsByDifficultyNumber(difficultyNumbers.toLong())
            .executeAsList()
    fun selectSongsAndCharts(songIds: List<String>): Map<SongInfo, List<ChartInfo>> {
        val songs = selectSongsBySkillID(songIds)
        return selectChartsForSongList(songIds).groupBy { chart -> songs.first { it.skillId == chart.skillId } }
    }
    fun selectSongsAndChartsByTitle(titles: List<String>): Map<SongInfo, List<ChartInfo>> {
        val songs = selectSongsByTitle(titles)
        return selectChartsForSongList(songs.map { it.skillId }).groupBy { chart -> songs.first { it.skillId == chart.skillId } }
    }

    fun allSongsAndCharts() = queries.allSongsAndCharts().executeAsList()

    fun deleteAll() = queries.deleteAll()
}

val ChartInfo.aggregateDiffStyleString get() = difficultyClass.aggregateString(playStyle)