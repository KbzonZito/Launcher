package de.mm20.launcher2.ui.settings.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import de.mm20.launcher2.preferences.GestureAction
import de.mm20.launcher2.searchable.SavableSearchableRepository
import de.mm20.launcher2.ui.settings.gestures.GesturePreference
import kotlinx.coroutines.flow.flowOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import de.mm20.launcher2.preferences.home.MinimalHomeSettings
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.preferences.Preference
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
    val notificationPreviews by settings.notificationPreviews.collectAsStateWithLifecycle(true)
    val splitSwipeDown by settings.splitSwipeDown.collectAsStateWithLifecycle(true)
    val twoFingerUp by settings.twoFingerUp.collectAsStateWithLifecycle(null)
    val twoFingerDown by settings.twoFingerDown.collectAsStateWithLifecycle(null)
    val searchableRepository: SavableSearchableRepository = koinInject()
    val launchKeys = listOfNotNull(
        (twoFingerUp as? GestureAction.Launch)?.key,
        (twoFingerDown as? GestureAction.Launch)?.key,
    )
    val launchTargets by remember(launchKeys) {
        if (launchKeys.isEmpty()) flowOf(emptyList()) else searchableRepository.getByKeys(launchKeys)
    }.collectAsStateWithLifecycle(emptyList())
    val gestureOptions = remember {
        setOf(
            GestureAction.NoAction::class, GestureAction.Notifications::class, GestureAction.QuickSettings::class,
            GestureAction.ScreenLock::class, GestureAction.Search::class, GestureAction.PowerMenu::class,
            GestureAction.Recents::class, GestureAction.Launch::class,
        )
    }

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
                    title = stringResource(R.string.fork_preference_minimal_home_notification_previews),
                    summary = stringResource(R.string.fork_preference_minimal_home_notification_previews_summary),
                    icon = R.drawable.notifications_24px,
                    value = notificationPreviews,
                    onValueChanged = settings::setNotificationPreviews,
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
        item {
            PreferenceCategory(title = stringResource(R.string.fork_preference_category_gestures)) {
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_split_swipe_down),
                    summary = stringResource(R.string.fork_preference_split_swipe_down_summary),
                    icon = R.drawable.swipe_down_alt_24px,
                    value = splitSwipeDown,
                    onValueChanged = settings::setSplitSwipeDown,
                    enabled = enabled,
                )
                GesturePreference(
                    title = stringResource(R.string.fork_preference_two_finger_up),
                    icon = R.drawable.swipe_up_alt_24px,
                    value = twoFingerUp,
                    onValueChanged = { action, target -> target?.let { searchableRepository.insert(it) }; settings.setTwoFingerUp(action) },
                    options = gestureOptions,
                    shortcutOptions = launchTargets,
                    widgetOptions = emptyList(),
                )
                GesturePreference(
                    title = stringResource(R.string.fork_preference_two_finger_down),
                    icon = R.drawable.swipe_down_alt_24px,
                    value = twoFingerDown,
                    onValueChanged = { action, target -> target?.let { searchableRepository.insert(it) }; settings.setTwoFingerDown(action) },
                    options = gestureOptions,
                    shortcutOptions = launchTargets,
                    widgetOptions = emptyList(),
                )
                Preference(
                    title = stringResource(R.string.fork_menu_shortcuts),
                    summary = stringResource(R.string.fork_preference_app_swipe_hint),
                    icon = R.drawable.swipe_right_alt_24px,
                )
            }
        }
        item {
            PreferenceCategory(title = stringResource(R.string.fork_search_help_title)) {
                Preference(
                    title = stringResource(R.string.fork_search_prefixes_title),
                    summary = stringResource(R.string.fork_search_prefixes_summary),
                    icon = R.drawable.search_24px,
                )
                Preference(
                    title = stringResource(R.string.fork_search_side_button_title),
                    summary = stringResource(R.string.fork_search_side_button_summary),
                    icon = R.drawable.power_24px,
                )
            }
        }
    }
}
