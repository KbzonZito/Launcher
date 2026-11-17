package dev.kbzon.launcher.ui.home

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kbzon.launcher.preferences.home.WaveIntensity
import dev.kbzon.launcher.ui.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * [fork] Estado compartido del riel: lo usan el riel visible y la zona táctil del borde contrario.
 */
@Stable
class RailState {
    /** y del dedo dentro del riel; NaN = sin tocar */
    var touchY by mutableFloatStateOf(Float.NaN)
    var heightPx by mutableIntStateOf(0)
    /** true si el gesto actual empezó en el borde contrario (se muestra la burbuja) */
    var fromMirror by mutableStateOf(false)
    var currentIndex by mutableIntStateOf(-1)
}

/** Alto del riel: [fraction] del disponible, como mucho [maxLetterHeight] por letra. */
fun Modifier.railHeight(letterCount: Int, fraction: Float = 0.62f, maxLetterHeight: Dp = 28.dp): Modifier = layout { measurable, constraints ->
    val cap = (maxLetterHeight * letterCount).roundToPx()
    val h = min((constraints.maxHeight * fraction).toInt(), cap)
    val placeable = measurable.measure(constraints.copy(minHeight = h, maxHeight = h))
    layout(placeable.width, h) { placeable.place(0, 0) }
}

/**
 * Gestos del riel: arrastrar recorre las letras (háptica por letra), soltar avisa la letra final
 * y dos toques rápidos sin moverse disparan [onDoubleTap].
 */
private fun Modifier.railTouch(
    state: RailState,
    letters: List<Char>,
    mirror: Boolean,
    view: View,
    onLetter: () -> (Char) -> Unit,
    onRelease: () -> (Char) -> Unit,
    onDoubleTap: () -> (() -> Unit)?,
    onDragStateChange: () -> (Boolean) -> Unit,
): Modifier = pointerInput(letters, mirror) {
    var lastTapUp = 0L
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        down.consume()
        val downTime = down.uptimeMillis
        var moved = 0f
        state.fromMirror = mirror
        onDragStateChange()(true)
        fun update(y: Float) {
            if (state.heightPx == 0 || letters.isEmpty()) return
            state.touchY = y.coerceIn(0f, state.heightPx.toFloat())
            val slot = state.heightPx.toFloat() / letters.size
            val index = (state.touchY / slot).toInt().coerceIn(0, letters.lastIndex)
            if (index != state.currentIndex) {
                state.currentIndex = index
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onLetter()(letters[index])
            }
        }
        state.currentIndex = -1
        update(down.position.y)
        var upTime = downTime
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) {
                change.consume()
                upTime = change.uptimeMillis
                break
            }
            moved += abs(change.positionChange().y)
            change.consume()
            update(change.position.y)
        }
        val index = state.currentIndex
        state.touchY = Float.NaN
        state.fromMirror = false
        onDragStateChange()(false)

        val isTap = moved < viewConfiguration.touchSlop && upTime - downTime < 250
        val doubleTap = onDoubleTap()
        if (isTap && doubleTap != null && upTime - lastTapUp < 300) {
            lastTapUp = 0L
            doubleTap()
            return@awaitEachGesture
        }
        lastTapUp = if (isTap) upTime else 0L
        if (index >= 0) onRelease()(letters[index])
    }
}

/**
 * [fork] Riel del alfabeto estilo Niagara (visible).
 *
 * Al arrastrar, las letras cercanas al dedo se agrandan y se desplazan hacia el contenido ("ola"),
 * con un toque háptico por letra. Soltar NO abre nada: la letra queda seleccionada y la home
 * despliega sus apps. Las transformaciones se aplican en la fase de dibujo (graphicsLayer).
 */
@Composable
fun AlphabetRail(
    state: RailState,
    letters: List<Char>,
    selected: Char?,
    onLetter: (Char) -> Unit,
    onRelease: (Char) -> Unit,
    onDoubleTap: (() -> Unit)?,
    onDragStateChange: (dragging: Boolean) -> Unit,
    color: Color,
    accent: Color,
    railOnLeft: Boolean,
    intensity: WaveIntensity,
    modifier: Modifier = Modifier,
    heightFraction: Float = 0.62f,
    bubbleColor: Color = accent,
    onBubble: Color = Color.Black,
) {
    if (letters.isEmpty()) return
    val view = LocalView.current
    val density = LocalDensity.current
    val bulgePx = with(density) { intensity.bulgeDp.dp.toPx() }
    val currentOnLetter by rememberUpdatedState(onLetter)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val currentOnDoubleTap by rememberUpdatedState(onDoubleTap)
    val currentOnDrag by rememberUpdatedState(onDragStateChange)

    val waveStrength by animateFloatAsState(
        targetValue = if (state.touchY.isNaN()) 0f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "wave",
    )
    var lastY by remember { mutableFloatStateOf(0f) }
    if (!state.touchY.isNaN()) lastY = state.touchY

    Box(
        modifier = modifier
            .width(40.dp)
            .railHeight(letters.size, heightFraction)
            .layout { m, c ->
                val p = m.measure(c)
                state.heightPx = p.height
                layout(p.width, p.height) { p.place(0, 0) }
            }
            .railTouch(state, letters, mirror = false, view, { currentOnLetter }, { currentOnRelease }, { currentOnDoubleTap }, { currentOnDrag }),
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
                        if (waveStrength == 0f || state.heightPx == 0) return@graphicsLayer
                        val slot = state.heightPx.toFloat() / letters.size
                        val center = slot * (index + 0.5f)
                        // B4: curva coseno ancha (~6 letras a cada lado): subida y bajada largas, sin pico.
                        val distance = abs(lastY - center) / (slot * 6f)
                        val influence = (if (distance < 1f) (1f + cos(PI.toFloat() * distance)) / 2f else 0f) * waveStrength
                        val scale = 1f + (intensity.scale) * influence
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
        // B5: burbuja con la letra, siempre junto al alfabeto y a la altura del dedo.
        val idx = state.currentIndex
        if (!state.touchY.isNaN() && idx in letters.indices) {
            val letter = letters[idx]
            val bubble = 60.dp
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        val b = bubble.roundToPx()
                        val gap = 20.dp.roundToPx()
                        val railW = 40.dp.roundToPx()
                        val x = if (railOnLeft) railW + bulgePx.roundToInt() + gap else -b - bulgePx.roundToInt() - gap
                        IntOffset(x, (state.touchY - b / 2f).roundToInt())
                    }
                    .requiredSize(bubble)
                    .background(bubbleColor, CircleShape),
            ) {
                if (letter == MinimalHomeVM.HIDDEN) {
                    Icon(painterResource(R.drawable.lock_20px), contentDescription = null, tint = onBubble, modifier = Modifier.size(22.dp))
                } else {
                    Text(letter.toString(), color = onBubble, fontSize = 28.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/**
 * [fork] Zona táctil invisible en el borde contrario: recorre el mismo riel. Mientras se usa,
 * muestra junto al dedo una burbuja con la letra actual.
 */
@Composable
fun RailMirrorZone(
    state: RailState,
    letters: List<Char>,
    onLetter: (Char) -> Unit,
    onRelease: (Char) -> Unit,
    onDoubleTap: (() -> Unit)?,
    onDragStateChange: (dragging: Boolean) -> Unit,
    accent: Color,
    onAccent: Color,
    zoneOnLeft: Boolean,
    modifier: Modifier = Modifier,
    heightFraction: Float = 0.62f,
) {
    if (letters.isEmpty()) return
    val view = LocalView.current
    val density = LocalDensity.current
    val currentOnLetter by rememberUpdatedState(onLetter)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val currentOnDoubleTap by rememberUpdatedState(onDoubleTap)
    val currentOnDrag by rememberUpdatedState(onDragStateChange)
    val zoneWidth = 28.dp
    val bubble = 60.dp

    Box(
        modifier = modifier
            .width(zoneWidth)
            .railHeight(letters.size, heightFraction)
            .railTouch(state, letters, mirror = true, view, { currentOnLetter }, { currentOnRelease }, { currentOnDoubleTap }, { currentOnDrag }),
    ) {
        // La burbuja ahora la dibuja el alfabeto visible (siempre de su lado).

    }
}
