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

    fun selectChart(
        songId: String,
        playStyle: PlayStyle,
        difficultyClass: DifficultyClass
    ) = queries.selectChart(songId, playStyle, difficultyClass)
        .executeAsOneOrNull()

    fun selectChartsForSongList(
        songIds: List<String>,
        playStyle: PlayStyle,
    ) = queries.selectChartsForSongList(songIds, playStyle)
        .executeAsList()

    fun selectChartsForSongListByDifficultyClasses(
        songIds: List<String>,
        playStyle: PlayStyle,
        difficultyClasses: List<DifficultyClass>
    ) = queries.selectChartsForSongListAndDifficultyClasses(songIds, playStyle, difficultyClasses)
        .executeAsList()

    fun selectChartsForSong(
        songId: String,
        playStyle: PlayStyle,
    ) = queries.selectChartsForSongAndPlayStyle(songId, playStyle)
        .executeAsList()

    // region Charts

    fun selectCharts(
        playStyle: PlayStyle,
    ) = queries.selectChartsByPlayStyle(
        playStyle,
    ).executeAsList()

    fun selectChartsByDifficultyClasses(
        playStyle: PlayStyle,
        difficultyClasses: List<DifficultyClass>,
    ) = queries.selectChartsByDifficultyClasses(
        playStyle,
        difficultyClasses,
    ).executeAsList()

    fun selectChartsByDifficultyNumbers(
        playStyle: PlayStyle,
        difficultyNumbers: List<Int>,
    ) = queries.selectChartsByDifficultyNumbers(
        playStyle,
        difficultyNumbers.map { it.toLong() },
    ).executeAsList()

    fun selectChartsByDifficultyClassesAndNumbers(
        playStyle: PlayStyle,
        difficultyClasses: List<DifficultyClass>,
        difficultyNumbers: List<Int>,
    ) = queries.selectChartsByDifficultyClassesAndNumbers(
        playStyle,
        difficultyClasses,
        difficultyNumbers.map { it.toLong() },
    ).executeAsList()

    // endregion

    fun selectSongCharts(
        playStyle: PlayStyle,
        songIds: List<String>,
    ): List<SongChartInfo> {
        val songs = selectSongsBySkillID(songIds)
        return selectChartsForSongList(songIds, playStyle)
    }

    fun selectSongChartsByTitle(
        playStyle: PlayStyle,
        titles: List<String>,
    ): Map<SongInfo, List<SongChartInfo>> {
        val songs = selectSongsByTitle(titles)
        return selectChartsForSongList(
            songs.map { it.skillId },
            playStyle,
        )
            .groupBy { chart -> songs.first { it.skillId == chart.skillId } }
    }

    fun selectSongChartsByTitleAndDifficultyClasses(
        playStyle: PlayStyle,
        titles: List<String>,
        difficultyClasses: List<DifficultyClass>,
    ): Map<SongInfo, List<SongChartInfo>> {
        val songs = selectSongsByTitle(titles)
        return selectChartsForSongListByDifficultyClasses(
            songs.map { it.skillId },
            playStyle,
            difficultyClasses,
        )
            .groupBy { chart -> songs.first { it.skillId == chart.skillId } }
    }

    fun allSongCharts() = queries.allSongCharts().executeAsList()

    fun deleteAll() = queries.deleteAll()
}

val ChartInfo.aggregateDiffStyleString get() = difficultyClass.aggregateString(playStyle)