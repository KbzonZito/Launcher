package dev.kbzon.launcher.ui.settings.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.preferences.Preference
import dev.kbzon.launcher.ui.component.preferences.PreferenceCategory
import dev.kbzon.launcher.ui.component.preferences.PreferenceScreen
import dev.kbzon.launcher.ui.locals.LocalBackStack
import dev.kbzon.launcher.ui.settings.about.AboutSettingsRoute
import dev.kbzon.launcher.ui.settings.appearance.AppearanceSettingsRoute
import dev.kbzon.launcher.ui.settings.backup.BackupSettingsRoute
import dev.kbzon.launcher.ui.settings.debug.DebugSettingsRoute
import dev.kbzon.launcher.ui.settings.gestures.GesturesSettingsRoute
import dev.kbzon.launcher.ui.settings.homescreen.HomescreenSettingsRoute
import dev.kbzon.launcher.ui.settings.icons.IconsSettingsRoute
import dev.kbzon.launcher.ui.settings.integrations.IntegrationsSettingsRoute
import dev.kbzon.launcher.ui.settings.locale.LocaleSettingsRoute
import dev.kbzon.launcher.ui.settings.privacy.PrivacySettingsRoute
import dev.kbzon.launcher.ui.settings.search.SearchSettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object MainRoute: NavKey

@Composable
fun MainSettingsScreen() {
    val backStack = LocalBackStack.current
    PreferenceScreen(
        title = stringResource(R.string.settings),
    ) {
        item {
            PreferenceCategory {
                Preference( // [fork]
                    icon = R.drawable.home_24px,
                    title = stringResource(id = R.string.fork_preference_screen_minimal_home),
                    summary = stringResource(id = R.string.fork_preference_screen_minimal_home_summary),
                    onClick = {
                        backStack.add(dev.kbzon.launcher.ui.settings.home.MinimalHomeSettingsRoute)
                    }
                )
                Preference(
                    icon = R.drawable.palette_24px,
                    title = stringResource(id = R.string.preference_screen_appearance),
                    summary = stringResource(id = R.string.preference_screen_appearance_summary),
                    onClick = {
                        backStack.add(AppearanceSettingsRoute)
                    }
                )
                Preference(
                    icon = R.drawable.home_24px,
                    title = stringResource(id = R.string.preference_screen_homescreen),
                    summary = stringResource(id = R.string.preference_screen_homescreen_summary),
                    onClick = {
                        backStack.add(HomescreenSettingsRoute)
                    }
                )
                Preference(
                    icon = R.drawable.apps_24px,
                    title = stringResource(id = R.string.preference_screen_icons),
                    summary = stringResource(id = R.string.preference_screen_icons_summary),
                    onClick = {
                        backStack.add(IconsSettingsRoute)
                    }
                )
                Preference(
                    icon = R.drawable.search_24px,
                    title = stringResource(id = R.string.preference_screen_search),
                    summary = stringResource(id = R.string.preference_screen_search_summary),
                    onClick = {
                        backStack.add(SearchSettingsRoute)
                    }
                )
                Preference(
                    icon = R.drawable.gesture_24px,
                    title = stringResource(id = R.string.preference_screen_gestures),
                    summary = stringResource(id = R.string.preference_screen_gestures_summary),
                    onClick = {
                        backStack.add(GesturesSettingsRoute)
                    }
                )
                Preference( // [fork]
                    icon = R.drawable.cloud_off_24px,
                    title = stringResource(id = R.string.fork_preference_screen_privacy),
                    summary = stringResource(id = R.string.fork_preference_screen_privacy_summary),
                    onClick = {
                        backStack.add(PrivacySettingsRoute)
                    }
                )
                Preference(
                    icon = R.drawable.power_24px,
                    title = stringResource(id = R.string.preference_screen_integrations),
                    summary = stringResource(id = R.string.preference_screen_integrations_summary),
                    onClick = {
                        backStack.add(IntegrationsSettingsRoute)
                    }
                )
                Preference(
                    icon = R.drawable.translate_24px,
                    title = stringResource(id = R.string.preference_screen_locale),
                    summary = stringResource(id = R.string.preference_screen_locale_summary),
                    onClick = {
                        backStack.add(LocaleSettingsRoute)
                    }
                )
                Preference(
                    icon = R.drawable.settings_backup_restore_24px,
                    title = stringResource(id = R.string.preference_screen_backup),
                    summary = stringResource(id = R.string.preference_screen_backup_summary),
                    onClick = {
                        backStack.add(BackupSettingsRoute)
                    }
                )
                Preference(
                    icon = R.drawable.bug_report_24px,
                    title = stringResource(id = R.string.preference_screen_debug),
                    summary = stringResource(id = R.string.preference_screen_debug_summary),
                    onClick = {
                        backStack.add(DebugSettingsRoute)
                    }
                )
                Preference(
                    icon = R.drawable.info_24px,
                    title = stringResource(id = R.string.preference_screen_about),
                    summary = stringResource(id = R.string.preference_screen_about_summary),
                    onClick = {
                        backStack.add(AboutSettingsRoute)
                    }
                )
            }
        }
    }
}