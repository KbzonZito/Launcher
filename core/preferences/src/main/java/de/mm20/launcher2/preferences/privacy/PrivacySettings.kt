package de.mm20.launcher2.preferences.privacy

import de.mm20.launcher2.preferences.LauncherDataStore
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
}
