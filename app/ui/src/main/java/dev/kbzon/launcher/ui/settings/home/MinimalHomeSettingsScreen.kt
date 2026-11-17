package dev.kbzon.launcher.ui.settings.home

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
import dev.kbzon.launcher.preferences.home.MinimalClockStyle
import dev.kbzon.launcher.preferences.home.WaveIntensity
import dev.kbzon.launcher.ui.common.SearchablePicker
import dev.kbzon.launcher.ui.component.DismissableBottomSheet
import dev.kbzon.launcher.ui.component.preferences.ListPreference
import dev.kbzon.launcher.ui.component.preferences.ListPreferenceItem
import dev.kbzon.launcher.preferences.GestureAction
import dev.kbzon.launcher.searchable.SavableSearchableRepository
import dev.kbzon.launcher.ui.settings.gestures.GesturePreference
import kotlinx.coroutines.flow.flowOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import dev.kbzon.launcher.preferences.home.MinimalHomeSettings
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.preferences.Preference
import dev.kbzon.launcher.ui.component.preferences.PreferenceCategory
import dev.kbzon.launcher.ui.component.preferences.PreferenceScreen
import dev.kbzon.launcher.ui.component.preferences.SwitchPreference
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

// [fork] Ajustes de la home minimalista.
@Serializable
data object MinimalHomeSettingsRoute : NavKey

@Composable
fun MinimalHomeSettingsScreen() {
    val settings: MinimalHomeSettings = koinInject()
    val enabled = true // [fork] la home minimalista ya no es opcional
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
    val clockStyle by settings.clockStyle.collectAsStateWithLifecycle(MinimalClockStyle.Thin)
    val agenda by settings.agenda.collectAsStateWithLifecycle(true)
    val mediaPlayer by settings.mediaPlayer.collectAsStateWithLifecycle(true)
    val clockKey by settings.clockAppKey.collectAsStateWithLifecycle(null)
    val usageSuggestions by settings.usageSuggestions.collectAsStateWithLifecycle(true)
    val railDoubleTap by settings.railDoubleTapLock.collectAsStateWithLifecycle(true)
    val wave by settings.waveIntensity.collectAsStateWithLifecycle(WaveIntensity.Normal)
    val bothEdges by settings.railBothEdges.collectAsStateWithLifecycle(true)
    val weatherKey by settings.weatherAppKey.collectAsStateWithLifecycle(null)
    val calendarKey by settings.calendarAppKey.collectAsStateWithLifecycle(null)
    val linkedKeys = listOfNotNull(weatherKey, calendarKey, clockKey)
    val linkedApps by remember(linkedKeys) {
        if (linkedKeys.isEmpty()) flowOf(emptyList()) else searchableRepository.getByKeys(linkedKeys)
    }.collectAsStateWithLifecycle(emptyList())
    var pickingFor by remember { mutableStateOf<String?>(null) } // "weather" | "calendar" | "clock"
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
                val audioApps by settings.audioMediaApps.collectAsStateWithLifecycle(true)
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_audio_media_apps),
                    summary = stringResource(R.string.fork_preference_audio_media_apps_summary),
                    icon = R.drawable.headphones_24px,
                    value = audioApps,
                    onValueChanged = settings::setAudioMediaApps,
                )
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_media_player),
                    summary = stringResource(R.string.fork_preference_media_player_summary),
                    icon = R.drawable.play_circle_24px,
                    value = mediaPlayer,
                    onValueChanged = settings::setMediaPlayer,
                    enabled = enabled,
                )
                Preference(
                    title = stringResource(R.string.fork_preference_clock_app),
                    summary = linkedApps.firstOrNull { it.key == clockKey }?.let { it.labelOverride ?: it.label }
                        ?: stringResource(R.string.fork_preference_clock_app_auto),
                    icon = R.drawable.alarm_24px,
                    onClick = { pickingFor = "clock" },
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
            PreferenceCategory(title = stringResource(R.string.fork_preference_category_rail)) {
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_usage_suggestions),
                    summary = stringResource(R.string.fork_preference_usage_suggestions_summary),
                    icon = R.drawable.schedule_24px,
                    value = usageSuggestions,
                    onValueChanged = settings::setUsageSuggestions,
                )
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_rail_double_tap),
                    summary = stringResource(R.string.fork_preference_rail_double_tap_summary),
                    icon = R.drawable.lock_24px,
                    value = railDoubleTap,
                    onValueChanged = settings::setRailDoubleTapLock,
                )
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_rail_both_edges),
                    summary = stringResource(R.string.fork_preference_rail_both_edges_summary),
                    icon = R.drawable.swipe_left_alt_24px,
                    value = bothEdges,
                    onValueChanged = settings::setRailBothEdges,
                )
                ListPreference(
                    title = stringResource(R.string.fork_preference_wave),
                    icon = R.drawable.gesture_24px,
                    items = listOf(
                        ListPreferenceItem(stringResource(R.string.fork_wave_soft), WaveIntensity.Soft),
                        ListPreferenceItem(stringResource(R.string.fork_wave_normal), WaveIntensity.Normal),
                        ListPreferenceItem(stringResource(R.string.fork_wave_strong), WaveIntensity.Strong),
                    ),
                    value = wave,
                    onValueChanged = settings::setWaveIntensity,
                )
            }
        }
        item {
            PreferenceCategory(title = stringResource(R.string.fork_preference_category_gestures)) {
                val backStack = dev.kbzon.launcher.ui.locals.LocalBackStack.current
                Preference(
                    title = stringResource(R.string.fork_preference_gestures_link),
                    summary = stringResource(R.string.fork_preference_gestures_link_summary),
                    icon = R.drawable.gesture_24px,
                    onClick = { backStack.add(dev.kbzon.launcher.ui.settings.gestures.GesturesSettingsRoute) },
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

    DismissableBottomSheet(expanded = pickingFor != null, onDismissRequest = { pickingFor = null }) {
        val forWeather = pickingFor == "weather"
        val forClock = pickingFor == "clock"
        Column {
            Text(
                stringResource(
                    when {
                        forClock -> R.string.fork_preference_clock_app
                        forWeather -> R.string.fork_preference_weather_app
                        else -> R.string.fork_preference_calendar_app
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            TextButton(
                onClick = {
                    when {
                        forClock -> settings.setClockAppKey(null)
                        forWeather -> settings.setWeatherAppKey(null)
                        else -> settings.setCalendarAppKey(null)
                    }
                    pickingFor = null
                },
                modifier = Modifier.padding(horizontal = 12.dp),
            ) { Text(stringResource(R.string.fork_preference_linked_app_auto)) }
            SearchablePicker(
                value = linkedApps.firstOrNull { it.key == (if (forClock) clockKey else if (forWeather) weatherKey else calendarKey) },
                onValueChanged = { app ->
                    app?.let { searchableRepository.insert(it) }
                    when {
                        forClock -> settings.setClockAppKey(app?.key)
                        forWeather -> settings.setWeatherAppKey(app?.key)
                        else -> settings.setCalendarAppKey(app?.key)
                    }
                    pickingFor = null
                },
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            )
        }
    }
}
