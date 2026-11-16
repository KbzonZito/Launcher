package dev.kbzon.launcher.ui.settings.locale

import androidx.lifecycle.ViewModel
import dev.kbzon.launcher.preferences.MeasurementSystem
import dev.kbzon.launcher.preferences.TimeFormat
import dev.kbzon.launcher.preferences.ui.LocaleSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CalendarSettingsScreenVM: ViewModel(), KoinComponent {
    private val localeSettings: LocaleSettings by inject()

    val primaryCalendar = localeSettings.primaryCalendar
    fun setPrimaryCalendar(calendar: String?) {
        localeSettings.setPrimaryCalendar(calendar)
    }

    val secondaryCalendar = localeSettings.secondaryCalendar
    fun setSecondaryCalendar(calendar: String?) {
        localeSettings.setSecondaryCalendar(calendar)
    }
}