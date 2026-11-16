package dev.kbzon.launcher.ui.settings.integrations

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import dev.kbzon.launcher.FeatureFlags
import dev.kbzon.launcher.ktx.isAtLeastApiLevel
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.preferences.Preference
import dev.kbzon.launcher.ui.component.preferences.PreferenceCategory
import dev.kbzon.launcher.ui.component.preferences.PreferenceScreen
import dev.kbzon.launcher.ui.locals.LocalBackStack
import dev.kbzon.launcher.ui.settings.breezyweather.BreezyWeatherSettingsRoute
import dev.kbzon.launcher.ui.settings.feed.FeedIntegrationSettingsRoute
import dev.kbzon.launcher.ui.settings.media.MediaIntegrationSettingsRoute
import dev.kbzon.launcher.ui.settings.smartspacer.SmartspacerSettingsRoute
import dev.kbzon.launcher.ui.settings.tasks.TasksIntegrationSettingsRoute
import dev.kbzon.launcher.ui.settings.weather.WeatherIntegrationSettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object IntegrationsSettingsRoute : NavKey

@Composable
fun IntegrationsSettingsScreen() {
    val viewModel: IntegrationsSettingsScreenVM = viewModel()
    val backStack = LocalBackStack.current

    PreferenceScreen(title = stringResource(R.string.preference_screen_integrations)) {
        item {
            PreferenceCategory {
                Preference(
                    title = stringResource(R.string.preference_weather_integration),
                    icon = R.drawable.light_mode_24px,
                    onClick = {
                        backStack.add(WeatherIntegrationSettingsRoute)
                    }
                )
                Preference(
                    title = stringResource(R.string.preference_media_integration),
                    icon = R.drawable.play_circle_24px,
                    onClick = {
                        backStack.add(MediaIntegrationSettingsRoute)
                    }
                )
                if (FeatureFlags.feed) {
                    Preference(
                        title = stringResource(R.string.preference_feed_integration),
                        icon = R.drawable.news_24px,
                        onClick = {
                            backStack.add(FeedIntegrationSettingsRoute)
                        }
                    )
                }
            }
        }
        item {
            PreferenceCategory {
                Preference(
                    title = stringResource(R.string.preference_tasks_integration),
                    icon = R.drawable.check_24px_sharp,
                    onClick = {
                        backStack.add(TasksIntegrationSettingsRoute)
                    }
                )
                Preference(
                    title = stringResource(R.string.preference_breezyweather_integration),
                    icon = R.drawable.breezy_weather,
                    onClick = {
                        backStack.add(BreezyWeatherSettingsRoute)
                    }
                )
                if (isAtLeastApiLevel(29)) {
                    if (FeatureFlags.smartspacerIntegration) {
                        Preference(
                            title = stringResource(R.string.preference_smartspacer_integration),
                            icon = R.drawable.smartspacer,
                            onClick = {
                                backStack.add(SmartspacerSettingsRoute)
                            }
                        )
                    }
                }
            }
        }
    }
}