package com.perrigogames.life4.feature.motd

import com.perrigogames.life4.api.base.unwrapLoaded
import com.perrigogames.life4.data.MessageOfTheDay
import com.perrigogames.life4.model.BaseModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

interface MotdManager {
    val motdFlow: SharedFlow<Event?>
    val dataVersionString: Flow<String>
}

class DefaultMotdManager: MotdManager, BaseModel() {

    private val _motdFlow = MutableSharedFlow<Event?>(replay = 8)
    override val motdFlow: SharedFlow<Event?> = _motdFlow

    private val data: MotdLocalRemoteData by inject()
    private val settings: MotdSettings by inject()

    override val dataVersionString: Flow<String> =
        data.versionState.map { it.versionString }

    init {
        mainScope.launch {
            combine(
                data.dataState,
                data.versionState
            ) { data, version ->
                if (version.majorVersionBlocked) {
                    _motdFlow.emit(Event.DataRequiresAppUpdateEvent)
                } else if (settings.shouldShowMotd(version.version)) {
                    data.unwrapLoaded()?.let {
                        _motdFlow.emit(Event.MotdEvent(it))
                    }
                }
            }
        }
        mainScope.launch {
            data.start()
        }
    }
}

sealed class Event {
    data object DataRequiresAppUpdateEvent: Event()
    data class MotdEvent(val motd: MessageOfTheDay): Event()
}
