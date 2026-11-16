package dev.kbzon.launcher.preferences

import dev.kbzon.launcher.backup.Backupable
import dev.kbzon.launcher.preferences.feed.FeedSettings
import dev.kbzon.launcher.preferences.search.ContactSearchSettings
import dev.kbzon.launcher.preferences.media.MediaSettings
import dev.kbzon.launcher.preferences.search.CalculatorSearchSettings
import dev.kbzon.launcher.preferences.search.CalendarSearchSettings
import dev.kbzon.launcher.preferences.search.FavoritesSettings
import dev.kbzon.launcher.preferences.search.FileSearchSettings
import dev.kbzon.launcher.preferences.search.LocationSearchSettings
import dev.kbzon.launcher.preferences.search.RankingSettings
import dev.kbzon.launcher.preferences.search.SearchFilterSettings
import dev.kbzon.launcher.preferences.search.ShortcutSearchSettings
import dev.kbzon.launcher.preferences.search.UnitConverterSettings
import dev.kbzon.launcher.preferences.search.WebsiteSearchSettings
import dev.kbzon.launcher.preferences.search.WikipediaSearchSettings
import dev.kbzon.launcher.preferences.ui.BadgeSettings
import dev.kbzon.launcher.preferences.ui.ClockWidgetSettings
import dev.kbzon.launcher.preferences.ui.GestureSettings
import dev.kbzon.launcher.preferences.ui.IconSettings
import dev.kbzon.launcher.preferences.ui.LocaleSettings
import dev.kbzon.launcher.preferences.ui.SearchUiSettings
import dev.kbzon.launcher.preferences.ui.UiSettings
import dev.kbzon.launcher.preferences.ui.UiState
import dev.kbzon.launcher.preferences.weather.WeatherSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val preferencesModule = module {
    single { LauncherDataStore(androidContext()) }
    factory { dev.kbzon.launcher.preferences.privacy.PrivacySettings(get()) } // [fork]
    factory { dev.kbzon.launcher.preferences.home.MinimalHomeSettings(get()) } // [fork]
    factory<Backupable>(named<LauncherDataStore>()) { get<LauncherDataStore>() }
    factory { MediaSettings(get()) }
    factory { ContactSearchSettings(get()) }
    factory { FileSearchSettings(get()) }
    factory { UnitConverterSettings(get()) }
    factory { BadgeSettings(get()) }
    factory { UiSettings(get()) }
    factory { ShortcutSearchSettings(get()) }
    factory { FavoritesSettings(get()) }
    factory { WikipediaSearchSettings(get()) }
    factory { IconSettings(get()) }
    factory { RankingSettings(get()) }
    factory { CalendarSearchSettings(get()) }
    factory { WebsiteSearchSettings(get()) }
    factory { UiState(get()) }
    factory { SearchUiSettings(get()) }
    factory { WeatherSettings(get()) }
    factory { GestureSettings(get()) }
    factory { CalculatorSearchSettings(get()) }
    factory { ClockWidgetSettings(get()) }
    factory { LocationSearchSettings(get()) }
    factory { SearchFilterSettings(get()) }
    factory { LocaleSettings(get()) }
    factory { FeedSettings(get()) }
}