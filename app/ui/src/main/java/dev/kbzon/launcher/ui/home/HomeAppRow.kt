package dev.kbzon.launcher.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kbzon.launcher.notifications.Notification
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.common.SearchablePicker
import dev.kbzon.launcher.ui.component.DismissableBottomSheet
import dev.kbzon.launcher.ui.component.ShapedLauncherIcon
import dev.kbzon.launcher.ui.launcher.sheets.LocalBottomSheetManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * [fork] Fila de la home minimalista.
 * - Toque: abre.  Mantener pulsado: menú propio.
 * - Deslizar a la derecha: despliega/oculta los shortcuts de la app debajo.
 * - Punto y vista previa de la última notificación.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeAppRow(
    item: SavableSearchable,
    vm: MinimalHomeVM,
    showIcons: Boolean,
    notifications: List<Notification>,
    showPreview: Boolean,
    isHiddenItem: Boolean,
    shadow: Shadow?,
    accent: Color,
    onLaunch: (SavableSearchable, IntRect?) -> Unit,
    swipeLeftTarget: SavableSearchable? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val sheetManager = LocalBottomSheetManager.current
    val iconSizePx = with(density) { 30.dp.roundToPx() }
    val thresholdPx = with(density) { 56.dp.toPx() }

    var expanded by remember(item.key) { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var pickSwipeLeft by remember { mutableStateOf(false) }
    // Límites de la fila en la ventana: la app se abre expandiéndose desde aquí.
    var rowBounds by remember { mutableStateOf<IntRect?>(null) }
    val dragOffset = remember { Animatable(0f) }
    val canExpand = item is Application

    Column(modifier = modifier.fillMaxWidth()) {
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        val r = coords.boundsInWindow()
                        rowBounds = IntRect(r.left.roundToInt(), r.top.roundToInt(), r.right.roundToInt(), r.bottom.roundToInt())
                    }
                    .offset { IntOffset(dragOffset.value.roundToInt(), 0) }
                    .pointerInput(item.key, canExpand, swipeLeftTarget?.key) {
                        // derecha: shortcuts (solo apps) · izquierda: app asignada (si hay)
                        val maxRight = if (canExpand) thresholdPx * 1.4f else 0f
                        val maxLeft = if (swipeLeftTarget != null) thresholdPx * 1.4f else thresholdPx * 0.25f
                        if (maxRight == 0f && swipeLeftTarget == null) return@pointerInput
                        var crossed = 0 // 1 = derecha, -1 = izquierda
                        detectHorizontalDragGestures(
                            onDragStart = { crossed = 0 },
                            onDragEnd = {
                                when (crossed) {
                                    1 -> expanded = !expanded
                                    -1 -> swipeLeftTarget?.let { onLaunch(it, rowBounds) }
                                }
                                scope.launch { dragOffset.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 500f)) }
                            },
                            onDragCancel = { scope.launch { dragOffset.animateTo(0f) } },
                        ) { change, dx ->
                            change.consume()
                            // resistencia elástica: se mueve menos cuanto más se arrastra
                            val next = (dragOffset.value + dx * 0.5f).coerceIn(-maxLeft, maxRight)
                            scope.launch { dragOffset.snapTo(next) }
                            val now = when {
                                next >= thresholdPx && canExpand -> 1
                                next <= -thresholdPx && swipeLeftTarget != null -> -1
                                else -> 0
                            }
                            if (now != crossed) {
                                crossed = now
                                if (now != 0) view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            }
                        }
                    }
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onLaunch(item, rowBounds) },
                        onLongClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            menuOpen = true
                        },
                    )
                    .padding(vertical = 9.dp),
            ) {
                if (showIcons) {
                    val icon by remember(item.key) { vm.icon(item, iconSizePx) }.collectAsState(null)
                    ShapedLauncherIcon(size = 30.dp, icon = { icon })
                    Spacer(Modifier.width(16.dp))
                }
                Column(Modifier.weight(1f, fill = false)) {
                    Text(
                        text = item.displayLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Normal, shadow = shadow),
                    )
                    val latest = notifications.firstOrNull()
                    if (showPreview && latest != null) {
                        Text(
                            text = listOfNotNull(latest.title, latest.text).joinToString(": "),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = LocalContentColor.current.copy(alpha = 0.7f),
                            style = TextStyle(fontSize = 13.sp, shadow = shadow),
                        )
                    }
                }
                if (notifications.isNotEmpty()) {
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.size(7.dp).background(accent, CircleShape))
                }
            }

            // Ícono de la app asignada, aparece en el borde al deslizar a la izquierda.
            if (swipeLeftTarget != null) {
                val targetIconPx = with(density) { 26.dp.roundToPx() }
                val targetIcon by remember(swipeLeftTarget.key) { vm.icon(swipeLeftTarget, targetIconPx) }.collectAsState(null)
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .graphicsLayer {
                            val p = (-dragOffset.value / thresholdPx).coerceIn(0f, 1f)
                            alpha = p
                            scaleX = 0.6f + 0.4f * p
                            scaleY = 0.6f + 0.4f * p
                        }
                ) {
                    ShapedLauncherIcon(size = 26.dp, icon = { targetIcon })
                }
            }

            HomeItemMenu(
                expanded = menuOpen,
                onDismiss = { menuOpen = false },
                item = item,
                vm = vm,
                isHiddenItem = isHiddenItem,
                onShowShortcuts = if (canExpand) ({ expanded = true }) else null,
                onCustomize = { sheetManager.showCustomizeSearchableModal(item) },
                swipeLeftTarget = swipeLeftTarget,
                onPickSwipeLeft = { pickSwipeLeft = true },
            )

            DismissableBottomSheet(expanded = pickSwipeLeft, onDismissRequest = { pickSwipeLeft = false }) {
                Column {
                    Text(
                        stringResource(R.string.fork_swipe_left_picker_title, item.displayLabel),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    )
                    SearchablePicker(
                        value = swipeLeftTarget,
                        onValueChanged = {
                            vm.setSwipeLeftTarget(item, it)
                            pickSwipeLeft = false
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    )
                }
            }
        }

        if (item is Application) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(spring(dampingRatio = 0.85f, stiffness = 600f)) + fadeIn(),
                exit = shrinkVertically(spring(stiffness = 900f)) + fadeOut(),
            ) {
                val shortcuts by remember(item.key) { vm.shortcuts(item) }.collectAsState(emptyList())
                val shortcutIconPx = with(density) { 22.dp.roundToPx() }
                Column(Modifier.padding(start = if (showIcons) 46.dp else 12.dp, bottom = 6.dp)) {
                    if (shortcuts.isEmpty()) {
                        Text(
                            stringResource(R.string.fork_minimal_home_no_shortcuts),
                            color = LocalContentColor.current.copy(alpha = 0.6f),
                            style = TextStyle(fontSize = 14.sp, shadow = shadow),
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                    shortcuts.forEach { shortcut ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onLaunch(shortcut, rowBounds) },
                                )
                                .padding(vertical = 7.dp),
                        ) {
                            val icon by remember(shortcut.key) { vm.icon(shortcut, shortcutIconPx) }.collectAsState(null)
                            ShapedLauncherIcon(size = 22.dp, icon = { icon })
                            Spacer(Modifier.width(12.dp))
                            Text(
                                shortcut.displayLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontSize = 16.sp, shadow = shadow),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeItemMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    item: SavableSearchable,
    vm: MinimalHomeVM,
    isHiddenItem: Boolean,
    onShowShortcuts: (() -> Unit)?,
    onCustomize: () -> Unit,
    swipeLeftTarget: SavableSearchable?,
    onPickSwipeLeft: () -> Unit,
) {
    val context = LocalContext.current
    val pinned by remember(item.key) { vm.isPinned(item) }.collectAsState(false)
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        @Composable
        fun entry(icon: Int, text: Int, action: () -> Unit) = DropdownMenuItem(
            leadingIcon = { Icon(painterResource(icon), contentDescription = null) },
            text = { Text(stringResource(text)) },
            onClick = { onDismiss(); action() },
        )
        if (pinned) entry(R.drawable.star_24px_filled, R.string.fork_menu_unpin) { vm.unpin(item) }
        else entry(R.drawable.star_24px, R.string.fork_menu_pin) { vm.pin(item) }
        if (onShowShortcuts != null) entry(R.drawable.apps_24px, R.string.fork_menu_shortcuts, onShowShortcuts)
        entry(R.drawable.swipe_left_alt_24px, if (swipeLeftTarget == null) R.string.fork_menu_swipe_left else R.string.fork_menu_swipe_left_change, onPickSwipeLeft)
        if (swipeLeftTarget != null) entry(R.drawable.close_24px, R.string.fork_menu_swipe_left_clear) { vm.setSwipeLeftTarget(item, null) }
        entry(R.drawable.edit_24px, R.string.fork_menu_customize, onCustomize)
        if (isHiddenItem) entry(R.drawable.visibility_24px, R.string.fork_menu_unhide) { vm.unhide(item) }
        else entry(R.drawable.visibility_off_24px, R.string.fork_menu_hide) { vm.hide(item) }
        if (item is Application) {
            entry(R.drawable.info_24px, R.string.fork_menu_app_info) { item.openAppDetails(context) }
            entry(R.drawable.delete_24px, R.string.fork_menu_uninstall) { item.uninstall(context) }
        }
    }
}
