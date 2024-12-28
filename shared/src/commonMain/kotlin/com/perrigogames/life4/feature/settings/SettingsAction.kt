package com.perrigogames.life4.feature.settings

/**
 * An action to be taken when a Settings object is interacted with.
 */
sealed class SettingsAction {
    data object None: SettingsAction()
    data class WebLink(val url: String): SettingsAction()
    data class Navigate(val page: SettingsPage): SettingsAction()
    data object NavigateBack: SettingsAction()
    data class Email(val email: String): SettingsAction()
    data class SetBoolean(val id: String, val newValue: Boolean): SettingsAction()
    data class Modal(val modal: SettingsPageModal): SettingsAction()
    data object ShowCredits: SettingsAction()
}