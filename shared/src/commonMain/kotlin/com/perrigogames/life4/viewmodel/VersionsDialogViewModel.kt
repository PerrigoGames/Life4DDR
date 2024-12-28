package com.perrigogames.life4.viewmodel

import com.perrigogames.life4.feature.motd.MotdManager
import com.perrigogames.life4.feature.songlist.IgnoreListManager
import com.perrigogames.life4.feature.songlist.SongDataManager
import com.perrigogames.life4.feature.trials.TrialManager
import com.perrigogames.life4.model.LadderDataManager
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            combine(
                ignoreListManager.dataVersionString,
                ladderDataManager.dataVersionString,
                motdManager.dataVersionString,
                songDataManager.dataVersionString,
                trialManager.dataVersionString,
            ) { ignoreVersion, ladderVersion, motdVersion, songDataVersion, trialVersion ->
                _state.value = VersionsDialogState(
                    ignoreListVersion = ignoreVersion,
                    ladderDataVersion = ladderVersion,
                    motdVersion = motdVersion,
                    songListVersion = songDataVersion,
                    trialDataVersion = trialVersion
                )
            }
        }
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
