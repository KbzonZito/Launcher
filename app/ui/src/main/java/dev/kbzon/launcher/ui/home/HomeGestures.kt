package dev.kbzon.launcher.ui.home

import android.os.SystemClock
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * [fork] Las filas avisan aquí cuando abren su propio menú (mantener pulsado), para que la home
 * no abra además el menú rápido.
 */
object HomeLongPressGuard {
    @Volatile var lastChildLongPress = 0L
    fun markChildLongPress() { lastChildLongPress = SystemClock.uptimeMillis() }
}

/**
 * [fork] Gestos propios de la home minimalista, resueltos antes que el scaffold:
 * - Mantener pulsado un espacio vacío ([onLongPress]).
 * - Bajar con un dedo: mitad izquierda / mitad derecha (si [splitSwipeDown] está activo).
 * - Dos dedos hacia arriba o abajo.
 *
 * Si un hijo (lista, riel, fila) ya consumió el gesto, se ignora. Los deslizamientos hacia
 * arriba y horizontales se dejan pasar para que el scaffold siga manejándolos.
 */
fun Modifier.homeGestures(
    splitSwipeDown: () -> Boolean,
    onSwipeDown: (leftHalf: Boolean) -> Unit,
    onTwoFinger: (up: Boolean) -> Unit,
    onLongPress: (Offset) -> Unit,
): Modifier = pointerInput(Unit) {
    val slop = viewConfiguration.touchSlop
    val swipeDownThreshold = 28.dp.toPx()
    val twoFingerThreshold = 64.dp.toPx()
    // +100 ms: el menú de una fila se dispara antes y avisa a HomeLongPressGuard.
    val longPressMs = viewConfiguration.longPressTimeoutMillis + 100

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val downConsumedByChild = down.isConsumed // p. ej. el riel
        val startX = down.position.x
        var done = false
        var twoFingerStartY: Float? = null
        var dx = 0f
        var dy = 0f
        var abandoned = false

        // Devuelve true si el gesto terminó (soltar, consumido por un hijo o resuelto).
        suspend fun AwaitPointerEventScope.handle(): Boolean {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }
            if (pressed.isEmpty()) return true

            if (pressed.size >= 2 || twoFingerStartY != null) {
                if (pressed.size < 2) {
                    event.changes.forEach { it.consume() }
                    return false
                }
                val avgY = pressed.map { it.position.y }.average().toFloat()
                if (twoFingerStartY == null) twoFingerStartY = avgY
                event.changes.forEach { it.consume() }
                val travel = avgY - twoFingerStartY!!
                if (!done && abs(travel) > twoFingerThreshold) {
                    done = true
                    onTwoFinger(travel < 0)
                }
                return false
            }
            if (done) {
                event.changes.forEach { it.consume() }
                return false
            }
            val change = event.changes.firstOrNull { it.id == down.id } ?: return true
            if (change.isConsumed) { abandoned = true; return true }
            val delta = change.positionChange()
            dx += delta.x
            dy += delta.y
            if (splitSwipeDown() && dy > 0 && dy > 2 * abs(dx)) {
                // Hacia abajo: lo reservamos para que el scaffold no arranque su animación.
                change.consume()
                if (dy > swipeDownThreshold) {
                    done = true
                    onSwipeDown(startX < size.width / 2f)
                }
                return false
            }
            if (abs(dx) > slop || abs(dy) > slop) { abandoned = true; return true }
            return false
        }

        // Fase 1: hasta el tiempo de "mantener pulsado".
        val finishedEarly = withTimeoutOrNull(longPressMs) {
            while (true) { if (handle()) return@withTimeoutOrNull true }
            @Suppress("UNREACHABLE_CODE") true
        }
        if (finishedEarly == null) {
            // Se cumplió el tiempo con el dedo quieto sobre un espacio vacío.
            val stillAndFree = !done && twoFingerStartY == null && !abandoned && !downConsumedByChild &&
                    abs(dx) <= slop && abs(dy) <= slop &&
                    SystemClock.uptimeMillis() - HomeLongPressGuard.lastChildLongPress > 600
            if (stillAndFree) {
                onLongPress(down.position)
                // Consumir el resto para que nada más reaccione.
                while (true) {
                    val ev = awaitPointerEvent()
                    ev.changes.forEach { it.consume() }
                    if (ev.changes.none { it.pressed }) break
                }
                return@awaitEachGesture
            }
            // Fase 2: seguir con los demás gestos.
            while (!handle()) { /* continuar */ }
        }
    }
}
