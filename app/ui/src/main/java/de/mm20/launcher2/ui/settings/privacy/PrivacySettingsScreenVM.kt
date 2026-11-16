package de.mm20.launcher2.ui.settings.privacy

import androidx.lifecycle.ViewModel
import de.mm20.launcher2.preferences.privacy.PrivacySettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// [fork]
class PrivacySettingsScreenVM : ViewModel(), KoinComponent {
    private val privacySettings: PrivacySettings by inject()

    val offlineMode = privacySettings.offlineMode

    fun setOfflineMode(enabled: Boolean) = privacySettings.setOfflineMode(enabled)
}
