package com.perrigogames.life4.model

import com.perrigogames.life4.SettingsKeys.KEY_HIGHEST_CALORIES
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import org.koin.core.inject

class CaloriesManager: BaseModel() {

    private val settings: Settings by inject()

    var highestCaloriesBurned: Int
        get() = settings[KEY_HIGHEST_CALORIES, 0]
        set(value) { settings[KEY_HIGHEST_CALORIES] = value }
}