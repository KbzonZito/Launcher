package dev.kbzon.launcher.ui.base

import android.icu.util.Calendar
import android.icu.util.LocaleData
import android.icu.util.ULocale
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.kbzon.launcher.ktx.isAtLeastApiLevel
import dev.kbzon.launcher.preferences.IconShape
import dev.kbzon.launcher.preferences.MeasurementSystem
import dev.kbzon.launcher.preferences.TimeFormat
import dev.kbzon.launcher.preferences.ui.GridSettings
import dev.kbzon.launcher.preferences.ui.LocaleSettings
import dev.kbzon.launcher.preferences.ui.SearchUiSettings
import dev.kbzon.launcher.preferences.ui.UiSettings
import dev.kbzon.launcher.ui.component.ProvideIconShape
import dev.kbzon.launcher.ui.locals.LocalCalendarSystemIds
import dev.kbzon.launcher.ui.locals.LocalCalendarSystems
import dev.kbzon.launcher.ui.locals.LocalFavoritesEnabled
import dev.kbzon.launcher.ui.locals.LocalShowAppDetails
import dev.kbzon.launcher.ui.locals.LocalGridSettings
import dev.kbzon.launcher.ui.locals.LocalMeasurementSystem
import dev.kbzon.launcher.ui.locals.LocalTimeFormat
import dev.kbzon.launcher.widgets.AppsWidget
import dev.kbzon.launcher.widgets.WidgetRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

@Composable
fun ProvideSettings(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val settings: UiSettings = koinInject()
    val searchUiSettings: SearchUiSettings = koinInject()
    val widgetRepository: WidgetRepository = koinInject()
    val localeSettings: LocaleSettings = koinInject()

    val iconShape by remember {
        settings.iconShape.distinctUntilChanged()
    }.collectAsState(IconShape.Circle)

    val favoritesEnabled by remember {
        combine(
            widgetRepository.exists(AppsWidget.Type),
            settings.favoritesEnabled,
        ) { a, b -> a || b }.distinctUntilChanged()
    }.collectAsState(true)

    val gridSettings by remember {
        settings.gridSettings.distinctUntilChanged()
    }.collectAsState(GridSettings())

    val showAppDetails by remember {
        searchUiSettings.showAppDetails.distinctUntilChanged()
    }.collectAsState(false)

    val timeFormat by remember(context) {
        localeSettings.timeFormat
            .map {
                if (it == TimeFormat.System) {
                    if (DateFormat.is24HourFormat(context)) TimeFormat.TwentyFourHour else TimeFormat.TwelveHour
                } else {
                    it
                }
            }.distinctUntilChanged()
    }.collectAsState(null)

    val measurementSystem by remember {
        localeSettings.measurementSystem.map { ms ->
            if (ms == MeasurementSystem.System) {
                return@map if (isAtLeastApiLevel(28)) {
                    val systemMs = LocaleData.getMeasurementSystem(ULocale.getDefault())
                    when (systemMs) {
                        LocaleData.MeasurementSystem.UK -> MeasurementSystem.UnitedKingdom
                        LocaleData.MeasurementSystem.US -> MeasurementSystem.UnitedStates
                        else -> MeasurementSystem.Metric
                    }
                } else {
                    MeasurementSystem.Metric
                }
            }
            return@map ms
        }.distinctUntilChanged()
    }.collectAsState(null)

    val calendarIds by remember {
        localeSettings.primaryCalendar.combine(localeSettings.secondaryCalendar) { p, s ->
            listOf(p, s)
        }
    }.collectAsState(listOf(null, null))

    val calendars by remember {
        derivedStateOf {
            val p = calendarIds.first()
            val s = calendarIds.last()
            val primary = if (p == null) {
                Calendar.getInstance(ULocale.getDefault())
            } else {
                Calendar.getInstance(ULocale.getDefault().setKeywordValue("calendar", p))
            }
            val secondary = if (s == null) {
                null
            } else {
                Calendar.getInstance(ULocale.getDefault().setKeywordValue("calendar", s))
            }
            listOfNotNull(
                primary, secondary
            )
        }
    }

    if (timeFormat == null || measurementSystem == null) return

    CompositionLocalProvider(
        LocalFavoritesEnabled provides favoritesEnabled,
        LocalShowAppDetails provides showAppDetails,
        LocalGridSettings provides gridSettings,
        LocalTimeFormat provides timeFormat!!,
        LocalMeasurementSystem provides measurementSystem!!,
        LocalCalendarSystems provides calendars,
        LocalCalendarSystemIds provides calendarIds,
    ) {
        ProvideIconShape(iconShape) {
            content()
        }
    }

}