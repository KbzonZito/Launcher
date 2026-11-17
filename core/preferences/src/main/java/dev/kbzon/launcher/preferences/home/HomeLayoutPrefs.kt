package dev.kbzon.launcher.preferences.home

import dev.kbzon.launcher.preferences.GestureAction
import kotlinx.serialization.Serializable

/**
 * [fork] Acciones del botón de acción de la home (abajo, del lado del riel).
 */
@Serializable
data class ActionButtonActions(
    val tap: GestureAction = GestureAction.Search,
    val doubleTap: GestureAction = GestureAction.ScreenLock,
    val longPress: GestureAction = GestureAction.QuickSettings,
    val swipeUp: GestureAction = GestureAction.Recents,
    val swipeDown: GestureAction = GestureAction.Notifications,
    val swipeLeft: GestureAction = GestureAction.NoAction,
    val swipeRight: GestureAction = GestureAction.NoAction,
) {
    val all: List<GestureAction> get() = listOf(tap, doubleTap, longPress, swipeUp, swipeDown, swipeLeft, swipeRight)
}

/**
 * [fork] Posición de los bloques de la home, en fracciones del alto disponible.
 * @param clockOffset desplazamiento vertical del bloque reloj + vistazo (-0.3 … 0.6)
 * @param railCenter centro vertical del riel (0 = arriba, 1 = abajo)
 * @param railHeight alto máximo del riel (0.3 … 0.9)
 */
@Serializable
data class HomeLayout(
    val clockOffset: Float = 0f,
    val railCenter: Float = 0.5f,
    val railHeight: Float = 0.62f,
    /** cuánto sube el botón de acción desde su posición base (0 … 0.5 del alto) */
    val buttonLift: Float = 0f,
)

/** [fork] Posición horizontal del botón de acción. */
@Serializable
enum class ActionButtonPosition { RailSide, OppositeSide, Center }
