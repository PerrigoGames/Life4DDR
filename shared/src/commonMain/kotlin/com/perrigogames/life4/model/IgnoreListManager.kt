package com.perrigogames.life4.model

import com.perrigogames.life4.LadderRanksReplacedEvent
import com.perrigogames.life4.SettingsKeys.KEY_IMPORT_GAME_VERSION
import com.perrigogames.life4.api.IgnoreListRemoteData
import com.perrigogames.life4.api.base.LocalDataReader
import com.perrigogames.life4.data.IgnoreGroup
import com.perrigogames.life4.data.IgnoreList
import com.perrigogames.life4.data.IgnoredSong
import com.perrigogames.life4.db.SongChartInfo
import com.perrigogames.life4.db.SongDatabaseHelper
import com.perrigogames.life4.db.SongInfo
import com.perrigogames.life4.enums.PlayStyle
import com.perrigogames.life4.ktor.GithubDataAPI.Companion.IGNORES_FILE_NAME
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import org.koin.core.inject
import org.koin.core.qualifier.named

class IgnoreListManager: BaseModel() {

    private val eventBus: EventBusNotifier by inject()
    private val settings: Settings by inject()
    private val dataReader: LocalDataReader by inject(named(IGNORES_FILE_NAME))
    private val dbHelper: SongDatabaseHelper by inject()

    private val ignoreLists = IgnoreListRemoteData(dataReader)
        .apply { start() }

    val dataVersionString get() = ignoreLists.versionString

    //
    // General Ignorelist
    //

    val ignoreListIds get() = ignoreLists.data.lists.map { it.id }
    val ignoreListTitles get() = ignoreLists.data.lists.map { it.name }

    fun getIgnoreList(id: String): IgnoreList =
        ignoreLists.data.lists.firstOrNull { it.id == id }
            ?: getIgnoreList(SongDataManager.DEFAULT_IGNORE_VERSION)

    //
    // Currently Selected
    //

    val selectedVersion: String
        get() = settings.getString(KEY_IMPORT_GAME_VERSION, SongDataManager.DEFAULT_IGNORE_VERSION)
    val selectedIgnoreList: IgnoreList
        get() = getIgnoreList(selectedVersion)
    val selectedIgnoreGroups: List<IgnoreGroup>?
        get() = selectedIgnoreList.groups?.map { id ->
            ignoreLists.data.groupsMap[id] ?: error("Invalid group name $id")
        }

    private var mSelectedIgnoreSongIds: List<String>? = null
    val selectedIgnoreSongIds: List<String>
        get() {
            if (mSelectedIgnoreSongIds == null) {
                val unlocks = getAllUnlockedSongs()
                mSelectedIgnoreSongIds = selectedIgnoreList.resolvedSongs
                    ?.filterNot { unlocks.contains(it) }
                    ?.map { it.skillId } ?: emptyList()
            }
            return mSelectedIgnoreSongIds!!
        }
    val selectedIgnoreCharts get() = selectedIgnoreList.resolvedCharts?.toList() ?: emptyList()

    //
    // Unlocks
    //

    fun getUnlockGroup(id: String) = ignoreLists.data.groups.firstOrNull { it.id == id }

    fun getGroupUnlockState(id: String) = settings.getLong("unlock_$id", 0L)

    fun getGroupUnlockFlags(id: String): List<Boolean>? = getUnlockGroup(id)?.fromStoredState(getGroupUnlockState(id))

    fun getGroupUnlockedSongs(id: String): List<IgnoredSong>? {
        val flags = getGroupUnlockFlags(id)
        return getUnlockGroup(id)?.songs?.filterIndexed { idx, _ -> flags?.get(idx) ?: false }
    }

    fun getAllUnlockedSongs(): List<IgnoredSong> = ignoreLists.data.groups.mapNotNull { getGroupUnlockedSongs(it.id) }.flatten()

    fun setGroupUnlockState(id: String, state: Long) {
        settings["unlock_$id"] = state
        invalidateIgnoredIds()
        eventBus.post(LadderRanksReplacedEvent())
    }

    /**
     * Nulls out the list of invalid IDs, to regenerate them
     */
    fun invalidateIgnoredIds() {
        mSelectedIgnoreSongIds = null
    }

    fun getCurrentlyIgnoredSongs(): List<SongInfo> = dbHelper.selectSongsBySkillID(selectedIgnoreSongIds)

    fun getCurrentlyIgnoredCharts(playStyle: PlayStyle): List<SongChartInfo> {
        val ignoreIds = selectedIgnoreCharts.map { it.skillId }
        val ignoreCharts = dbHelper.selectSongCharts(playStyle, ignoreIds)
        return selectedIgnoreCharts.map { ignored ->
            ignoreCharts.first {
                ignored.skillId == it.skillId &&
                        ignored.playStyle == it.playStyle &&
                        ignored.difficultyClass == it.difficultyClass
            }
        }
    }
}
