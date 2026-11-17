package dev.kbzon.launcher.ui.home

import dev.kbzon.launcher.ui.theme.typography.fontfamily.LocalAppFontFamily
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.ShapedLauncherIcon

/**
 * [fork] Reordenar favoritos arrastrando el asa de cada fila. El orden se guarda al tocar "Listo".
 */
@Composable
fun HomeReorderFavorites(
    items: List<SavableSearchable>,
    vm: MinimalHomeVM,
    showIcons: Boolean,
    shadow: Shadow?,
    accent: Color,
    maxHeight: Dp,
    onDone: (List<SavableSearchable>) -> Unit,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val rowHeight = 52.dp
    val rowPx = with(density) { rowHeight.toPx() }
    val iconPx = with(density) { 28.dp.roundToPx() }
    var order by remember(items) { mutableStateOf(items) }
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragDy by remember { mutableFloatStateOf(0f) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.fork_reorder_title),
                color = accent,
                style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 22.sp, shadow = shadow),
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { onDone(order) }) { Text(stringResource(R.string.fork_edit_layout_done), color = accent) }
        }
        Column(Modifier.heightIn(max = maxHeight).verticalScroll(rememberScrollState())) {
            order.forEach { item ->
                key(item.key) {
                    val dragging = draggingKey == item.key
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight)
                            .zIndex(if (dragging) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (dragging) dragDy else 0f
                                val s = if (dragging) 1.03f else 1f
                                scaleX = s; scaleY = s
                            }
                            .background(
                                if (dragging) LocalContentColor.current.copy(alpha = 0.10f) else Color.Transparent,
                                RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 6.dp),
                    ) {
                        if (showIcons) {
                            val icon by remember(item.key) { vm.icon(item, iconPx) }.collectAsState(null)
                            ShapedLauncherIcon(size = 28.dp, icon = { icon })
                            Spacer(Modifier.width(14.dp))
                        }
                        Text(
                            item.displayLabel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 20.sp, shadow = shadow),
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            painterResource(R.drawable.drag_indicator_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp)
                                .pointerInput(item.key) {
                                    detectDragGestures(
                                        onDragStart = { draggingKey = item.key; dragDy = 0f },
                                        onDragEnd = { draggingKey = null; dragDy = 0f },
                                        onDragCancel = { draggingKey = null; dragDy = 0f },
                                    ) { change, amount ->
                                        change.consume()
                                        dragDy += amount.y
                                        val idx = order.indexOfFirst { it.key == item.key }
                                        if (dragDy > rowPx / 2 && idx in 0 until order.lastIndex) {
                                            order = order.toMutableList().apply { add(idx + 1, removeAt(idx)) }
                                            dragDy -= rowPx
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        } else if (dragDy < -rowPx / 2 && idx > 0) {
                                            order = order.toMutableList().apply { add(idx - 1, removeAt(idx)) }
                                            dragDy += rowPx
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        }
                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}
