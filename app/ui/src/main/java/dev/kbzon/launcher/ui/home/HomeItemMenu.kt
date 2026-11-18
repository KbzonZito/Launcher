package dev.kbzon.launcher.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.Tag
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.ShapedLauncherIcon
import kotlin.math.roundToInt

/** Ocupa toda la ventana: el velo cubre la pantalla y la tarjeta se ubica a mano. */
private object FullWindowPosition : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset.Zero
}

/**
 * [fork] F16: menú de una app o carpeta de la home, como tarjeta anclada a su fila (estilo Niagara).
 * Antes era un DropdownMenu que se dibujaba encima del reloj sin velo y dejaba ver la home a través.
 * Arriba van el nombre y los atajos de la app; debajo, las acciones.
 */
@Composable
internal fun HomeItemMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    anchor: IntRect?,
    item: SavableSearchable,
    vm: MinimalHomeVM,
    isHiddenItem: Boolean,
    onLaunchShortcut: (SavableSearchable) -> Unit,
    onCustomize: () -> Unit,
    swipeLeftTarget: SavableSearchable?,
    onPickSwipeLeft: () -> Unit,
    onReorderFavorites: (() -> Unit)?,
    onFolder: () -> Unit,
    onRenameFolder: () -> Unit,
) {
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = expanded
    if (!visibleState.currentState && !visibleState.targetState) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val pinned by remember(item.key) { vm.isPinned(item) }.collectAsState(false)

    Popup(
        popupPositionProvider = FullWindowPosition,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, clippingEnabled = false),
    ) {
        // La ventana del popup puede no empezar en (0,0) de la pantalla: se resta su origen.
        var origin by remember { mutableStateOf(IntOffset.Zero) }
        Box(Modifier.fillMaxSize().onGloballyPositioned { c ->
            val o = c.positionOnScreen(); origin = IntOffset(o.x.roundToInt(), o.y.roundToInt())
        }) {
            AnimatedVisibility(visibleState, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .combinedClickable(interactionSource = null, indication = null, onClick = onDismiss),
                )
            }
            val safe = WindowInsets.safeDrawing
            val marginPx = with(density) { 16.dp.roundToPx() }
            // La tarjeta se superpone a la fila (su encabezado queda donde estaba la app) y se mantiene
            // dentro de la pantalla: si no cabe hacia abajo, sube.
            Layout(
                content = {
                    AnimatedVisibility(
                        visibleState,
                        enter = scaleIn(spring(dampingRatio = 0.8f, stiffness = 700f), initialScale = 0.9f, transformOrigin = TransformOrigin(0.2f, 0f)) + fadeIn(),
                        exit = scaleOut(targetScale = 0.95f) + fadeOut(),
                    ) {
                        MenuCard(
                            item = item,
                            vm = vm,
                            pinned = pinned,
                            isHiddenItem = isHiddenItem,
                            onDismiss = onDismiss,
                            onLaunchShortcut = onLaunchShortcut,
                            onCustomize = onCustomize,
                            swipeLeftTarget = swipeLeftTarget,
                            onPickSwipeLeft = onPickSwipeLeft,
                            onReorderFavorites = onReorderFavorites,
                            onFolder = onFolder,
                            onRenameFolder = onRenameFolder,
                            openAppDetails = { (item as? Application)?.openAppDetails(context) },
                            uninstall = { (item as? Application)?.uninstall(context) },
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) { measurables, constraints ->
                val top = safe.getTop(this) + marginPx
                val bottom = constraints.maxHeight - safe.getBottom(this) - marginPx
                val maxW = minOf(constraints.maxWidth - 2 * marginPx, 420.dp.roundToPx())
                val placeable = measurables.firstOrNull()?.measure(
                    constraints.copy(minWidth = 0, maxWidth = maxW, minHeight = 0, maxHeight = (bottom - top).coerceAtLeast(0))
                )
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable ?: return@layout
                    val anchorTop = anchor?.let { it.top - origin.y } ?: ((constraints.maxHeight - placeable.height) / 2)
                    val y = (anchorTop - 8.dp.roundToPx()).coerceAtMost(bottom - placeable.height).coerceAtLeast(top)
                    val anchorLeft = anchor?.let { it.left - origin.x } ?: marginPx
                    val x = anchorLeft.coerceIn(marginPx, (constraints.maxWidth - marginPx - placeable.width).coerceAtLeast(marginPx))
                    placeable.place(x, y)
                }
            }
        }
    }
}

@Composable
private fun MenuCard(
    item: SavableSearchable,
    vm: MinimalHomeVM,
    pinned: Boolean,
    isHiddenItem: Boolean,
    onDismiss: () -> Unit,
    onLaunchShortcut: (SavableSearchable) -> Unit,
    onCustomize: () -> Unit,
    swipeLeftTarget: SavableSearchable?,
    onPickSwipeLeft: () -> Unit,
    onReorderFavorites: (() -> Unit)?,
    onFolder: () -> Unit,
    onRenameFolder: () -> Unit,
    openAppDetails: () -> Unit,
    uninstall: () -> Unit,
) {
    val density = LocalDensity.current
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .widthIn(min = 260.dp)
            .combinedClickable(interactionSource = null, indication = null, onClick = {}),
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            // Encabezado: ícono + nombre.
            val headerIconPx = with(density) { 24.dp.roundToPx() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                if (item is Tag) {
                    FolderIcon(vm, item, 24.dp)
                } else {
                    val icon by remember(item.key) { vm.icon(item, headerIconPx) }.collectAsState(null)
                    ShapedLauncherIcon(size = 24.dp, icon = { icon })
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    item.displayLabel,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Atajos de la app, como en Niagara.
            if (item is Application) {
                val shortcuts by remember(item.key) { vm.shortcuts(item) }.collectAsState(emptyList())
                val iconPx = with(density) { 22.dp.roundToPx() }
                shortcuts.take(5).forEach { shortcut ->
                    val icon by remember(shortcut.key) { vm.icon(shortcut, iconPx) }.collectAsState(null)
                    MenuRow(
                        leading = { ShapedLauncherIcon(size = 22.dp, icon = { icon }) },
                        text = shortcut.displayLabel,
                        onClick = { onDismiss(); onLaunchShortcut(shortcut) },
                    )
                }
                if (shortcuts.isNotEmpty()) HorizontalDivider(Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
            }

            @Composable
            fun entry(icon: Int, text: Int, action: () -> Unit) = MenuRow(
                leading = { Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp)) },
                text = stringResource(text),
                onClick = { onDismiss(); action() },
            )

            if (item is Tag) {
                // Carpeta: renombrar, reordenar favoritos y eliminar (sus apps vuelven a favoritos).
                entry(R.drawable.edit_24px, R.string.fork_folder_rename, onRenameFolder)
                if (onReorderFavorites != null) entry(R.drawable.drag_indicator_24px, R.string.fork_reorder_title, onReorderFavorites)
                entry(R.drawable.delete_24px, R.string.fork_folder_delete) { vm.deleteFolder(item) }
                return@Column
            }
            if (pinned) entry(R.drawable.star_24px_filled, R.string.fork_menu_unpin) { vm.unpin(item) }
            else entry(R.drawable.star_24px, R.string.fork_menu_pin) { vm.pin(item) }
            if (pinned && onReorderFavorites != null) entry(R.drawable.drag_indicator_24px, R.string.fork_reorder_title, onReorderFavorites)
            if (!isHiddenItem) entry(R.drawable.create_new_folder_24px, R.string.fork_menu_folder, onFolder)
            entry(R.drawable.swipe_left_alt_24px, if (swipeLeftTarget == null) R.string.fork_menu_swipe_left else R.string.fork_menu_swipe_left_change, onPickSwipeLeft)
            if (swipeLeftTarget != null) entry(R.drawable.close_24px, R.string.fork_menu_swipe_left_clear) { vm.setSwipeLeftTarget(item, null) }
            entry(R.drawable.edit_24px, R.string.fork_menu_customize, onCustomize)
            if (isHiddenItem) entry(R.drawable.visibility_24px, R.string.fork_menu_unhide) { vm.unhide(item) }
            else entry(R.drawable.visibility_off_24px, R.string.fork_menu_hide) { vm.hide(item) }
            if (item is Application) {
                entry(R.drawable.info_24px, R.string.fork_menu_app_info, openAppDetails)
                entry(R.drawable.delete_24px, R.string.fork_menu_uninstall, uninstall)
            }
        }
    }
}

@Composable
private fun MenuRow(leading: @Composable () -> Unit, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
    ) {
        leading()
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
