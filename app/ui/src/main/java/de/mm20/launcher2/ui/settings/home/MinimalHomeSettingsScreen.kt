package de.mm20.launcher2.ui.settings.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import de.mm20.launcher2.preferences.home.MinimalHomeSettings
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.preferences.PreferenceCategory
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import de.mm20.launcher2.ui.component.preferences.SwitchPreference
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

// [fork] Ajustes de la home minimalista.
@Serializable
data object MinimalHomeSettingsRoute : NavKey

@Composable
fun MinimalHomeSettingsScreen() {
    val settings: MinimalHomeSettings = koinInject()
    val enabled by settings.enabled.collectAsStateWithLifecycle(true)
    val showIcons by settings.showIcons.collectAsStateWithLifecycle(true)
    val railOnLeft by settings.railOnLeft.collectAsStateWithLifecycle(false)

    PreferenceScreen(title = stringResource(R.string.fork_preference_screen_minimal_home)) {
        item {
            PreferenceCategory {
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_minimal_home),
                    summary = stringResource(R.string.fork_preference_minimal_home_summary),
                    icon = R.drawable.home_24px,
                    value = enabled,
                    onValueChanged = settings::setEnabled,
                )
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_minimal_home_icons),
                    icon = R.drawable.apps_24px,
                    value = showIcons,
                    onValueChanged = settings::setShowIcons,
                    enabled = enabled,
                )
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_minimal_home_rail_left),
                    summary = stringResource(R.string.fork_preference_minimal_home_rail_left_summary),
                    icon = R.drawable.gesture_24px,
                    value = railOnLeft,
                    onValueChanged = settings::setRailOnLeft,
                    enabled = enabled,
                )
            }
        }
    }
}
