package dev.kbzon.launcher.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.Tag
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.ShapedLauncherIcon
import dev.kbzon.launcher.ui.component.LocalIconShape

/**
 * [fork] Fase 12: icono de carpeta. Mosaico 2×2 con las primeras apps dentro de una tarjeta
 * con la forma de icono elegida; vacío muestra el símbolo de carpeta.
 */
@Composable
fun FolderIcon(vm: MinimalHomeVM, folder: Tag, size: Dp) {
    val items by remember(folder.key) { vm.folderItems(folder) }.collectAsState(emptyList())
    val cell = size * 0.38f
    val cellPx = with(LocalDensity.current) { cell.roundToPx() }
    Box(
        Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f), LocalIconShape.current),
        contentAlignment = Alignment.Center,
    ) {
        if (items.isEmpty()) {
            Icon(painterResource(R.drawable.folder_24px), contentDescription = null, modifier = Modifier.size(size * 0.6f))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(size * 0.06f)) {
                items.take(4).chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(size * 0.06f)) {
                        row.forEach { item ->
                            val icon by remember(item.key) { vm.icon(item, cellPx) }.collectAsState(null)
                            ShapedLauncherIcon(size = cell, icon = { icon })
                        }
                        if (row.size == 1) Spacer(Modifier.size(cell))
                    }
                }
            }
        }
    }
}

/** Diálogo para nombrar una carpeta (crear o renombrar). */
@Composable
fun FolderNameDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf(TextFieldValue(initial, TextRange(0, initial.length))) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val confirm = { if (value.text.isNotBlank()) onConfirm(value.text.trim()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                label = { Text(stringResource(R.string.fork_folder_name)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { confirm() }),
                modifier = Modifier.focusRequester(focus),
            )
        },
        confirmButton = { TextButton(onClick = confirm, enabled = value.text.isNotBlank()) { Text(stringResource(android.R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } },
    )
}

/** Elige una carpeta existente para [item], o crea una nueva. */
@Composable
fun FolderPickerDialog(
    vm: MinimalHomeVM,
    item: SavableSearchable,
    onDismiss: () -> Unit,
) {
    val folders by vm.folders.collectAsState()
    var naming by remember { mutableStateOf(false) }
    if (naming) {
        FolderNameDialog(
            title = stringResource(R.string.fork_folder_new),
            initial = "",
            onConfirm = { vm.createFolder(it, item); onDismiss() },
            onDismiss = onDismiss,
        )
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.fork_folder_add_title, item.displayLabel)) },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                folders.forEach { folder ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = { vm.addToFolder(folder, item); onDismiss() })
                            .padding(vertical = 10.dp),
                    ) {
                        FolderIcon(vm, folder, 32.dp)
                        Spacer(Modifier.width(14.dp))
                        Text(folder.displayLabel, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(onClick = { naming = true })
                        .padding(vertical = 10.dp),
                ) {
                    Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.create_new_folder_24px), contentDescription = null)
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(stringResource(R.string.fork_folder_new), style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } },
    )
}

/**
 * Tarjeta flotante con el contenido de una carpeta: cuadrícula de apps (toque abre,
 * mantener pulsado quita de la carpeta), título que se toca para renombrar y botón para eliminarla.
 */
@Composable
fun HomeFolderPopup(
    vm: MinimalHomeVM,
    folder: Tag?,
    insets: PaddingValues,
    onLaunch: (SavableSearchable) -> Unit,
    onDismiss: () -> Unit,
) {
    // Se recuerda la última carpeta para que la animación de salida aún tenga contenido.
    var shown by remember { mutableStateOf<Tag?>(null) }
    if (folder != null) shown = folder
    val visible = folder != null
    var renaming by rememberSaveable { mutableStateOf(false) }
    val view = LocalView.current

    AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            Modifier
                .bleed(insets)
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .combinedClickable(interactionSource = null, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(spring(dampingRatio = 0.8f, stiffness = 700f), initialScale = 0.85f, transformOrigin = TransformOrigin.Center) + fadeIn(),
                exit = scaleOut(targetScale = 0.92f) + fadeOut(),
            ) {
                val current = shown ?: return@AnimatedVisibility
                val items by remember(current.key) { vm.folderItems(current) }.collectAsState(emptyList())
                val iconPx = with(LocalDensity.current) { 48.dp.roundToPx() }
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 400.dp)
                        .fillMaxWidth()
                        .combinedClickable(interactionSource = null, indication = null, onClick = {}),
                ) {
                    Column(Modifier.padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                current.displayLabel,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .combinedClickable(interactionSource = null, indication = null, onClick = { renaming = true }),
                            )
                            IconButton(onClick = { renaming = true }) {
                                Icon(painterResource(R.drawable.edit_24px), contentDescription = stringResource(R.string.fork_folder_rename))
                            }
                            IconButton(onClick = { vm.deleteFolder(current); onDismiss() }) {
                                Icon(painterResource(R.drawable.delete_24px), contentDescription = stringResource(R.string.fork_folder_delete))
                            }
                        }
                        if (items.isEmpty()) {
                            Text(
                                stringResource(R.string.fork_folder_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, end = 12.dp),
                            )
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.padding(top = 8.dp, end = 12.dp).heightIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(items, key = { it.key }) { item ->
                                var menu by remember { mutableStateOf(false) }
                                Box {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = { onLaunch(item) },
                                                onLongClick = {
                                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                    menu = true
                                                },
                                            )
                                            .padding(vertical = 4.dp),
                                    ) {
                                        val icon by remember(item.key) { vm.icon(item, iconPx) }.collectAsState(null)
                                        ShapedLauncherIcon(size = 48.dp, icon = { icon })
                                        Text(
                                            item.displayLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 6.dp, start = 2.dp, end = 2.dp),
                                        )
                                    }
                                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                        DropdownMenuItem(
                                            leadingIcon = { Icon(painterResource(R.drawable.close_24px), contentDescription = null) },
                                            text = { Text(stringResource(R.string.fork_folder_remove_item)) },
                                            onClick = { menu = false; vm.removeFromFolder(current, item) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (renaming) {
                    FolderNameDialog(
                        title = stringResource(R.string.fork_folder_rename),
                        initial = current.displayLabel,
                        onConfirm = { name ->
                            renaming = false
                            vm.renameFolder(current, name)
                            onDismiss() // el nombre cambia la clave; se cierra para no mostrar la carpeta vieja
                        },
                        onDismiss = { renaming = false },
                    )
                }
            }
        }
    }
}

/**
 * Extiende el elemento por encima de los [insets] que aplica su contenedor, para que los
 * fondos de los popups cubran toda la pantalla (barra de estado y de navegación incluidas).
 */
fun Modifier.bleed(insets: PaddingValues): Modifier = layout { measurable, constraints ->
    val l = insets.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    val r = insets.calculateEndPadding(LayoutDirection.Ltr).roundToPx()
    val t = insets.calculateTopPadding().roundToPx()
    val b = insets.calculateBottomPadding().roundToPx()
    val w = constraints.maxWidth + l + r
    val h = constraints.maxHeight + t + b
    val placeable = measurable.measure(constraints.copy(minWidth = w, maxWidth = w, minHeight = h, maxHeight = h))
    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(-l, -t) }
}
