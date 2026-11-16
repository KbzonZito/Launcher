package de.mm20.launcher2.ui.settings.privacy

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import de.mm20.launcher2.ui.privacy.authenticateUser
import kotlinx.coroutines.launch
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
    val protectHiddenItems by viewModel.protectHiddenItems.collectAsStateWithLifecycle(false)
    val incognito by viewModel.incognito.collectAsStateWithLifecycle(false)
    val secureScreens by viewModel.secureScreens.collectAsStateWithLifecycle(true)
    val activity = LocalContext.current as FragmentActivity
    val scope = rememberCoroutineScope()

    PreferenceScreen(title = stringResource(R.string.fork_preference_screen_privacy)) {
        item {
            PreferenceCategory(title = stringResource(R.string.fork_preference_category_on_device)) {
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_protect_hidden_items),
                    summary = stringResource(R.string.fork_preference_protect_hidden_items_summary),
                    icon = R.drawable.lock_24px,
                    value = protectHiddenItems,
                    onValueChanged = { enabled ->
                        // Desactivar la protección también exige autenticarse.
                        if (enabled) viewModel.setProtectHiddenItems(true)
                        else scope.launch { if (activity.authenticateUser()) viewModel.setProtectHiddenItems(false) }
                    },
                )
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_incognito),
                    summary = stringResource(R.string.fork_preference_incognito_summary),
                    icon = R.drawable.visibility_off_24px,
                    value = incognito,
                    onValueChanged = viewModel::setIncognito,
                )
                SwitchPreference(
                    title = stringResource(R.string.fork_preference_secure_screens),
                    summary = stringResource(R.string.fork_preference_secure_screens_summary),
                    icon = R.drawable.mobile_24px,
                    value = secureScreens,
                    onValueChanged = viewModel::setSecureScreens,
                )
            }
        }
        item {
            PreferenceCategory(title = stringResource(R.string.fork_preference_category_network)) {
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
