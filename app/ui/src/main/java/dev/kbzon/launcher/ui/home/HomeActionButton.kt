package dev.kbzon.launcher.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.preferences.GestureAction
import dev.kbzon.launcher.preferences.home.ActionButtonActions
import dev.kbzon.launcher.ui.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

private enum class Dir { Up, Down, Left, Right }

/**
 * [fork] Botón de acción de la home: toque, doble toque, mantener y deslizar en 4 direcciones,
 * cada gesto con su acción. Al arrastrar, el botón sigue al dedo con resistencia y muestra una
 * flecha hacia la dirección elegida; vibra al cruzar el umbral.
 */
@Composable
fun HomeActionButton(
    actions: ActionButtonActions,
    onAction: (GestureAction) -> Unit,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val currentActions by rememberUpdatedState(actions)
    val currentOnAction by rememberUpdatedState(onAction)
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var armed by remember { mutableStateOf<Dir?>(null) }
    var pressed by remember { mutableStateOf(false) }
    val threshold = with(density) { 44.dp.toPx() }
    val maxTravel = with(density) { 28.dp.toPx() }
    val color = LocalContentColor.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
            .graphicsLayer {
                val s = if (pressed) 0.92f else 1f
                scaleX = s; scaleY = s
            }
            .size(56.dp)
            .background(if (armed != null) accent.copy(alpha = 0.9f) else color.copy(alpha = 0.10f), CircleShape)
            .border(1.dp, color.copy(alpha = 0.28f), CircleShape)
            .pointerInput(Unit) {
                val longPressMs = viewConfiguration.longPressTimeoutMillis
                val doubleTapMs = viewConfiguration.doubleTapTimeoutMillis
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    pressed = true
                    var total = Offset.Zero
                    var dragging = false
                    // Fase 1: ¿mantener pulsado, arrastrar o soltar?
                    val outcome = withTimeoutOrNull(longPressMs) {
                        while (true) {
                            val ev = awaitPointerEvent()
                            val c = ev.changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull "up"
                            if (!c.pressed) { c.consume(); return@withTimeoutOrNull "up" }
                            total += c.position - c.previousPosition
                            c.consume()
                            if (total.getDistance() > viewConfiguration.touchSlop) return@withTimeoutOrNull "drag"
                        }
                        @Suppress("UNREACHABLE_CODE") "up"
                    }
                    when (outcome) {
                        null -> { // mantener pulsado
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            currentOnAction(currentActions.longPress)
                            while (true) {
                                val ev = awaitPointerEvent()
                                ev.changes.forEach { it.consume() }
                                if (ev.changes.none { it.pressed }) break
                            }
                        }
                        "drag" -> {
                            dragging = true
                            while (true) {
                                val ev = awaitPointerEvent()
                                val c = ev.changes.firstOrNull { it.id == down.id } ?: break
                                if (!c.pressed) { c.consume(); break }
                                total += c.position - c.previousPosition
                                c.consume()
                                val dir = when {
                                    total.getDistance() < threshold -> null
                                    abs(total.x) > abs(total.y) -> if (total.x > 0) Dir.Right else Dir.Left
                                    else -> if (total.y > 0) Dir.Down else Dir.Up
                                }
                                if (dir != armed) {
                                    armed = dir
                                    if (dir != null) view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                }
                                // resistencia: el botón se mueve como mucho maxTravel
                                val d = total.getDistance().coerceAtLeast(1f)
                                val travel = maxTravel * (1f - 1f / (1f + d / (maxTravel * 2f)))
                                scope.launch { offset.snapTo(total / d * travel) }
                            }
                            val dir = armed
                            armed = null
                            scope.launch { offset.animateTo(Offset.Zero, spring(dampingRatio = 0.55f, stiffness = 500f)) }
                            if (dir != null) {
                                currentOnAction(
                                    when (dir) {
                                        Dir.Up -> currentActions.swipeUp
                                        Dir.Down -> currentActions.swipeDown
                                        Dir.Left -> currentActions.swipeLeft
                                        Dir.Right -> currentActions.swipeRight
                                    }
                                )
                            }
                        }
                        else -> { // toque: ¿hay un segundo toque?
                            pressed = false
                            val wantsDouble = currentActions.doubleTap !is GestureAction.NoAction
                            val second = if (wantsDouble) withTimeoutOrNull(doubleTapMs) { awaitFirstDown() } else null
                            if (second != null) {
                                second.consume()
                                while (true) {
                                    val ev = awaitPointerEvent()
                                    ev.changes.forEach { it.consume() }
                                    if (ev.changes.none { it.pressed }) break
                                }
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                currentOnAction(currentActions.doubleTap)
                            } else {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                currentOnAction(currentActions.tap)
                            }
                        }
                    }
                    pressed = false
                    if (!dragging) armed = null
                }
            },
    ) {
        val icon = when (armed) {
            Dir.Up -> R.drawable.swipe_up_alt_24px
            Dir.Down -> R.drawable.swipe_down_alt_24px
            Dir.Left -> R.drawable.swipe_left_alt_24px
            Dir.Right -> R.drawable.swipe_right_alt_24px
            null -> if (actions.tap is GestureAction.Search) R.drawable.search_24px else R.drawable.adjust_24px
        }
        Icon(painterResource(icon), contentDescription = null, tint = if (armed != null) Color.Black else color, modifier = Modifier.size(24.dp))
    }
}

@Suppress("unused")
private fun Offset.len() = sqrt(x * x + y * y)
