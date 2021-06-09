package com.perrigogames.life4.model

import com.perrigogames.life4.SettingsKeys.KEY_SONG_LIST_VERSION
import com.perrigogames.life4.api.SongListRemoteData
import com.perrigogames.life4.api.base.CompositeData
import com.perrigogames.life4.api.base.LocalDataReader
import com.perrigogames.life4.data.SongList
import com.perrigogames.life4.db.ChartInfo
import com.perrigogames.life4.db.SongDatabaseHelper
import com.perrigogames.life4.db.SongInfo
import com.perrigogames.life4.enums.DifficultyClass
import com.perrigogames.life4.enums.GameVersion
import com.perrigogames.life4.enums.PlayStyle
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.SONGS_FILE_NAME
import com.perrigogames.life4.log
import com.perrigogames.life4.logE
import com.perrigogames.life4.logException
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.launch
import org.koin.core.inject
import org.koin.core.qualifier.named


/**
 * A Manager class that keeps track of the available songs
 */
class SongDataManager: BaseModel() {

    private val dbHelper: SongDatabaseHelper by inject()
    private val majorUpdates: MajorUpdateManager by inject()
    private val ignoreListManager: IgnoreListManager by inject()
    private val settings: Settings by inject()
    private val dataReader: LocalDataReader by inject(named(SONGS_FILE_NAME))

    private val songList = SongListRemoteData(dataReader, object: CompositeData.NewDataListener<SongList> {
        override fun onDataVersionChanged(data: SongList) {
            refreshSongDatabase(data)
        }

        override fun onMajorVersionBlock() = Unit
    })
    val dataVersionString get() = songList.versionString

    init {
        songList.start()
        if (majorUpdates.updates.contains(MajorUpdate.SONG_DB)) {
            initializeSongDatabase()
        }
    }

    // region Song List Management

    fun initializeSongDatabase() {
        dbHelper.deleteAll()
        refreshSongDatabase(force = true)
    }

    private fun refreshSongDatabase(input: SongList = songList.data, force: Boolean = false) {
        mainScope.launch {
            try {
                val lines = input.songLines
                if (force || settings.getInt(KEY_SONG_LIST_VERSION, -1) < lines[0].toInt()) {

                    val songs = mutableListOf<SongInfo>()
                    val charts = mutableListOf<ChartInfo>()

                    lines.forEachIndexed { idx, line ->
                        if (line.isEmpty()) {
                            return@forEachIndexed
                        }
                        val data = line.split('\t')
                        val id = data[0].toLong()
                        val skillId = data[1]

                        var preview = false
                        val mixCode = data[2]
                        val mix = GameVersion.parse(mixCode.let {
                            it.toLongOrNull() ?: it.substring(0, it.length - 1).let { seg ->
                                preview = true
                                seg.toLong()
                            }
                        }) ?: GameVersion.values().last().also {
                            logException(Exception("No game version found for mix code \"$mixCode\""))
                        }

                        val title = data[12]
                        val artist = data[13]

                        songs.add(SongInfo(skillId, id, title, artist, mix, preview))
                        var count = 3
                        PlayStyle.values().forEach { style ->
                            DifficultyClass.values().forEach { diff ->
                                if (style != PlayStyle.DOUBLE || diff != DifficultyClass.BEGINNER) {
                                    val diffNum = data[count++].toLong()
                                    if (diffNum != -1L) {
                                        charts.add(ChartInfo(skillId, diff, style, diffNum))
                                    }
                                }
                            }
                        }

                        log("SongImport", "Importing ${songs.last().title}")
                    }
                    dbHelper.insertSongsAndCharts(songs, charts)

                    ignoreListManager.invalidateIgnoredIds()
                    settings[KEY_SONG_LIST_VERSION] = lines[0].toInt()
                }
            } catch (e: Exception) {
                logE("SongData", e.message ?: "")
            }
        }
    }

    // endregion

    // region Queries

    fun getChartsForDifficultyNumber(difficulty: Int): List<ChartInfo> =
        dbHelper.selectChartsByDifficultyNumber(difficulty)

    fun getChartsForDifficultyNumbers(difficulties: List<Int>): List<ChartInfo> =
        dbHelper.selectChartsByDifficultyNumbers(difficulty)

    // endregion

    companion object {
        const val DEFAULT_IGNORE_VERSION = "A20_PLUS_US"
    }
}

class SongNotFoundException(name: String): Exception("$name does not exist in the song database")

class ChartNotFoundException(songTitle: String, playStyle: PlayStyle, difficultyClass: DifficultyClass, difficultyNumber: Int): Exception(
    "$songTitle (${playStyle.aggregateString(difficultyClass)} $difficultyNumber) does not exist in the song database")
