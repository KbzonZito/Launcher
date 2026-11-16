package de.mm20.launcher2.ui.home

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * [fork] Gestos propios de la home minimalista, resueltos antes que el scaffold:
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
): Modifier = pointerInput(Unit) {
    val slop = viewConfiguration.touchSlop
    val swipeDownThreshold = 28.dp.toPx()
    val twoFingerThreshold = 64.dp.toPx()

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val startX = down.position.x
        var done = false
        var twoFingerStartY: Float? = null
        var dx = 0f
        var dy = 0f

        while (true) {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }
            if (pressed.isEmpty()) break

            // Dos dedos: se toma el gesto completo.
            if (pressed.size >= 2 || twoFingerStartY != null) {
                if (pressed.size < 2) {
                    event.changes.forEach { it.consume() }
                    continue
                }
                val avgY = pressed.map { it.position.y }.average().toFloat()
                if (twoFingerStartY == null) twoFingerStartY = avgY
                event.changes.forEach { it.consume() }
                val travel = avgY - twoFingerStartY
                if (!done && abs(travel) > twoFingerThreshold) {
                    done = true
                    onTwoFinger(travel < 0)
                }
                continue
            }
            if (done) {
                event.changes.forEach { it.consume() }
                continue
            }

            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (change.isConsumed) break // lo maneja un hijo (lista, riel, fila)
            val delta = change.positionChange()
            dx += delta.x
            dy += delta.y

            if (!splitSwipeDown()) break
            if (dy > 0 && dy > 2 * abs(dx)) {
                // Hacia abajo: lo reservamos para que el scaffold no arranque su animación.
                change.consume()
                if (dy > swipeDownThreshold) {
                    done = true
                    onSwipeDown(startX < size.width / 2f)
                }
            } else if (abs(dx) > slop || dy < -slop) {
                break // arriba u horizontal: para el scaffold
            }
        }
    }
}
