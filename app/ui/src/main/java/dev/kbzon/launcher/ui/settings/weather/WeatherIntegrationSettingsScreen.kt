package dev.kbzon.launcher.ui.settings.weather

import android.app.PendingIntent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import dev.kbzon.launcher.crashreporter.CrashReporter
import dev.kbzon.launcher.ktx.sendWithBackgroundPermission
import dev.kbzon.launcher.ktx.tryStartActivity
import dev.kbzon.launcher.ui.BuildConfig
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.common.WeatherLocationSearchDialog
import dev.kbzon.launcher.ui.component.Banner
import dev.kbzon.launcher.ui.component.preferences.GuardedPreference
import dev.kbzon.launcher.ui.component.preferences.ListPreference
import dev.kbzon.launcher.ui.component.preferences.Preference
import dev.kbzon.launcher.ui.component.preferences.PreferenceCategory
import dev.kbzon.launcher.ui.component.preferences.PreferenceScreen
import dev.kbzon.launcher.ui.component.preferences.SwitchPreference
import dev.kbzon.launcher.ui.locals.LocalBackStack
import dev.kbzon.launcher.ui.settings.locale.LocaleSettingsRoute
import dev.kbzon.launcher.weather.breezy.BreezyWeatherProvider
import kotlinx.serialization.Serializable

@Serializable
data object WeatherIntegrationSettingsRoute: NavKey

@Composable
fun WeatherIntegrationSettingsScreen() {
    val viewModel: WeatherIntegrationSettingsScreenVM = viewModel()
    val context = LocalContext.current
    val backStack = LocalBackStack.current

    val availableProviders by viewModel.availableProviders.collectAsState(emptyList())
    val weatherProvider by viewModel.weatherProvider.collectAsState()

    val selectedProviderInfo by remember {
        derivedStateOf { availableProviders.find { it.id == weatherProvider } }
    }


    PreferenceScreen(
        title = stringResource(R.string.preference_screen_weatherwidget),
    ) {
        item {
            PreferenceCategory {
                ListPreference(
                    title = stringResource(R.string.preference_weather_provider),
                    items = availableProviders.map {
                        it.name to it.id
                    },
                    onValueChanged = {
                        if (it != null) viewModel.setWeatherProvider(it)
                    },
                    value = weatherProvider
                )
            }

        }
        item {
            PreferenceCategory(title = stringResource(R.string.preference_category_location)) {
                if (selectedProviderInfo?.id == BreezyWeatherProvider.Id) {
                    Preference(
                        title = stringResource(R.string.preference_location),
                        summary = stringResource(R.string.preference_location_breezy),
                        onClick = {
                            val intent =
                                context.packageManager.getLaunchIntentForPackage("org.breezyweather")
                                    ?: return@Preference
                            context.tryStartActivity(intent)
                        }
                    )
                } else if (selectedProviderInfo?.managedLocation == true) {
                    Preference(
                        title = stringResource(R.string.preference_location_managed),
                        summary = stringResource(R.string.preference_location_managed_summary),
                        enabled = false
                    )
                } else {
                    val hasPermission by viewModel.hasLocationPermission.collectAsState()
                    val autoLocation by viewModel.autoLocation.collectAsState()
                    GuardedPreference(
                        locked = hasPermission == false,
                        description = stringResource(R.string.missing_permission_auto_location),
                        onUnlock = {
                            viewModel.requestLocationPermission(context as AppCompatActivity)
                        }
                    ) {
                        SwitchPreference(
                            title = stringResource(R.string.preference_automatic_location),
                            summary = stringResource(R.string.preference_automatic_location_summary),
                            value = autoLocation,
                            onValueChanged = {
                                viewModel.setAutoLocation(it)
                            }
                        )
                    }
                    val location by viewModel.location.collectAsStateWithLifecycle()
                    LocationPreference(
                        title = stringResource(R.string.preference_location),
                        value = location,
                        enabled = !autoLocation,
                    )
                }
            }
        }
        item {
            PreferenceCategory {
                Preference(
                    title = stringResource(R.string.preference_measurement_system),
                    icon = R.drawable.open_in_new_24px,
                    onClick = {
                        backStack.add(LocaleSettingsRoute)
                    }
                )
            }
        }
        if (BuildConfig.DEBUG) {
            item {
                PreferenceCategory(stringResource(R.string.preference_category_debug)) {
                    Preference(
                        "Clear weather data",
                        summary = "Remove weather data from database",
                        onClick = {
                            viewModel.clearWeatherData()
                        })
                }
            }
        }
    }
}

@Composable
fun LocationPreference(
    title: String,
    value: String?,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    Preference(
        title = title,
        summary = value,
        enabled = enabled,
        onClick = {
            showDialog = true
        }
    )
    WeatherLocationSearchDialog(
        expanded = showDialog,
        onDismissRequest = { showDialog = false }
    )
}