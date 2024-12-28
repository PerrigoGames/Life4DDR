package com.perrigogames.life4.feature.motd

import com.perrigogames.life4.SettingsKeys.KEY_LAST_MOTD
import com.perrigogames.life4.model.BaseModel
import com.russhwolf.settings.Settings
import org.koin.core.component.inject

/**
 * Settings class to simplify reading and writing Message of the Day data.
 * MotD operates on an integer-based priority system, where a new message
 * is shown if the message's version number is higher than the one that is
 * currently saved.
 */
interface MotdSettings {
    fun shouldShowMotd(version: Int): Boolean
    fun setLastVersion(version: Int)
}

class DefaultMotdSettings : MotdSettings, BaseModel() {
    private val settings: Settings by inject()

    private var lastMotdId: Int
        get() = settings.getInt(KEY_LAST_MOTD, defaultValue = -1)
        set(value) = settings.putInt(KEY_LAST_MOTD, value)

    override fun shouldShowMotd(version: Int): Boolean {
        return version < lastMotdId
    }

    override fun setLastVersion(version: Int) {
        lastMotdId = version
    }
}