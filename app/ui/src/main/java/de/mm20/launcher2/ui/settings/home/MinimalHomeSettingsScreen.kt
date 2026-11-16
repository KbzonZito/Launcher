package de.mm20.launcher2.ui.settings.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.preferences.home.MinimalClockStyle
import de.mm20.launcher2.ui.common.SearchablePicker
import de.mm20.launcher2.ui.component.DismissableBottomSheet
import de.mm20.launcher2.ui.component.preferences.ListPreference
import de.mm20.launcher2.ui.component.preferences.ListPreferenceItem
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
    val searchStyle by settings.searchStyle.collectAsStateWithLifecycle(true)
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
    val clockStyle by settings.clockStyle.collectAsStateWithLifecycle(MinimalClockStyle.Thin)
    val agenda by settings.agenda.collectAsStateWithLifecycle(true)
    val weatherKey by settings.weatherAppKey.collectAsStateWithLifecycle(null)
    val calendarKey by settings.calendarAppKey.collectAsStateWithLifecycle(null)
    val linkedKeys = listOfNotNull(weatherKey, calendarKey)
    val linkedApps by remember(linkedKeys) {
        if (linkedKeys.isEmpty()) flowOf(emptyList()) else searchableRepository.getByKeys(linkedKeys)
    }.collectAsStateWithLifecycle(emptyList())
    var pickingFor by remember { mutableStateOf<String?>(null) } // "weather" | "calendar"
    val clockLabels = mapOf(
        MinimalClockStyle.Thin to R.string.fork_clock_thin,
        MinimalClockStyle.Bold to R.string.fork_clock_bold,
        MinimalClockStyle.Stacked to R.string.fork_clock_stacked,
        MinimalClockStyle.Words to R.string.fork_clock_words,
        MinimalClockStyle.Analog to R.string.fork_clock_analog,
        MinimalClockStyle.DayProgress to R.string.fork_clock_day_progress,
    )
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
            PreferenceCategory(title = stringResource(R.string.fork_preference_category_clock)) {
                ListPreference(
                    title = stringResource(R.string.fork_preference_clock_style),
                    icon = R.drawable.schedule_24px,
                    items = MinimalClockStyle.entries.map { ListPreferenceItem(stringResource(clockLabels.getValue(it)), it) },
                    value = clockStyle,
                    onValueChanged = settings::setClockStyle,
                    enabled = enabled,
                )
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_agenda),
                    summary = stringResource(R.string.fork_preference_agenda_summary),
                    icon = R.drawable.event_24px,
                    value = agenda,
                    onValueChanged = settings::setAgenda,
                    enabled = enabled,
                )
                Preference(
                    title = stringResource(R.string.fork_preference_weather_app),
                    summary = linkedApps.firstOrNull { it.key == weatherKey }?.let { it.labelOverride ?: it.label }
                        ?: stringResource(R.string.fork_preference_linked_app_auto),
                    icon = R.drawable.light_mode_24px,
                    onClick = { pickingFor = "weather" },
                    enabled = enabled,
                )
                Preference(
                    title = stringResource(R.string.fork_preference_calendar_app),
                    summary = linkedApps.firstOrNull { it.key == calendarKey }?.let { it.labelOverride ?: it.label }
                        ?: stringResource(R.string.fork_preference_linked_app_auto),
                    icon = R.drawable.calendar_today_24px,
                    onClick = { pickingFor = "calendar" },
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
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_minimal_search),
                    summary = stringResource(R.string.fork_preference_minimal_search_summary),
                    icon = R.drawable.search_24px,
                    value = searchStyle,
                    onValueChanged = settings::setSearchStyle,
                    enabled = enabled,
                )
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

    DismissableBottomSheet(expanded = pickingFor != null, onDismissRequest = { pickingFor = null }) {
        val forWeather = pickingFor == "weather"
        Column {
            Text(
                stringResource(if (forWeather) R.string.fork_preference_weather_app else R.string.fork_preference_calendar_app),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            TextButton(
                onClick = {
                    if (forWeather) settings.setWeatherAppKey(null) else settings.setCalendarAppKey(null)
                    pickingFor = null
                },
                modifier = Modifier.padding(horizontal = 12.dp),
            ) { Text(stringResource(R.string.fork_preference_linked_app_auto)) }
            SearchablePicker(
                value = linkedApps.firstOrNull { it.key == (if (forWeather) weatherKey else calendarKey) },
                onValueChanged = { app ->
                    app?.let { searchableRepository.insert(it) }
                    if (forWeather) settings.setWeatherAppKey(app?.key) else settings.setCalendarAppKey(app?.key)
                    pickingFor = null
                },
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            )
        }
    }
}
