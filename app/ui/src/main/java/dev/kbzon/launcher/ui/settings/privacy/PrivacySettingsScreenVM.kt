package dev.kbzon.launcher.ui.settings.privacy

import androidx.lifecycle.ViewModel
import dev.kbzon.launcher.preferences.privacy.PrivacySettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// [fork]
class PrivacySettingsScreenVM : ViewModel(), KoinComponent {
    private val privacySettings: PrivacySettings by inject()
    private val favoritesService: dev.kbzon.launcher.services.favorites.FavoritesService by inject()

    fun resetUsageStats() = favoritesService.resetUsageStats()

    val offlineMode = privacySettings.offlineMode

    fun setOfflineMode(enabled: Boolean) = privacySettings.setOfflineMode(enabled)

    val protectHiddenItems = privacySettings.protectHiddenItems
    fun setProtectHiddenItems(enabled: Boolean) = privacySettings.setProtectHiddenItems(enabled)

    val incognito = privacySettings.incognito
    fun setIncognito(enabled: Boolean) = privacySettings.setIncognito(enabled)

    val secureScreens = privacySettings.secureScreens
    fun setSecureScreens(enabled: Boolean) = privacySettings.setSecureScreens(enabled)
}
