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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.preferences.home.MinimalClockStyle
import dev.kbzon.launcher.preferences.home.WaveIntensity
import dev.kbzon.launcher.preferences.home.LetterSort
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

/** [fork] Fase 15: "Riel y lista" en su propia pantalla. */
@Serializable
data object RailListSettingsRoute : NavKey

/** [fork] Fase 15: opciones propias de la búsqueda (barra abajo, contactos, atajos, prefijos). */
@Serializable
data object MinimalSearchSettingsRoute : NavKey

enum class HomeSettingsSection { Home, RailList, Search }

@Composable
fun MinimalHomeSettingsScreen(section: HomeSettingsSection = HomeSettingsSection.Home) {
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
    val letterSort by settings.letterSort.collectAsStateWithLifecycle(LetterSort.Alphabetical)
    val appFont by settings.appFont.collectAsStateWithLifecycle(dev.kbzon.launcher.preferences.home.AppFont.Manrope)
    val textScale by settings.textScale.collectAsStateWithLifecycle(1f)
    val iconStyle by settings.iconStyle.collectAsStateWithLifecycle(dev.kbzon.launcher.preferences.home.HomeIconStyle.Original)
    val bothEdges by settings.railBothEdges.collectAsStateWithLifecycle(true)
    val weatherKey by settings.weatherAppKey.collectAsStateWithLifecycle(null)
    val calendarKey by settings.calendarAppKey.collectAsStateWithLifecycle(null)
    val linkedKeys = listOfNotNull(weatherKey, calendarKey, clockKey)
    val linkedApps by remember(linkedKeys) {
        if (linkedKeys.isEmpty()) flowOf(emptyList()) else searchableRepository.getByKeys(linkedKeys)
    }.collectAsStateWithLifecycle(emptyList())
    var pickingFor by remember { mutableStateOf<String?>(null) }
    var editingKeywords by remember { mutableStateOf(false) } // "weather" | "calendar" | "clock"
    val clockLabels = mapOf(
        MinimalClockStyle.Thin to R.string.fork_clock_thin,
        MinimalClockStyle.Bold to R.string.fork_clock_bold,
        MinimalClockStyle.Stacked to R.string.fork_clock_stacked,
        MinimalClockStyle.Words to R.string.fork_clock_words,
        MinimalClockStyle.Analog to R.string.fork_clock_analog,
        MinimalClockStyle.DayProgress to R.string.fork_clock_day_progress,
        MinimalClockStyle.Vertical to R.string.fork_clock_vertical,
        MinimalClockStyle.DotMatrix to R.string.fork_clock_dots,
        MinimalClockStyle.Ring to R.string.fork_clock_ring,
    MinimalClockStyle.Vertical to R.string.fork_clock_vertical,
    MinimalClockStyle.DotMatrix to R.string.fork_clock_dots,
    MinimalClockStyle.Ring to R.string.fork_clock_ring,
    )
    val gestureOptions = remember {
        setOf(
            GestureAction.NoAction::class, GestureAction.Notifications::class, GestureAction.QuickSettings::class,
            GestureAction.ScreenLock::class, GestureAction.Search::class, GestureAction.PowerMenu::class,
            GestureAction.Recents::class, GestureAction.Launch::class,
        )
    }

    val screenTitle = when (section) {
        HomeSettingsSection.Home -> R.string.fork_preference_screen_minimal_home
        HomeSettingsSection.RailList -> R.string.fork_settings_rail_list
        HomeSettingsSection.Search -> R.string.fork_settings_search_extras
    }
    PreferenceScreen(title = stringResource(screenTitle)) {
        if (section == HomeSettingsSection.Home) item {
            PreferenceCategory {
                // [fork] Fase 13: estilo de íconos ("none" = sin íconos).
                ListPreference(
                    title = stringResource(R.string.fork_preference_minimal_home_icons),
                    icon = R.drawable.apps_24px,
                    items = listOf(
                        ListPreferenceItem(stringResource(R.string.fork_icons_original), "original"),
                        ListPreferenceItem(stringResource(R.string.fork_icons_monochrome), "monochrome"),
                        ListPreferenceItem(stringResource(R.string.fork_icons_outline), "outline"),
                        ListPreferenceItem(stringResource(R.string.fork_icons_none), "none"),
                    ),
                    value = if (!showIcons) "none" else when (iconStyle) {
                        dev.kbzon.launcher.preferences.home.HomeIconStyle.Original -> "original"
                        dev.kbzon.launcher.preferences.home.HomeIconStyle.Monochrome -> "monochrome"
                        dev.kbzon.launcher.preferences.home.HomeIconStyle.Outline -> "outline"
                    },
                    onValueChanged = {
                        when (it) {
                            "none" -> settings.setShowIcons(false)
                            "monochrome" -> settings.setIconStyle(dev.kbzon.launcher.preferences.home.HomeIconStyle.Monochrome)
                            "outline" -> settings.setIconStyle(dev.kbzon.launcher.preferences.home.HomeIconStyle.Outline)
                            else -> settings.setIconStyle(dev.kbzon.launcher.preferences.home.HomeIconStyle.Original)
                        }
                    },
                    enabled = enabled,
                )
                ListPreference(
                    title = stringResource(R.string.fork_preference_app_font),
                    icon = R.drawable.text_fields_24px,
                    items = listOf(
                        ListPreferenceItem("Manrope", dev.kbzon.launcher.preferences.home.AppFont.Manrope),
                        ListPreferenceItem("Inter", dev.kbzon.launcher.preferences.home.AppFont.Inter),
                        ListPreferenceItem(stringResource(R.string.fork_font_system), dev.kbzon.launcher.preferences.home.AppFont.System),
                        ListPreferenceItem(stringResource(R.string.fork_font_theme), dev.kbzon.launcher.preferences.home.AppFont.Theme),
                    ),
                    value = appFont,
                    onValueChanged = settings::setAppFont,
                )
                ListPreference(
                    title = stringResource(R.string.fork_preference_text_size),
                    icon = R.drawable.format_size_24px,
                    items = listOf(
                        ListPreferenceItem(stringResource(R.string.fork_text_size_small), 0.85f),
                        ListPreferenceItem(stringResource(R.string.fork_text_size_normal), 1f),
                        ListPreferenceItem(stringResource(R.string.fork_text_size_large), 1.15f),
                        ListPreferenceItem(stringResource(R.string.fork_text_size_xlarge), 1.3f),
                    ),
                    value = textScale,
                    onValueChanged = settings::setTextScale,
                )
                val themeBackStack = dev.kbzon.launcher.ui.locals.LocalBackStack.current
                Preference(
                    title = stringResource(R.string.fork_preference_color_theme),
                    summary = stringResource(R.string.fork_preference_color_theme_summary),
                    icon = R.drawable.palette_24px,
                    onClick = { themeBackStack.add(dev.kbzon.launcher.ui.settings.colorscheme.ColorSchemesSettingsRoute) },
                )
            }
        }
        if (section == HomeSettingsSection.Home) item {
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
        if (section == HomeSettingsSection.RailList) item {
            PreferenceCategory(title = stringResource(R.string.fork_preference_category_rail)) {
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
                ListPreference(
                    title = stringResource(R.string.fork_preference_letter_sort),
                    icon = R.drawable.sort_24px,
                    items = listOf(
                        ListPreferenceItem(stringResource(R.string.fork_letter_sort_alphabetical), LetterSort.Alphabetical),
                        ListPreferenceItem(stringResource(R.string.fork_letter_sort_most_used), LetterSort.MostUsed),
                        ListPreferenceItem(stringResource(R.string.fork_letter_sort_recent), LetterSort.Recent),
                        ListPreferenceItem(stringResource(R.string.fork_letter_sort_newest), LetterSort.Newest),
                    ),
                    value = letterSort,
                    onValueChanged = settings::setLetterSort,
                )
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
        if (section == HomeSettingsSection.Home) item {
            PreferenceCategory(title = stringResource(R.string.fork_preference_category_gestures)) {
                val linkBackStack = dev.kbzon.launcher.ui.locals.LocalBackStack.current
                Preference(
                    title = stringResource(R.string.fork_settings_rail_list),
                    summary = stringResource(R.string.fork_settings_rail_list_summary),
                    icon = R.drawable.sort_24px,
                    onClick = { linkBackStack.add(RailListSettingsRoute) },
                )
                val backStack = dev.kbzon.launcher.ui.locals.LocalBackStack.current
                Preference(
                    title = stringResource(R.string.fork_preference_gestures_link),
                    summary = stringResource(R.string.fork_preference_gestures_link_summary),
                    icon = R.drawable.gesture_24px,
                    onClick = { backStack.add(dev.kbzon.launcher.ui.settings.gestures.GesturesSettingsRoute) },
                )
            }
        }
        if (section == HomeSettingsSection.Search) item {
            PreferenceCategory(title = stringResource(R.string.fork_search_help_title)) {
                // [fork] Fase 14: barra abajo, contactos y atajos con palabra clave.
                val uiSettings: dev.kbzon.launcher.preferences.ui.UiSettings = org.koin.compose.koinInject()
                val contactSettings: dev.kbzon.launcher.preferences.search.ContactSearchSettings = org.koin.compose.koinInject()
                val bottomBar by uiSettings.bottomSearchBar.collectAsStateWithLifecycle(false)
                val callOnTap by contactSettings.callOnTap.collectAsStateWithLifecycle(false)
                SwitchPreference(
                    title = stringResource(R.string.fork_search_bottom_bar),
                    summary = stringResource(R.string.fork_search_bottom_bar_summary),
                    icon = R.drawable.search_24px,
                    value = bottomBar,
                    onValueChanged = uiSettings::setBottomSearchBar,
                )
                SwitchPreference(
                    title = stringResource(R.string.fork_search_call_on_tap),
                    summary = stringResource(R.string.fork_search_call_on_tap_summary),
                    icon = R.drawable.call_24px,
                    value = callOnTap,
                    onValueChanged = contactSettings::setCallOnTap,
                )
                Preference(
                    title = stringResource(R.string.fork_search_keywords_title),
                    summary = stringResource(R.string.fork_search_keywords_summary),
                    icon = R.drawable.keyboard_24px,
                    onClick = { editingKeywords = true },
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

    if (editingKeywords) SearchKeywordsDialog(onDismiss = { editingKeywords = false })

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

/** [fork] Fase 14: palabra clave de cada acción de búsqueda ("yt" → YouTube). */
@Composable
private fun SearchKeywordsDialog(onDismiss: () -> Unit) {
    val actions: dev.kbzon.launcher.searchactions.SearchActionService = org.koin.compose.koinInject()
    val settings: MinimalHomeSettings = org.koin.compose.koinInject()
    val builders by remember { actions.getSearchActionBuilders() }.collectAsStateWithLifecycle(emptyList())
    val overrides by settings.searchKeywords.collectAsStateWithLifecycle(emptyMap())
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.fork_search_keywords_title)) },
        text = {
            Column(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                Text(
                    stringResource(R.string.fork_search_keywords_help),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                builders.forEach { builder ->
                    val current = dev.kbzon.launcher.ui.search.SearchKeywords.keywordFor(builder, overrides).orEmpty()
                    var text by remember(builder.key, current) { mutableStateOf(current) }
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        Text(builder.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        androidx.compose.material3.OutlinedTextField(
                            value = text,
                            onValueChange = { v ->
                                val clean = v.filterNot { it.isWhitespace() }.take(6)
                                text = clean
                                settings.setSearchKeyword(builder.key, clean)
                            },
                            singleLine = true,
                            placeholder = { Text("—") },
                            modifier = Modifier.width(96.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.ok)) }
        },
    )
}
