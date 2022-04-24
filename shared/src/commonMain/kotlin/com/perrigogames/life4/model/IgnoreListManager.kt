package com.perrigogames.life4.model

import co.touchlab.kermit.Logger
import com.perrigogames.life4.LadderRanksReplacedEvent
import com.perrigogames.life4.SettingsKeys.KEY_IMPORT_GAME_VERSION
import com.perrigogames.life4.api.IgnoreListRemoteData
import com.perrigogames.life4.api.base.LocalDataReader
import com.perrigogames.life4.data.IgnoreGroup
import com.perrigogames.life4.data.IgnoreList
import com.perrigogames.life4.data.IgnoredSong
import com.perrigogames.life4.db.DetailedChartInfo
import com.perrigogames.life4.db.aggregateDiffStyleString
import com.perrigogames.life4.injectLogger
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.IGNORES_FILE_NAME
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class IgnoreListManager: BaseModel() {

    private val logger: Logger by injectLogger("IgnoreList")
    private val eventBus: EventBusNotifier by inject()
    private val settings: Settings by inject()
    private val dataReader: LocalDataReader by inject(named(IGNORES_FILE_NAME))
    private val songDataManager: SongDataManager by inject()

    private val ignoreLists = IgnoreListRemoteData(dataReader)
        .apply { start() }

    val dataVersionString get() = ignoreLists.versionString

    //
    // General Ignorelist
    //

    val ignoreListIds get() = ignoreLists.data.lists.map { it.id }
    val ignoreListTitles get() = ignoreLists.data.lists.map { it.name }

    fun getIgnoreList(id: String): IgnoreList =
        ignoreLists.data.lists.firstOrNull { it.id == id } ?: getIgnoreList(SongDataManager.DEFAULT_IGNORE_VERSION)

    //
    // Currently Selected
    //

    val selectedVersion: String
        get() = settings.getString(KEY_IMPORT_GAME_VERSION, SongDataManager.DEFAULT_IGNORE_VERSION)
    val selectedIgnoreList: IgnoreList
        get() = getIgnoreList(selectedVersion)
    val selectedIgnoreGroups: List<IgnoreGroup>
        get() = selectedIgnoreList.groups.map { id ->
            ignoreLists.data.groupsMap[id] ?: error("Invalid group name $id")
        }
    val selectedIgnores: List<IgnoredSong>
        get() = selectedIgnoreList.allIgnores?.toList() ?: emptyList()
    val currentlyIgnoredCharts: Set<DetailedChartInfo>
        get() = selectedIgnores.resolveCharts()

    //
    // Unlocks
    //

    private fun getUnlockGroup(id: String): IgnoreGroup? = ignoreLists.data.groups.firstOrNull { it.id == id }

    private fun getGroupUnlockState(id: String): Long = settings.getLong("unlock_$id", 0L)

    fun getGroupUnlockFlags(id: String): List<Boolean>? = getUnlockGroup(id)?.fromStoredState(getGroupUnlockState(id))

    private fun resolveGroupUnlocks(id: String, unlocked: Boolean): List<IgnoredSong> =
        getGroupUnlockFlags(id)?.let { flags ->
            getUnlockGroup(id)?.songs?.filterIndexed { idx, _ -> flags[idx] == unlocked }
        } ?: throw IllegalArgumentException("No group with id $id")

    fun getCurrentlyUnlockedSongs(): Set<DetailedChartInfo> =
        selectedIgnoreList.groups.flatMap {
            resolveGroupUnlocks(selectedIgnoreList.id, true)
        }
            .resolveCharts()

    fun getCurrentlyLockedSongs(): Set<DetailedChartInfo> {
        val groupSongs = selectedIgnoreList.groups.flatMap { groupId ->
            resolveGroupUnlocks(groupId, false)
        }
        val permalockedSongs = selectedIgnoreList.songs
        val permalockedGroup = selectedIgnoreList.lockedGroups.flatMap { groupId ->
            getUnlockGroup(groupId)!!.songs
        }
        return (groupSongs + permalockedSongs + permalockedGroup)
            .resolveCharts()
    }

    fun setGroupUnlockState(id: String, state: Long) {
        settings["unlock_$id"] = state
        eventBus.post(LadderRanksReplacedEvent())
    }

    private fun List<IgnoredSong>.resolveCharts(): Set<DetailedChartInfo> {
        val tempCharts = songDataManager.detailedCharts.toMutableList()
        return this.flatMap { ignore ->
            if (ignore.difficultyClass == null) {
                tempCharts.filter { chart ->
                    chart.skillId == ignore.skillId
                }
            } else {
                try {
                    listOf(tempCharts.first { chart ->
                        chart.skillId == ignore.skillId && chart.difficultyClass == ignore.difficultyClass
                    })
                } catch (e: NoSuchElementException) {
                    logger.e("Failed ignore ${ignore.skillId}/${ignore.difficultyClass}")
                    tempCharts.groupBy { it.skillId }
                        .forEach { (skillId, charts) ->
                            logger.e("$skillId: ${charts[0].title} (${charts.joinToString("/") { it.aggregateDiffStyleString }})")
                        }
                    throw e
                }
            }.also { tempCharts.removeAll(it) }
        }.toSet()
    }
}
