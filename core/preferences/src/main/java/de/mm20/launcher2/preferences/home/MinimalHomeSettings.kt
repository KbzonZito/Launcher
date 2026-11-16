package de.mm20.launcher2.preferences.home

import de.mm20.launcher2.preferences.LauncherDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * [fork] Ajustes de la home minimalista (lista + riel del alfabeto).
 */
class MinimalHomeSettings internal constructor(
    private val dataStore: LauncherDataStore,
) {
    val enabled: Flow<Boolean>
        get() = dataStore.data.map { it.minimalHomeEnabled }.distinctUntilChanged()

    fun setEnabled(enabled: Boolean) = dataStore.update { it.copy(minimalHomeEnabled = enabled) }

    val showIcons: Flow<Boolean>
        get() = dataStore.data.map { it.minimalHomeShowIcons }.distinctUntilChanged()

    fun setShowIcons(show: Boolean) = dataStore.update { it.copy(minimalHomeShowIcons = show) }

    val railOnLeft: Flow<Boolean>
        get() = dataStore.data.map { it.minimalHomeRailOnLeft }.distinctUntilChanged()

    fun setRailOnLeft(left: Boolean) = dataStore.update { it.copy(minimalHomeRailOnLeft = left) }
}
