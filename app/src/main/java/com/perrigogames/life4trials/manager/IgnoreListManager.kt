package com.perrigogames.life4trials.manager

import android.content.Context
import com.perrigogames.life4trials.Life4Application
import com.perrigogames.life4trials.R
import com.perrigogames.life4trials.activity.SettingsActivity.Companion.KEY_IMPORT_GAME_VERSION
import com.perrigogames.life4trials.api.GithubDataAPI
import com.perrigogames.life4trials.api.MajorVersionedRemoteData
import com.perrigogames.life4trials.data.IgnoreGroup
import com.perrigogames.life4trials.data.IgnoreList
import com.perrigogames.life4trials.data.IgnoreListData
import com.perrigogames.life4trials.data.IgnoredSong
import com.perrigogames.life4trials.event.LadderRanksReplacedEvent
import com.perrigogames.life4trials.repo.SongRepo
import com.perrigogames.life4trials.util.DataUtil

class IgnoreListManager(private val context: Context,
                        private val songRepo: SongRepo,
                        private val githubDataAPI: GithubDataAPI,
                        private val settingsManager: SettingsManager): BaseManager() {

    private val ignoreLists = object: MajorVersionedRemoteData<IgnoreListData>(context, R.raw.ignore_lists_v2,
        SongDataManager.IGNORES_FILE_NAME, 1) {
        override suspend fun getRemoteResponse() = githubDataAPI.getIgnoreLists()
        override fun createLocalDataFromText(text: String) = DataUtil.gson.fromJson(text, IgnoreListData::class.java)
        override fun onNewDataLoaded(newData: IgnoreListData) {
            super.onNewDataLoaded(newData)
            newData.evaluateIgnoreLists()
        }
    }

    init {
        ignoreLists.start()
    }

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
        get() = settingsManager.getUserString(KEY_IMPORT_GAME_VERSION, SongDataManager.DEFAULT_IGNORE_VERSION)!!
    val selectedIgnoreList: IgnoreList?
        get() = getIgnoreList(selectedVersion)
    val selectedIgnoreGroups: List<IgnoreGroup>?
        get() = selectedIgnoreList?.groups?.map { id -> ignoreLists.data.groupsMap[id] ?: error("Invalid group name $id") }

    private var mSelectedIgnoreSongIds: LongArray? = null
    private var mSelectedIgnoreChartIds: LongArray? = null

    val selectedIgnoreSongIds: LongArray
        get() {
            if (mSelectedIgnoreSongIds == null) {
                val unlocks = getAllUnlockedSongs()
                mSelectedIgnoreSongIds = selectedIgnoreList?.resolvedSongs?.filterNot { unlocks.contains(it) }?.map { it.title }?.toTypedArray()?.let { ignoreTitles ->
                    val versionId = selectedIgnoreList!!.baseVersion.stableId
                    songRepo.findBlockedSongs(ignoreTitles, versionId, versionId + 1).map { it.id }.toLongArray()
                } ?: LongArray(0)
            }
            return mSelectedIgnoreSongIds!!
        }
    val selectedIgnoreChartIds: LongArray
        get() {
            if (mSelectedIgnoreChartIds == null) {
                mSelectedIgnoreChartIds = selectedIgnoreList?.resolvedCharts?.mapNotNull { chart ->
                    val song = songRepo.findSongByTitle(chart.title)
                    return@mapNotNull song?.charts?.firstOrNull { it.difficultyClass == chart.difficultyClass }?.id
                }?.toLongArray() ?: LongArray(0)
            }
            return mSelectedIgnoreChartIds!!
        }

    //
    // Unlocks
    //

    fun getUnlockGroup(id: String) = ignoreLists.data.groups.firstOrNull { it.id == id }

    fun getGroupUnlockState(id: String): Long {
        return settingsManager.getUserLong("unlock_$id", 0L)
    }

    fun getGroupUnlockFlags(id: String): List<Boolean>? {
        return getUnlockGroup(id)?.fromStoredState(getGroupUnlockState(id))
    }

    fun getGroupUnlockedSongs(id: String): List<IgnoredSong>? {
        val flags = getGroupUnlockFlags(id)
        return getUnlockGroup(id)?.songs?.filterIndexed { idx, _ -> flags?.get(idx) ?: false }
    }

    fun getAllUnlockedSongs(): List<IgnoredSong> =
        ignoreLists.data.groups.mapNotNull { getGroupUnlockedSongs(it.id) }.flatten()

    fun setGroupUnlockState(id: String, state: Long) {
        settingsManager.setUserLong("unlock_$id", state)
        invalidateIgnoredIds()
        Life4Application.eventBus.post(LadderRanksReplacedEvent())
    }

    /**
     * Nulls out the list of invalid IDs, to regenerate them
     */
    fun invalidateIgnoredIds() {
        mSelectedIgnoreSongIds = null
        mSelectedIgnoreChartIds = null
    }
}