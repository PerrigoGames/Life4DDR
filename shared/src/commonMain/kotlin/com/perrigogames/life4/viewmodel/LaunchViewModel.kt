package com.perrigogames.life4.viewmodel

import com.perrigogames.life4.feature.firstrun.FirstRunSettingsManager
import com.perrigogames.life4.feature.firstrun.InitState
import dev.icerock.moko.mvvm.flow.CFlow
import dev.icerock.moko.mvvm.flow.cFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LaunchViewModel : ViewModel(), KoinComponent {

    private val firstRunSettingsManager: FirstRunSettingsManager by inject()

    private val _launchState = MutableSharedFlow<InitState?>()
    val launchState: CFlow<InitState?> = _launchState.cFlow()

    init {
        viewModelScope.launch {
            firstRunSettingsManager.initState.collect(_launchState)
        }
    }
}

data class UILaunchScreen(
    val requireSignin: Boolean,
    val initState: InitState?
)