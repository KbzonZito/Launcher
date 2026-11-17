package dev.kbzon.launcher.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.app.GrammaticalInflectionManagerCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.kbzon.launcher.ui.base.BaseActivity
import dev.kbzon.launcher.ui.base.ProvideCompositionLocals
import dev.kbzon.launcher.ui.locals.LocalBackStack
import dev.kbzon.launcher.ui.locals.LocalDarkTheme
import dev.kbzon.launcher.ui.locals.LocalWallpaperColors
import dev.kbzon.launcher.ui.overlays.OverlayHost
import dev.kbzon.launcher.ui.settings.about.AboutSettingsRoute
import dev.kbzon.launcher.ui.settings.about.AboutSettingsScreen
import dev.kbzon.launcher.ui.settings.appearance.AppearanceSettingsRoute
import dev.kbzon.launcher.ui.settings.appearance.AppearanceSettingsScreen
import dev.kbzon.launcher.ui.settings.appearance.ExportThemeSettingsRoute
import dev.kbzon.launcher.ui.settings.appearance.ExportThemeSettingsScreen
import dev.kbzon.launcher.ui.settings.appearance.ImportThemeSettingsRoute
import dev.kbzon.launcher.ui.settings.appearance.ImportThemeSettingsScreen
import dev.kbzon.launcher.ui.settings.apps.AppSearchSettingsRoute
import dev.kbzon.launcher.ui.settings.apps.AppSearchSettingsScreen
import dev.kbzon.launcher.ui.settings.appshortcuts.AppShortcutsSettingsRoute
import dev.kbzon.launcher.ui.settings.appshortcuts.AppShortcutsSettingsScreen
import dev.kbzon.launcher.ui.settings.backup.BackupSettingsRoute
import dev.kbzon.launcher.ui.settings.backup.BackupSettingsScreen
import dev.kbzon.launcher.ui.settings.breezyweather.BreezyWeatherSettingsRoute
import dev.kbzon.launcher.ui.settings.breezyweather.BreezyWeatherSettingsScreen
import dev.kbzon.launcher.ui.settings.buildinfo.BuildInfoSettingsRoute
import dev.kbzon.launcher.ui.settings.buildinfo.BuildInfoSettingsScreen
import dev.kbzon.launcher.ui.settings.calendarsearch.CalendarProviderSettingsRoute
import dev.kbzon.launcher.ui.settings.calendarsearch.CalendarProviderSettingsScreen
import dev.kbzon.launcher.ui.settings.calendarsearch.CalendarSearchSettingsRoute
import dev.kbzon.launcher.ui.settings.calendarsearch.CalendarSearchSettingsScreen
import dev.kbzon.launcher.ui.settings.colorscheme.ColorSchemeSettingsRoute
import dev.kbzon.launcher.ui.settings.colorscheme.ColorSchemeSettingsScreen
import dev.kbzon.launcher.ui.settings.colorscheme.ColorSchemesSettingsRoute
import dev.kbzon.launcher.ui.settings.colorscheme.ColorSchemesSettingsScreen
import dev.kbzon.launcher.ui.settings.contacts.ContactsSettingsRoute
import dev.kbzon.launcher.ui.settings.contacts.ContactsSettingsScreen
import dev.kbzon.launcher.ui.settings.crashreporter.CrashReportRoute
import dev.kbzon.launcher.ui.settings.crashreporter.CrashReportScreen
import dev.kbzon.launcher.ui.settings.crashreporter.CrashReporterRoute
import dev.kbzon.launcher.ui.settings.crashreporter.CrashReporterScreen
import dev.kbzon.launcher.ui.settings.debug.DebugSettingsRoute
import dev.kbzon.launcher.ui.settings.debug.DebugSettingsScreen
import dev.kbzon.launcher.ui.settings.debug.StringNormalizerTestRoute
import dev.kbzon.launcher.ui.settings.debug.StringNormalizerTestScreen
import dev.kbzon.launcher.ui.settings.easteregg.EasterEggSettingsRoute
import dev.kbzon.launcher.ui.settings.easteregg.EasterEggSettingsScreen
import dev.kbzon.launcher.ui.settings.favorites.FavoritesSettingsRoute
import dev.kbzon.launcher.ui.settings.favorites.FavoritesSettingsScreen
import dev.kbzon.launcher.ui.settings.feed.FeedIntegrationSettingsRoute
import dev.kbzon.launcher.ui.settings.feed.FeedIntegrationSettingsScreen
import dev.kbzon.launcher.ui.settings.filesearch.FileSearchSettingsRoute
import dev.kbzon.launcher.ui.settings.filesearch.FileSearchSettingsScreen
import dev.kbzon.launcher.ui.settings.filterbar.FilterBarSettingsRoute
import dev.kbzon.launcher.ui.settings.filterbar.FilterBarSettingsScreen
import dev.kbzon.launcher.ui.settings.gestures.GestureSettingsScreen
import dev.kbzon.launcher.ui.settings.gestures.GesturesSettingsRoute
import dev.kbzon.launcher.ui.settings.hiddenitems.HiddenItemsSettingsRoute
import dev.kbzon.launcher.ui.settings.hiddenitems.HiddenItemsSettingsScreen
import dev.kbzon.launcher.ui.settings.homescreen.HomescreenSettingsRoute
import dev.kbzon.launcher.ui.settings.homescreen.HomescreenSettingsScreen
import dev.kbzon.launcher.ui.settings.icons.IconsSettingsRoute
import dev.kbzon.launcher.ui.settings.icons.IconsSettingsScreen
import dev.kbzon.launcher.ui.settings.integrations.IntegrationsSettingsRoute
import dev.kbzon.launcher.ui.settings.integrations.IntegrationsSettingsScreen
import dev.kbzon.launcher.ui.settings.license.LicenseRoute
import dev.kbzon.launcher.ui.settings.license.LicenseScreen
import dev.kbzon.launcher.ui.settings.locale.CalendarSettingsRoute
import dev.kbzon.launcher.ui.settings.locale.CalendarSettingsScreen
import dev.kbzon.launcher.ui.settings.locale.CurrencySettingsRoute
import dev.kbzon.launcher.ui.settings.locale.CurrencySettingsScreen
import dev.kbzon.launcher.ui.settings.locale.LocaleSettingsRoute
import dev.kbzon.launcher.ui.settings.locale.LocaleSettingsScreen
import dev.kbzon.launcher.ui.settings.locale.MeasurementSystemSettingsRoute
import dev.kbzon.launcher.ui.settings.locale.MeasurementSystemSettingsScreen
import dev.kbzon.launcher.ui.settings.locations.LocationsSettingsRoute
import dev.kbzon.launcher.ui.settings.locations.LocationsSettingsScreen
import dev.kbzon.launcher.ui.settings.log.LogRoute
import dev.kbzon.launcher.ui.settings.log.LogScreen
import dev.kbzon.launcher.ui.settings.main.MainRoute
import dev.kbzon.launcher.ui.settings.main.MainSettingsScreen
import dev.kbzon.launcher.ui.settings.media.MediaIntegrationSettingsRoute
import dev.kbzon.launcher.ui.settings.media.MediaIntegrationSettingsScreen
import dev.kbzon.launcher.ui.settings.osm.OsmSettingsRoute
import dev.kbzon.launcher.ui.settings.osm.OsmSettingsScreen
import dev.kbzon.launcher.ui.settings.search.SearchSettingsRoute
import dev.kbzon.launcher.ui.settings.search.SearchSettingsScreen
import dev.kbzon.launcher.ui.settings.searchactions.SearchActionsSettingsRoute
import dev.kbzon.launcher.ui.settings.searchactions.SearchActionsSettingsScreen
import dev.kbzon.launcher.ui.settings.shapes.ShapeSchemeSettingsRoute
import dev.kbzon.launcher.ui.settings.shapes.ShapeSchemeSettingsScreen
import dev.kbzon.launcher.ui.settings.shapes.ShapeSchemesSettingsRoute
import dev.kbzon.launcher.ui.settings.shapes.ShapeSchemesSettingsScreen
import dev.kbzon.launcher.ui.settings.smartspacer.SmartspacerSettingsRoute
import dev.kbzon.launcher.ui.settings.smartspacer.SmartspacerSettingsScreen
import dev.kbzon.launcher.ui.settings.tags.TagsSettingsRoute
import dev.kbzon.launcher.ui.settings.tags.TagsSettingsScreen
import dev.kbzon.launcher.ui.settings.tasks.TasksIntegrationSettingsRoute
import dev.kbzon.launcher.ui.settings.tasks.TasksIntegrationSettingsScreen
import dev.kbzon.launcher.ui.settings.transparencies.TransparencySchemeSettingsRoute
import dev.kbzon.launcher.ui.settings.transparencies.TransparencySchemeSettingsScreen
import dev.kbzon.launcher.ui.settings.transparencies.TransparencySchemesSettingsRoute
import dev.kbzon.launcher.ui.settings.transparencies.TransparencySchemesSettingsScreen
import dev.kbzon.launcher.ui.settings.typography.TypographiesSettingsRoute
import dev.kbzon.launcher.ui.settings.typography.TypographiesSettingsScreen
import dev.kbzon.launcher.ui.settings.typography.TypographySettingsRoute
import dev.kbzon.launcher.ui.settings.typography.TypographySettingsScreen
import dev.kbzon.launcher.ui.settings.unitconverter.UnitConverterHelpSettingsRoute
import dev.kbzon.launcher.ui.settings.unitconverter.UnitConverterHelpSettingsScreen
import dev.kbzon.launcher.ui.settings.unitconverter.UnitConverterSettingsRoute
import dev.kbzon.launcher.ui.settings.unitconverter.UnitConverterSettingsScreen
import dev.kbzon.launcher.ui.settings.weather.WeatherIntegrationSettingsRoute
import dev.kbzon.launcher.ui.settings.weather.WeatherIntegrationSettingsScreen
import dev.kbzon.launcher.ui.theme.LauncherTheme
import dev.kbzon.launcher.ui.theme.wallpaperColorsAsState

class SettingsActivity : BaseActivity() {

    private var initialRoute by mutableStateOf<NavKey?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)

        val newRoute = getStartRoute(intent)
        initialRoute = newRoute

        val entryProvider = entryProvider {
            entry<MainRoute> {
                MainSettingsScreen()
            }
            entry<dev.kbzon.launcher.ui.settings.home.MinimalHomeSettingsRoute> { // [fork]
                dev.kbzon.launcher.ui.settings.home.MinimalHomeSettingsScreen()
            }
            entry<dev.kbzon.launcher.ui.settings.home.RailListSettingsRoute> { // [fork] Fase 15
                dev.kbzon.launcher.ui.settings.home.MinimalHomeSettingsScreen(dev.kbzon.launcher.ui.settings.home.HomeSettingsSection.RailList)
            }
            entry<dev.kbzon.launcher.ui.settings.home.MinimalSearchSettingsRoute> { // [fork] Fase 15
                dev.kbzon.launcher.ui.settings.home.MinimalHomeSettingsScreen(dev.kbzon.launcher.ui.settings.home.HomeSettingsSection.Search)
            }
            entry<dev.kbzon.launcher.ui.settings.privacy.PrivacySettingsRoute> { // [fork]
                dev.kbzon.launcher.ui.settings.privacy.PrivacySettingsScreen()
            }
            entry<AppearanceSettingsRoute> {
                AppearanceSettingsScreen()
            }
            entry<ExportThemeSettingsRoute> {
                ExportThemeSettingsScreen()
            }
            entry<ImportThemeSettingsRoute> {
                ImportThemeSettingsScreen(it.fromUri)
            }
            entry<HomescreenSettingsRoute> {
                HomescreenSettingsScreen()
            }
            entry<IconsSettingsRoute> {
                IconsSettingsScreen()
            }
            entry<ColorSchemesSettingsRoute> {
                ColorSchemesSettingsScreen()
            }
            entry<ColorSchemeSettingsRoute> {
                ColorSchemeSettingsScreen(it.id)
            }
            entry<ShapeSchemesSettingsRoute> {
                ShapeSchemesSettingsScreen()
            }
            entry<ShapeSchemeSettingsRoute> {
                ShapeSchemeSettingsScreen(it.id)
            }

            entry<TransparencySchemesSettingsRoute> {
                TransparencySchemesSettingsScreen()
            }
            entry<TransparencySchemeSettingsRoute> {
                TransparencySchemeSettingsScreen(it.id)
            }
            entry<TypographiesSettingsRoute> {
                TypographiesSettingsScreen()
            }
            entry<TypographySettingsRoute> {
                TypographySettingsScreen(it.id)
            }
            entry<SearchSettingsRoute> {
                SearchSettingsScreen()
            }
            entry<GesturesSettingsRoute> {
                GestureSettingsScreen()
            }
            entry<UnitConverterSettingsRoute> {
                UnitConverterSettingsScreen()
            }
            entry<UnitConverterHelpSettingsRoute> {
                UnitConverterHelpSettingsScreen()
            }
            entry<LocationsSettingsRoute> {
                LocationsSettingsScreen()
            }
            entry<OsmSettingsRoute> {
                OsmSettingsScreen()
            }
            entry<FileSearchSettingsRoute> {
                FileSearchSettingsScreen()
            }
            entry<CalendarSearchSettingsRoute> {
                CalendarSearchSettingsScreen()
            }
            entry<CalendarProviderSettingsRoute> {
                CalendarProviderSettingsScreen(it.providerId)
            }
            entry<SearchActionsSettingsRoute> {
                SearchActionsSettingsScreen()
            }
            entry<HiddenItemsSettingsRoute> {
                val hiddenItemsBackStack = LocalBackStack.current // [fork]
                dev.kbzon.launcher.ui.privacy.RequireHiddenItemsUnlock(onDenied = { hiddenItemsBackStack.removeLastOrNull() }) {
                    HiddenItemsSettingsScreen()
                }
            }
            entry<TagsSettingsRoute> {
                TagsSettingsScreen()
            }
            entry<FilterBarSettingsRoute> {
                FilterBarSettingsScreen()
            }
            entry<WeatherIntegrationSettingsRoute> {
                WeatherIntegrationSettingsScreen()
            }
            entry<MediaIntegrationSettingsRoute> {
                MediaIntegrationSettingsScreen()
            }
            entry<FavoritesSettingsRoute> {
                FavoritesSettingsScreen()
            }
            entry<AppShortcutsSettingsRoute> {
                AppShortcutsSettingsScreen()
            }
            entry<ContactsSettingsRoute> {
                ContactsSettingsScreen()
            }
            entry<IntegrationsSettingsRoute> {
                IntegrationsSettingsScreen()
            }
            entry<TasksIntegrationSettingsRoute> {
                TasksIntegrationSettingsScreen()
            }
            entry<BreezyWeatherSettingsRoute> {
                BreezyWeatherSettingsScreen()
            }
            entry<AboutSettingsRoute> {
                AboutSettingsScreen()
            }
            entry<BuildInfoSettingsRoute> {
                BuildInfoSettingsScreen()
            }
            entry<EasterEggSettingsRoute> {
                EasterEggSettingsScreen()
            }
            entry<DebugSettingsRoute> {
                DebugSettingsScreen()
            }
            entry<StringNormalizerTestRoute> {
                StringNormalizerTestScreen()
            }
            entry<LocaleSettingsRoute> {
                LocaleSettingsScreen()
            }
            entry<CalendarSettingsRoute> {
                CalendarSettingsScreen()
            }
            entry<CurrencySettingsRoute> {
                CurrencySettingsScreen()
            }
            entry<MeasurementSystemSettingsRoute> {
                MeasurementSystemSettingsScreen()
            }
            entry<BackupSettingsRoute> {
                BackupSettingsScreen()
            }
            entry<CrashReporterRoute> {
                CrashReporterScreen()
            }
            entry<LogRoute> {
                LogScreen()
            }
            entry<CrashReportRoute> {
                CrashReportScreen(it.fileName)
            }
            entry<LicenseRoute> {
                LicenseScreen(it.libraryName)
            }
            entry<AppSearchSettingsRoute> {
                AppSearchSettingsScreen()
            }
            entry<SmartspacerSettingsRoute> {
                SmartspacerSettingsScreen()
            }
            entry<FeedIntegrationSettingsRoute> {
                FeedIntegrationSettingsScreen()
            }
        }


        setContent {
            val backStack = rememberNavBackStack(MainRoute)

            LaunchedEffect(initialRoute) {
                if (initialRoute != null) {
                    backStack.clear()
                    backStack.add(initialRoute!!)
                }
            }

            val wallpaperColors by wallpaperColorsAsState()
            CompositionLocalProvider(
                LocalWallpaperColors provides wallpaperColors,
                LocalBackStack provides backStack,
            ) {
                ProvideCompositionLocals {
                    LauncherTheme {
                        val isDarkTheme = LocalDarkTheme.current
                        val lifecycleOwner = LocalLifecycleOwner.current
                        val activity = LocalActivity.current
                        val view = LocalView.current
                        LaunchedEffect(isDarkTheme) {
                            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                                val insetsController = WindowInsetsControllerCompat(activity!!.window, view)
                                insetsController.isAppearanceLightStatusBars = !isDarkTheme
                                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
                            }
                        }

                        OverlayHost(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            NavDisplay(
                                backStack = backStack,
                                onBack = { backStack.removeLastOrNull() },
                                entryProvider = entryProvider,
                                transitionSpec = {
                                    (slideInHorizontally { it / 2 } + scaleIn(initialScale = 0.9f) + fadeIn())
                                        .togetherWith(
                                            slideOutHorizontally { -it / 4 }
                                        )
                                },
                                popTransitionSpec = {
                                    (slideInHorizontally { -it / 4 })
                                        .togetherWith(
                                            slideOutHorizontally { it / 2 } + scaleOut(targetScale = 0.9f) + fadeOut()
                                        )
                                },
                                predictivePopTransitionSpec = {
                                    (slideInHorizontally { -it / 4 })
                                        .togetherWith(
                                            slideOutHorizontally { it / 2 } + scaleOut(targetScale = 0.9f) + fadeOut()
                                        )
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val newRoute = getStartRoute(intent)
        initialRoute = newRoute
    }

    private fun getStartRoute(intent: Intent): NavKey? {
        val routeName = if (intent.data?.host == "kvaesitso.mm20.de") {
            intent.data?.getQueryParameter("route") ?: return null
        } else {
            intent.getStringExtra(EXTRA_ROUTE) ?: return null
        }
        return when(routeName) {
            ROUTE_WEATHER_INTEGRATION -> WeatherIntegrationSettingsRoute
            ROUTE_MEDIA_INTEGRATION -> MediaIntegrationSettingsRoute
            ROUTE_SEARCH_ACTIONS -> SearchActionsSettingsRoute
            ROUTE_HIDDEN_ITEMS -> HiddenItemsSettingsRoute
            ROUTE_GESTURES -> dev.kbzon.launcher.ui.settings.gestures.GesturesSettingsRoute // [fork]
            ROUTE_MINIMAL_HOME -> dev.kbzon.launcher.ui.settings.home.MinimalHomeSettingsRoute // [fork]
            ROUTE_CRASH_REPORT if (intent.hasExtra(EXTRA_CRASH_REPORT_PATH)) -> {
                CrashReportRoute(intent.getStringExtra(EXTRA_CRASH_REPORT_PATH)!!)
            }
            else -> null
        }
    }

    companion object {
        const val EXTRA_ROUTE = "dev.kbzon.launcher.settings.ROUTE"
        const val ROUTE_WEATHER_INTEGRATION = "settings/integrations/weather"
        const val ROUTE_MEDIA_INTEGRATION = "settings/integrations/media"
        const val ROUTE_SEARCH_ACTIONS = "settings/search/searchactions"
        const val ROUTE_HIDDEN_ITEMS = "settings/search/hiddenitems"
        const val ROUTE_CRASH_REPORT = "settings/debug/crashreport"
        const val ROUTE_GESTURES = "settings/gestures" // [fork]
        const val ROUTE_MINIMAL_HOME = "settings/home" // [fork]
        const val EXTRA_CRASH_REPORT_PATH = "crash_report_path"
    }
}
