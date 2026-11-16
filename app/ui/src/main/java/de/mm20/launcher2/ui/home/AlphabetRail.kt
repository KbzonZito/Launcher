package de.mm20.launcher2.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import de.mm20.launcher2.ui.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * [fork] Riel del alfabeto estilo Niagara.
 *
 * Al arrastrar, las letras cercanas al dedo se agrandan y se desplazan hacia el contenido ("ola"),
 * con un toque háptico por letra. Soltar NO abre nada: la letra queda seleccionada y la home
 * despliega sus apps en la lista.
 *
 * Las transformaciones se aplican en la fase de dibujo (graphicsLayer) leyendo la posición
 * del dedo, así el arrastre no recompone las letras.
 */
@Composable
fun AlphabetRail(
    letters: List<Char>,
    selected: Char?,
    onLetter: (Char) -> Unit,
    onRelease: (Char) -> Unit,
    onDragStateChange: (dragging: Boolean) -> Unit,
    color: Color,
    accent: Color,
    railOnLeft: Boolean,
    modifier: Modifier = Modifier,
    bulge: Dp = 36.dp,
    maxLetterHeight: Dp = 28.dp,
) {
    if (letters.isEmpty()) return
    val view = LocalView.current
    val density = LocalDensity.current
    val bulgePx = with(density) { bulge.toPx() }
    val currentOnLetter by rememberUpdatedState(onLetter)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val currentOnDrag by rememberUpdatedState(onDragStateChange)

    var heightPx by remember { mutableIntStateOf(0) }
    // y del dedo dentro del riel; NaN = sin tocar
    var touchY by remember { mutableFloatStateOf(Float.NaN) }
    // intensidad de la ola, animada para que entre y salga suavemente
    val waveStrength by animateFloatAsState(
        targetValue = if (touchY.isNaN()) 0f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
        label = "wave",
    )
    var lastY by remember { mutableFloatStateOf(0f) }
    if (!touchY.isNaN()) lastY = touchY

    Box(
        modifier = modifier
            .width(40.dp)
            // Alto = 62 % del disponible, pero como mucho maxLetterHeight por letra (con pocas apps no se estira).
            .layout { measurable, constraints ->
                val cap = (maxLetterHeight * letters.size).roundToPx()
                val h = min((constraints.maxHeight * 0.62f).toInt(), cap)
                val placeable = measurable.measure(constraints.copy(minHeight = h, maxHeight = h))
                layout(placeable.width, h) { placeable.place(0, 0) }
            }
            .onSizeChanged { heightPx = it.height }
            .pointerInput(letters) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    currentOnDrag(true)
                    var lastIndex = -1
                    fun update(y: Float) {
                        touchY = y.coerceIn(0f, heightPx.toFloat())
                        val slot = heightPx.toFloat() / letters.size
                        val index = (touchY / slot).toInt().coerceIn(0, letters.lastIndex)
                        if (index != lastIndex) {
                            lastIndex = index
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            currentOnLetter(letters[index])
                        }
                    }
                    update(down.position.y)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            change.consume()
                            break
                        }
                        change.consume()
                        update(change.position.y)
                    }
                    touchY = Float.NaN
                    currentOnDrag(false)
                    if (lastIndex >= 0) currentOnRelease(letters[lastIndex])
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            letters.forEachIndexed { index, letter ->
                val isSelected = letter == selected
                val waveModifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)
                    .graphicsLayer {
                        if (waveStrength == 0f || heightPx == 0) return@graphicsLayer
                        val slot = heightPx.toFloat() / letters.size
                        val center = slot * (index + 0.5f)
                        val distance = abs(lastY - center) / (slot * 3.5f)
                        val influence = max(0f, 1f - distance).let { it * it } * waveStrength
                        val scale = 1f + 1.4f * influence
                        scaleX = scale
                        scaleY = scale
                        translationX = (if (railOnLeft) 1f else -1f) * bulgePx * influence
                        transformOrigin = TransformOrigin.Center
                    }
                if (letter == MinimalHomeVM.HIDDEN) {
                    Box(waveModifier, contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.lock_20px),
                            contentDescription = null,
                            tint = if (isSelected) accent else color.copy(alpha = 0.72f),
                            modifier = Modifier.size(12.dp),
                        )
                    }
                    return@forEachIndexed
                }
                Text(
                    text = letter.toString(),
                    textAlign = TextAlign.Center,
                    color = if (isSelected) accent else color.copy(alpha = 0.72f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (letter == MinimalHomeVM.RECENT || letter == MinimalHomeVM.STAR) 15.sp else 12.sp,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = waveModifier,
                )
            }
        }
    }
}
