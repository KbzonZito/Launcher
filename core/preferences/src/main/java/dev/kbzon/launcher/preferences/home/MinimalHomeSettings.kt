package dev.kbzon.launcher.preferences.home

import dev.kbzon.launcher.preferences.GestureAction
import dev.kbzon.launcher.preferences.LauncherDataStore
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

    val notificationPreviews: Flow<Boolean>
        get() = dataStore.data.map { it.minimalHomeNotificationPreviews }.distinctUntilChanged()

    fun setNotificationPreviews(show: Boolean) = dataStore.update { it.copy(minimalHomeNotificationPreviews = show) }

    /** Bajar en la mitad izquierda = notificaciones, mitad derecha = ajustes rápidos. */
    val splitSwipeDown: Flow<Boolean>
        get() = dataStore.data.map { it.minimalHomeSplitSwipeDown }.distinctUntilChanged()

    fun setSplitSwipeDown(enabled: Boolean) = dataStore.update { it.copy(minimalHomeSplitSwipeDown = enabled) }

    val twoFingerUp: Flow<GestureAction>
        get() = dataStore.data.map { it.minimalHomeTwoFingerUp }.distinctUntilChanged()

    fun setTwoFingerUp(action: GestureAction) = dataStore.update { it.copy(minimalHomeTwoFingerUp = action) }

    val twoFingerDown: Flow<GestureAction>
        get() = dataStore.data.map { it.minimalHomeTwoFingerDown }.distinctUntilChanged()

    fun setTwoFingerDown(action: GestureAction) = dataStore.update { it.copy(minimalHomeTwoFingerDown = action) }

    val clockStyle: Flow<MinimalClockStyle>
        get() = dataStore.data.map { it.minimalHomeClockStyle }.distinctUntilChanged()

    fun setClockStyle(style: MinimalClockStyle) = dataStore.update { it.copy(minimalHomeClockStyle = style) }

    /** Mostrar hasta 3 próximos eventos en vez de solo el siguiente. */
    val agenda: Flow<Boolean>
        get() = dataStore.data.map { it.minimalHomeAgenda }.distinctUntilChanged()

    fun setAgenda(enabled: Boolean) = dataStore.update { it.copy(minimalHomeAgenda = enabled) }

    /** App que abre el clima (clave de búsqueda); null = automático. */
    val weatherAppKey: Flow<String?>
        get() = dataStore.data.map { it.minimalHomeWeatherAppKey }.distinctUntilChanged()

    fun setWeatherAppKey(key: String?) = dataStore.update { it.copy(minimalHomeWeatherAppKey = key) }

    /** App que abre la fecha (clave de búsqueda); null = la de calendario por defecto. */
    val calendarAppKey: Flow<String?>
        get() = dataStore.data.map { it.minimalHomeCalendarAppKey }.distinctUntilChanged()

    fun setCalendarAppKey(key: String?) = dataStore.update { it.copy(minimalHomeCalendarAppKey = key) }

    /** Búsqueda (y paneles) con velo oscuro y filas transparentes, como la home. */
    val searchStyle: Flow<Boolean>
        get() = dataStore.data.map { it.minimalSearchStyle }.distinctUntilChanged()

    fun setSearchStyle(enabled: Boolean) = dataStore.update { it.copy(minimalSearchStyle = enabled) }

    /** Mini reproductor en la home cuando hay música sonando o en pausa. */
    val mediaPlayer: Flow<Boolean>
        get() = dataStore.data.map { it.minimalHomeMediaPlayer }.distinctUntilChanged()

    fun setMediaPlayer(enabled: Boolean) = dataStore.update { it.copy(minimalHomeMediaPlayer = enabled) }

    /** App que abre el reloj (clave de búsqueda); null = la app de reloj del sistema. */
    val clockAppKey: Flow<String?>
        get() = dataStore.data.map { it.minimalHomeClockAppKey }.distinctUntilChanged()

    fun setClockAppKey(key: String?) = dataStore.update { it.copy(minimalHomeClockAppKey = key) }

    /** Sugerir apps según el uso (◷ en el riel y apps frecuentes). */
    val usageSuggestions: Flow<Boolean>
        get() = dataStore.data.map { it.minimalHomeUsageSuggestions }.distinctUntilChanged()

    fun setUsageSuggestions(enabled: Boolean) = dataStore.update {
        // También apaga las "frecuentes" de los favoritos de Kvaesitso, para que no aparezcan en ningún lado.
        it.copy(minimalHomeUsageSuggestions = enabled, favoritesFrequentlyUsed = enabled)
    }

    /** Doble toque en el riel apaga la pantalla. */
    val railDoubleTapLock: Flow<Boolean>
        get() = dataStore.data.map { it.minimalHomeRailDoubleTapLock }.distinctUntilChanged()

    fun setRailDoubleTapLock(enabled: Boolean) = dataStore.update { it.copy(minimalHomeRailDoubleTapLock = enabled) }

    val waveIntensity: Flow<WaveIntensity>
        get() = dataStore.data.map { it.minimalHomeWaveIntensity }.distinctUntilChanged()

    fun setWaveIntensity(value: WaveIntensity) = dataStore.update { it.copy(minimalHomeWaveIntensity = value) }

    /** El riel también responde desde el borde contrario. */
    val railBothEdges: Flow<Boolean>
        get() = dataStore.data.map { it.minimalHomeRailBothEdges }.distinctUntilChanged()

    fun setRailBothEdges(enabled: Boolean) = dataStore.update { it.copy(minimalHomeRailBothEdges = enabled) }

    val appSwipeLeft: Flow<Map<String, String>>
        get() = dataStore.data.map { it.minimalHomeAppSwipeLeft }.distinctUntilChanged()

    fun setAppSwipeLeft(appKey: String, targetKey: String?) = dataStore.update {
        it.copy(
            minimalHomeAppSwipeLeft = if (targetKey == null) it.minimalHomeAppSwipeLeft - appKey
            else it.minimalHomeAppSwipeLeft + (appKey to targetKey)
        )
    }
}
