package com.perrigogames.life4.model

import com.perrigogames.life4.*
import com.perrigogames.life4.db.ResultDatabaseHelper
import com.perrigogames.life4.db.SongDatabaseHelper
import com.perrigogames.life4.enums.ClearType
import com.perrigogames.life4.enums.DifficultyClass
import com.perrigogames.life4.enums.DifficultyClass.BEGINNER
import com.perrigogames.life4.enums.PlayStyle
import com.perrigogames.life4.enums.PlayStyle.DOUBLE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.inject

/**
 * Model class to facilitate importing ladder data from an external source. Data is provided as a List<String> with each
 * line corresponding to either a single chart or a full song, depending on operation mode.
 *
 * This importer can function in two modes: SA and Legacy mode.
 *
 * In SA mode, input lines are interpreted as Score Attack import lines, where each line holds scores for an entire song.
 * In Legacy mode, input lines are interpreted using the old copy-paste style data lines, where each line was only a single
 *  chart, which led to a lot of duplication.
 */
class LadderImporter(
    private var dataLines: List<String>,
    private val opMode: OpMode = OpMode.AUTO,
): BaseModel() {

    private val ignoreListManager: IgnoreListManager by inject()
    private val songDbHelper: SongDatabaseHelper by inject()
    private val resultDbHelper: ResultDatabaseHelper by inject()
    private val ladderDialogs: LadderDialogs by inject()
    private val eventBus: EventBusNotifier by inject()

    private var success = 0
    private var errors = 0
    private var importJob: Job? = null
    private var listener: Listener? = null

    fun start(listener: Listener? = null) {
        success = 0
        errors = 0
        dataLines = dataLines.filterNot { it.isEmpty() }
        this.listener = listener
        importJob = MainScope(Dispatchers.Unconfined).launch {
            dataLines.forEach { when (opMode) {
                OpMode.AUTO -> autoParseManagerLine(it)
                OpMode.LEGACY -> parseLegacyManagerLine(it)
                OpMode.SA -> parseManagerLine(it)
            } }
            withContext(Dispatchers.Main) {
                ladderDialogs.showImportFinishedToast()
                ignoreListManager.invalidateIgnoredIds()
                eventBus.post(SongResultsImportCompletedEvent())
                if (success > 0) {
                    eventBus.post(SongResultsUpdatedEvent())
                }
                importJob = null
                listener?.onCompleted()
            }
        }
    }

    private suspend fun autoParseManagerLine(entry: String) {
        val chunk = entry.substring(0, 6)
        when {
            chunk.count { it == ';' } >= 1 -> parseLegacyManagerLine(entry)
            chunk.count { it == '\t' } >= 1 -> parseManagerLine(entry)
            else -> signalError("Unable to determine input mode")
        }
    }

    private suspend fun parseManagerLine(entry: String) {
        val entryParts = entry.trim().split('\t').toMutableList()
        if (entryParts.size == 39) {
            val id = entryParts.removeAt(0)
            val charts = PlayStyle.values().flatMap { playStyle ->
                DifficultyClass.values().mapNotNull { difficulty ->
                    if (playStyle == DOUBLE && difficulty == BEGINNER) { null }
                    else {
                        val grade = entryParts.removeAt(0)
                        val score = entryParts.removeAt(0).toLong()
                        val fullCombo = entryParts.removeAt(0)
                        val combo = entryParts.removeAt(0).toInt()
                        if (grade == "NoPlay") {
                            null
                        } else {
                            SASongEntry(null, score, ClearType.parseSA(grade, fullCombo), playStyle, difficulty)
                        }
                    }
                }
            }

            val skillId = entryParts.removeAt(0)
            charts.forEach { it.skillId = skillId }

            val songName = entryParts.removeAt(0).replace('+', ' ')
            val songDb = songDbHelper.selectSongBySkillID(skillId)
            if (isDebug) {
                log("SAImport", "$songName ($skillId) - ${charts.size} found, dbExist=${songDb != null}")
            }
            if (songDb != null) {
                resultDbHelper.insertSAResults(charts)
                success++
                signalUpdate()
            } else {
                signalError("Song \"$songName\" ($skillId) not found in the database")
            }
        } else if (entry.isNotEmpty()) {
            signalError("Entry is too short or too long")
        }
    }

    private suspend fun parseLegacyManagerLine(entry: String) {
        val entryParts = entry.trim().split(';')
        if (entryParts.size >= 4) {
            // format = %p:b:B:D:E:C%%y:SP:DP%;%d%;%s0%;%l%;%f:mfc:pfc:gfc:fc:life4:clear%;%e%;%a%;%t%
            try {
                val chartType = entryParts[0] // ESP
                val difficultyNumber = entryParts[1].toInt()
                val score = entryParts[2].toInt()
                // need 5 and 6 first
                val clears = entryParts[5].toIntOrNull() ?: 0

                var clear = ClearType.parse(entryParts[4])!!
                if (clear == ClearType.CLEAR) {
                    when {
                        entryParts[3] == "-" -> clear = ClearType.NO_PLAY
                        entryParts[3] == "E" -> clear = when {
                            clears > 0 -> ClearType.CLEAR
                            else -> ClearType.FAIL
                        }
                    }
                }

                val songName = entryParts.subList(entryParts.size - 1, entryParts.size).joinToString(";")

                val playStyle = PlayStyle.parse(chartType)!!
                val difficultyClass = DifficultyClass.parse(chartType)!!

                val songDB = songDbHelper.selectSongByTitle(
                    songName
                ) ?: throw SongNotFoundException(songName)
                val chartDB = songDbHelper.selectChart(
                    songDB.skillId,
                    playStyle,
                    difficultyClass
                ) ?: throw ChartNotFoundException(songDB.title, playStyle, difficultyClass, difficultyNumber)
                resultDbHelper.insertResult(songDB, chartDB, clear, score)

                if (isDebug && clear == ClearType.NO_PLAY) {
                    log("import", "${songDB.title} - ${chartDB.difficultyClass} (${chartDB.difficultyNumber})")
                }
                success++
                if (success % 2 == 0) {
                    signalUpdate()
                }
            } catch (e: Exception) {
                logE("Exception", e.message ?: "")
                signalError("${entry}\n${e.message}")
            }
        } else if (entry.isNotEmpty()) {
            signalError("Entry is too short")
        }
    }

    private suspend fun signalUpdate(current: Int = success + errors,
                                     total: Int = dataLines.size - 1) {
        withContext(Dispatchers.Main) { listener?.onCountUpdated(current, total) }
    }

    private suspend fun signalError(message: String) {
        errors++
        withContext(Dispatchers.Main) { listener?.onError(errors, message) }
    }

    class SASongEntry(
        var skillId: String?,
        val score: Long,
        val clearType: ClearType,
        val playStyle: PlayStyle,
        val difficultyClass: DifficultyClass,
    )

    fun cancel() {
        importJob?.cancel()
        importJob = null
    }

    /**
     * Listener class for the manager import process
     */
    interface Listener {
        fun onCountUpdated(current: Int, total: Int)
        fun onError(totalCount: Int, message: String)
        fun onCompleted()
    }

    enum class OpMode { LEGACY, SA, AUTO }
}