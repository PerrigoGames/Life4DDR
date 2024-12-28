package com.perrigogames.life4.feature.settings

import com.perrigogames.life4.AppInfo
import com.perrigogames.life4.feature.songresults.SongResultsManager
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsViewModel(
    private val onClose: () -> Unit,
    private val onNavigateToCredits: () -> Unit,
) : ViewModel(), KoinComponent {
    private val appInfo: AppInfo by inject()
    private val resultsManager: SongResultsManager by inject()

    private val pageStackState = MutableStateFlow(listOf(SettingsPage.ROOT)).cMutableStateFlow()
    private val pageFlow = pageStackState.map { it.last() }

    val state: CStateFlow<UISettingsData?> = pageFlow.map { createPage(it) }
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = null).cStateFlow()
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    fun handleAction(action: SettingsAction) {
        when (action) {
            SettingsAction.None -> {}
            is SettingsAction.Modal -> TODO()
            is SettingsAction.Navigate -> pushPage(action.page)
            is SettingsAction.NavigateBack -> {
                if (pageStackState.value.size > 1) {
                    popPage()
                } else {
                    onClose()
                }
            }
            is SettingsAction.SetBoolean -> TODO()
            is SettingsAction.Email -> {
                viewModelScope.launch {
                    _events.emit(SettingsEvent.Email(action.email))
                }
            }
            is SettingsAction.WebLink -> {
                viewModelScope.launch {
                    _events.emit(SettingsEvent.WebLink(action.url))
                }
            }
            is SettingsAction.ShowCredits -> onNavigateToCredits()
            is SettingsAction.Debug.SongData -> resultsManager.createDebugScores()
        }
    }

    private fun pushPage(page: SettingsPage) {
        pageStackState.value += page
    }

    private fun popPage() {
        pageStackState.value = pageStackState.value.dropLast(1)
    }

    private fun createPage(page: SettingsPage): UISettingsData {
        return when (page) {
            SettingsPage.ROOT -> UISettingsMocks.Root(isDebug = appInfo.isDebug).page
            SettingsPage.EDIT_USER_INFO -> UISettingsMocks.EditUser.page
            SettingsPage.TRIAL_SETTINGS -> UISettingsMocks.Trial.page
            SettingsPage.CLEAR_DATA -> UISettingsMocks.ClearData.page
            SettingsPage.DEBUG -> UISettingsMocks.Debug.page
        }
    }
}