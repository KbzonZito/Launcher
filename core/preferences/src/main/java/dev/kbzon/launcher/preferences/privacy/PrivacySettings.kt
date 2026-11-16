package dev.kbzon.launcher.preferences.privacy

import dev.kbzon.launcher.preferences.LauncherDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * [fork] Ajustes de privacidad propios del fork.
 */
class PrivacySettings internal constructor(
    private val dataStore: LauncherDataStore,
) {
    /** Si es true, nada de la app debe usar la red. */
    val offlineMode: Flow<Boolean>
        get() = dataStore.data.map { it.privacyOfflineMode }.distinctUntilChanged()

    fun setOfflineMode(enabled: Boolean) {
        dataStore.update { it.copy(privacyOfflineMode = enabled) }
    }

    /** Pedir autenticación antes de mostrar apps ocultas. */
    val protectHiddenItems: Flow<Boolean>
        get() = dataStore.data.map { it.privacyProtectHiddenItems }.distinctUntilChanged()

    fun setProtectHiddenItems(enabled: Boolean) {
        dataStore.update { it.copy(privacyProtectHiddenItems = enabled) }
    }

    /** No registrar aperturas de apps/resultados. */
    val incognito: Flow<Boolean>
        get() = dataStore.data.map { it.privacyIncognito }.distinctUntilChanged()

    fun setIncognito(enabled: Boolean) {
        dataStore.update { it.copy(privacyIncognito = enabled) }
    }

    /** FLAG_SECURE mientras se muestra la búsqueda o las apps ocultas. */
    val secureScreens: Flow<Boolean>
        get() = dataStore.data.map { it.privacySecureScreens }.distinctUntilChanged()

    fun setSecureScreens(enabled: Boolean) {
        dataStore.update { it.copy(privacySecureScreens = enabled) }
    }
}
