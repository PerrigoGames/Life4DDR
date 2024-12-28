package com.perrigogames.life4.feature.settings

import com.perrigogames.life4.MR
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc

/**
 * Describes the contents of the Settings screen.  A Flow of these items should
 * be expected, updating the content as the user navigates through it.
 */
data class UISettingsData(
    val screenTitle: StringDesc,
    val settingsItems: List<UISettingsItem>
)

/**
 * Describes a single clickable item in the Settings screen.
 */
sealed class UISettingsItem {

    /**
     * A text header with no clickability.
     * @param title the text the header should display
     */
    data class Header(
        val title: StringDesc
    ) : UISettingsItem()

    /**
     * A link item that only consists of text. Usually performs a navigation action.
     * @param title The main text to be shown.
     * @param subtitle An optional less emphasized text to show.
     * @param action The [SettingsAction] to be taken when the checkbox item is clicked.
     * @param enabled Whether the item should be interactable. Defaults to true.
     */
    data class Link(
        val title: StringDesc,
        val subtitle: StringDesc? = null,
        val action: SettingsAction,
        val enabled: Boolean = true,
    ) : UISettingsItem()

    /**
     * A link item that only consists of text. Usually performs a navigation action.
     * @param title The main text to be shown.
     * @param subtitle An optional less emphasized text to show.
     * @param dropdownItems
     * @param enabled Whether the item should be interactable. Defaults to true.
     */
    data class Dropdown(
        val title: StringDesc,
        val subtitle: StringDesc? = null,
        val dropdownItems: List<StringDesc>,
        val selectedIndex: Int,
    )

    /**
     * A checkbox item that controls a boolean flag in the settings.
     * @param title The main text to be shown.
     * @param subtitle An optional less emphasized text to show.
     * @param action The [SettingsAction] to be taken when the checkbox item is clicked.
     * @param enabled Whether the item should be interactable. Defaults to true.
     * @param toggled The current toggled state of checkbox. Defaults to false.
     */
    data class Checkbox(
        val title: StringDesc,
        val subtitle: StringDesc? = null,
        val action: SettingsAction,
        val enabled: Boolean = true,
        val toggled: Boolean = false,
    ) : UISettingsItem()

    /**
     * A divider object.
     */
    data object Divider : UISettingsItem()
}

/**
 * Enum describing the different pages available to the settings flow. Typically sent with
 * [SettingsAction]s.
 */
enum class SettingsPage(val nameDesc: StringDesc) {
    ROOT(StringDesc.Resource(MR.strings.action_settings)),
    EDIT_USER_INFO(StringDesc.Resource(MR.strings.edit_user_info)),
    TRIAL_SETTINGS(StringDesc.Resource(MR.strings.trial_settings)),
    CLEAR_DATA(StringDesc.Resource(MR.strings.clear_data)),
}

sealed class SettingsPageModal {
    data object GameVersion : SettingsPageModal()
    data object AppVersion : SettingsPageModal()
}