package com.perrigogames.life4.viewmodel

import com.perrigogames.life4.feature.songlist.IgnoreListManager
import com.perrigogames.life4.feature.trials.TrialManager
import com.perrigogames.life4.model.LadderDataManager
import com.perrigogames.life4.model.MotdManager
import com.perrigogames.life4.feature.songlist.SongDataManager
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VersionsDialogViewModel : ViewModel(), KoinComponent {
    private val ignoreListManager: IgnoreListManager by inject()
    private val ladderDataManager: LadderDataManager by inject()
    private val motdManager: MotdManager by inject()
    private val songDataManager: SongDataManager by inject()
    private val trialManager: TrialManager by inject()

    private val _state = MutableStateFlow(VersionsDialogState()).cMutableStateFlow()
    val state: StateFlow<VersionsDialogState> = _state

    init {
        _state.value = VersionsDialogState(
            ignoreListVersion = ignoreListManager.dataVersionString,
            ladderDataVersion = ladderDataManager.dataVersionString,
            motdVersion = motdManager.dataVersionString,
            songListVersion = songDataManager.dataVersionString,
            trialDataVersion = trialManager.dataVersionString
        )
    }
}

data class VersionsDialogState(
    val appVersion: String = "",
    val ignoreListVersion: String = "",
    val ladderDataVersion: String = "",
    val motdVersion: String = "",
    val songListVersion: String = "",
    val trialDataVersion: String = ""
)
