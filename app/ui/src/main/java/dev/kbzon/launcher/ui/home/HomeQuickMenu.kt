package dev.kbzon.launcher.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.preferences.home.MinimalClockStyle
import dev.kbzon.launcher.ui.R
import kotlin.math.roundToInt

/**
 * [fork] Menú rápido de la home (mantener pulsado un espacio vacío): ajustes frecuentes en una
 * tarjeta, más accesos a "Editar diseño", Gestos y Ajustes.
 */
@Composable
fun HomeQuickMenu(
    visible: Boolean,
    anchorY: Float,
    clockStyle: MinimalClockStyle,
    onClockStyle: (MinimalClockStyle) -> Unit,
    railOnLeft: Boolean,
    onRailOnLeft: (Boolean) -> Unit,
    splitSwipeDown: Boolean,
    onSplitSwipeDown: (Boolean) -> Unit,
    showIcons: Boolean,
    onShowIcons: (Boolean) -> Unit,
    actionButton: Boolean,
    onActionButton: (Boolean) -> Unit,
    onEditLayout: () -> Unit,
    onGestures: () -> Unit,
    onSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .combinedClickable(interactionSource = null, indication = null, onClick = onDismiss),
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(spring(dampingRatio = 0.8f, stiffness = 700f), initialScale = 0.9f, transformOrigin = TransformOrigin(0.5f, 0f)) + fadeIn(),
                exit = scaleOut(targetScale = 0.95f) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    // Se coloca cerca del dedo, sin salirse de la pantalla.
                    .layout { measurable, constraints ->
                        val p = measurable.measure(constraints.copy(minHeight = 0))
                        val margin = 24.dp.roundToPx()
                        val y = (anchorY - p.height / 3f).roundToInt()
                            .coerceIn(margin, (constraints.maxHeight - p.height - margin).coerceAtLeast(margin))
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            p.place((constraints.maxWidth - p.width) / 2, y)
                        }
                    },
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .widthIn(max = 380.dp)
                        .fillMaxWidth()
                        .combinedClickable(interactionSource = null, indication = null, onClick = {}),
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(stringResource(R.string.fork_quick_menu_title), style = MaterialTheme.typography.titleMedium)

                        Label(stringResource(R.string.fork_preference_clock_style))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            clockLabels.forEach { (style, label) ->
                                FilterChip(
                                    selected = style == clockStyle,
                                    onClick = { onClockStyle(style) },
                                    label = { Text(stringResource(label)) },
                                )
                            }
                        }

                        Label(stringResource(R.string.fork_quick_menu_rail_side))
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            SegmentedButton(railOnLeft, { onRailOnLeft(true) }, SegmentedButtonDefaults.itemShape(0, 2)) {
                                Text(stringResource(R.string.fork_quick_menu_left))
                            }
                            SegmentedButton(!railOnLeft, { onRailOnLeft(false) }, SegmentedButtonDefaults.itemShape(1, 2)) {
                                Text(stringResource(R.string.fork_quick_menu_right))
                            }
                        }

                        Label(stringResource(R.string.fork_quick_menu_swipe_down))
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            SegmentedButton(!splitSwipeDown, { onSplitSwipeDown(false) }, SegmentedButtonDefaults.itemShape(0, 2)) {
                                Text(stringResource(R.string.fork_quick_menu_search))
                            }
                            SegmentedButton(splitSwipeDown, { onSplitSwipeDown(true) }, SegmentedButtonDefaults.itemShape(1, 2)) {
                                Text(stringResource(R.string.fork_quick_menu_split))
                            }
                        }

                        SwitchRow(stringResource(R.string.fork_preference_minimal_home_icons), showIcons, onShowIcons)
                        SwitchRow(stringResource(R.string.fork_preference_action_button), actionButton, onActionButton)

                        Spacer(Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = onEditLayout, modifier = Modifier.weight(1f)) {
                                Icon(painterResource(R.drawable.edit_24px), contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(stringResource(R.string.fork_quick_menu_edit_layout), maxLines = 1)
                            }
                            OutlinedButton(onClick = onGestures, modifier = Modifier.weight(1f)) {
                                Icon(painterResource(R.drawable.gesture_24px), contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(stringResource(R.string.preference_screen_gestures), maxLines = 1)
                            }
                        }
                        FilledTonalButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                            Icon(painterResource(R.drawable.settings_24px), contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.settings))
                        }
                    }
                }
            }
        }
    }
}

private val clockLabels = listOf(
    MinimalClockStyle.Thin to R.string.fork_clock_thin,
    MinimalClockStyle.Bold to R.string.fork_clock_bold,
    MinimalClockStyle.Stacked to R.string.fork_clock_stacked,
    MinimalClockStyle.Words to R.string.fork_clock_words,
    MinimalClockStyle.Analog to R.string.fork_clock_analog,
    MinimalClockStyle.DayProgress to R.string.fork_clock_day_progress,
)

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun SwitchRow(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
