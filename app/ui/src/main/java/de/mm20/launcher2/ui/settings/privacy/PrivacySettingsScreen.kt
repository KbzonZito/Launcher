package de.mm20.launcher2.ui.settings.privacy

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.preferences.Preference
import de.mm20.launcher2.ui.component.preferences.PreferenceCategory
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import de.mm20.launcher2.ui.component.preferences.SwitchPreference
import kotlinx.serialization.Serializable

// [fork] Pantalla de privacidad propia del fork.
@Serializable
data object PrivacySettingsRoute : NavKey

@Composable
fun PrivacySettingsScreen() {
    val viewModel: PrivacySettingsScreenVM = viewModel()
    val offlineMode by viewModel.offlineMode.collectAsStateWithLifecycle(false)

    PreferenceScreen(title = stringResource(R.string.fork_preference_screen_privacy)) {
        item {
            PreferenceCategory {
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_offline_mode),
                    summary = stringResource(R.string.fork_preference_offline_mode_summary),
                    icon = R.drawable.cloud_off_24px,
                    value = offlineMode,
                    onValueChanged = viewModel::setOfflineMode,
                )
                if (offlineMode) {
                    Preference(
                        title = {
                            Text(
                                stringResource(R.string.fork_preference_offline_mode_info),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }
            }
        }
    }
}
