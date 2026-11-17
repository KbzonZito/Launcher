package dev.kbzon.launcher.ui.settings.calendarsearch

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import dev.kbzon.launcher.calendar.providers.CalendarList
import dev.kbzon.launcher.themes.colors.atTone
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.preferences.CheckboxPreference
import dev.kbzon.launcher.ui.component.preferences.PreferenceCategory
import dev.kbzon.launcher.ui.component.preferences.PreferenceScreen
import dev.kbzon.launcher.ui.component.preferences.SwitchPreference
import dev.kbzon.launcher.ui.locals.LocalDarkTheme
import kotlinx.serialization.Serializable

@Serializable
data class CalendarProviderSettingsRoute(val providerId: String): NavKey

@Composable
fun CalendarProviderSettingsScreen(providerId: String) {
    val viewModel = viewModel<CalendarProviderSettingsScreenVM>()
    LaunchedEffect(providerId) {
        viewModel.init(providerId)
    }

    val enabled by viewModel.isProviderEnabled.collectAsStateWithLifecycle(false)
    val calendarLists by viewModel.calendarLists.collectAsStateWithLifecycle(sortedMapOf<String, List<CalendarList>>())
    val excludedCalendars by viewModel.excludedCalendars.collectAsStateWithLifecycle(emptySet())

    val providerAvailable = providerId == "local" || providerId == "tasks.org" // [fork] sin plugins

    PreferenceScreen(
        title = stringResource(R.string.preference_search_calendar)
    ) {
        if (!providerAvailable) {
            return@PreferenceScreen
        }
        item {
            PreferenceCategory {
                SwitchPreference(
                    title =
                        if (providerId == "local") stringResource(R.string.preference_search_calendar)
                        else if (providerId == "tasks.org") stringResource(R.string.preference_search_tasks)
                        else "",
                    summary =
                        if (providerId == "local") stringResource(R.string.preference_search_local_calendar_summary)
                        else if (providerId == "tasks.org") stringResource(R.string.preference_search_tasks_summary)
                        else null,
                    value = enabled,
                    onValueChanged = { viewModel.setProviderEnabled(providerId, it) }
                )
            }
        }
        items(calendarLists.toList()) { (k, v) ->
            PreferenceCategory(
                title = k,
            ) {
                for (list in v) {
                    CheckboxPreference(
                        title = list.name,
                        value = !excludedCalendars.contains(list.id),
                        onValueChanged = { viewModel.setCalendarExcluded(list.id, !it) },
                        checkboxColors = CheckboxDefaults.colors(
                            checkedColor = if (list.color == 0) MaterialTheme.colorScheme.primary
                            else Color(
                                list.color.atTone(if (LocalDarkTheme.current) 80 else 40)
                            ),
                            checkmarkColor = if (list.color == 0) MaterialTheme.colorScheme.onPrimary
                            else Color(
                                list.color.atTone(if (LocalDarkTheme.current) 20 else 100)
                            )
                        ),
                        enabled = enabled,
                    )
                }
            }
        }
    }
}