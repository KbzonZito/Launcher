package dev.kbzon.launcher.ui.settings.calendarsearch

import androidx.lifecycle.ViewModel
import dev.kbzon.launcher.calendar.CalendarRepository
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.search.CalendarSearchSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CalendarProviderSettingsScreenVM: ViewModel(), KoinComponent {
    private val providerId = MutableStateFlow<String>("")

    fun init(providerId: String) {
        this.providerId.value = providerId
    }

    private val calendarSearchSettings: CalendarSearchSettings by inject()
    private val calendarRepository: CalendarRepository by inject()

    val isProviderEnabled = providerId.flatMapLatest { calendarSearchSettings.isProviderEnabled(it) }
    fun setProviderEnabled(providerId: String, enabled: Boolean) {
        calendarSearchSettings.setProviderEnabled(providerId, enabled)
    }

    val calendarLists = providerId
        .flatMapLatest { calendarRepository.getCalendars(it) }
        .map { it.groupBy { it.owner }.toSortedMap(compareBy { it }) }

    val excludedCalendars = calendarSearchSettings.excludedCalendars

    fun setCalendarExcluded(calendarId: String, excluded: Boolean) {
        calendarSearchSettings.setCalendarExcluded(calendarId, excluded)
    }
}