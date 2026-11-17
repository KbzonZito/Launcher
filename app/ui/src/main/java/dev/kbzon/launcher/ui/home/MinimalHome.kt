package dev.kbzon.launcher.ui.home

import dev.kbzon.launcher.ui.theme.typography.fontfamily.LocalAppFontFamily
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.text.format.DateFormat
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import dev.kbzon.launcher.preferences.GestureAction
import dev.kbzon.launcher.preferences.home.HomeLayout
import dev.kbzon.launcher.ui.settings.SettingsActivity
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableFloatStateOf
import dev.kbzon.launcher.preferences.home.ActionButtonPosition
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntRect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.kbzon.launcher.notifications.Notification
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.CalendarEvent
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.Tag
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.ShapedLauncherIcon
import dev.kbzon.launcher.ui.launcher.sheets.LocalBottomSheetManager
import dev.kbzon.launcher.ui.locals.LocalMeasurementSystem
import dev.kbzon.launcher.ui.locals.LocalPreferDarkContentOverWallpaper
import dev.kbzon.launcher.ui.privacy.HiddenItemsLock
import dev.kbzon.launcher.ui.utils.formatTemperature
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale

/**
 * [fork] Home minimalista: reloj, línea de un vistazo, favoritos y riel del alfabeto.
 */
@Composable
fun MinimalHome(
    insets: PaddingValues,
    onActiveListState: (LazyListState) -> Unit = {},
    modifier: Modifier = Modifier,
    openSearch: () -> Unit = {},
    openWidgets: () -> Unit = {},
    homePresses: kotlinx.coroutines.flow.Flow<Unit> = kotlinx.coroutines.flow.emptyFlow(),
    onEditModeChange: (Boolean) -> Unit = {},
) {
    val vm: MinimalHomeVM = viewModel()
    val context = LocalContext.current
    val view = LocalView.current

    val darkContent = LocalPreferDarkContentOverWallpaper.current
    val contentColor = if (darkContent) Color(0xE6000000) else Color.White
    val shadow = if (darkContent) null else Shadow(Color(0x66000000), blurRadius = 8f)
    // Acento legible sobre el fondo de pantalla: el tono claro del primario sobre fondos oscuros y el oscuro
    // sobre fondos claros. (primaryFixedDim no lo define el tema heredado y quedaba sin especificar.)
    val scheme = MaterialTheme.colorScheme
    val accent = listOf(scheme.primary, scheme.inversePrimary).let { tones ->
        if (darkContent) tones.minBy { it.luminance() } else tones.maxBy { it.luminance() }
    }

    val appsByLetter by vm.appsByLetter.collectAsState()
    val favorites by vm.favorites.collectAsState()
    val selectedLetter by vm.selectedLetter.collectAsState()
    val showIcons by vm.showIcons.collectAsState()
    val railOnLeft by vm.railOnLeft.collectAsState()
    val frequentApps by vm.frequentApps.collectAsState()
    val hiddenApps by vm.hiddenApps.collectAsState()
    val notificationsByPackage by vm.notificationsByPackage.collectAsState()
    val notificationPreviews by vm.notificationPreviews.collectAsState()
    val hiddenUnlocked by HiddenItemsLock.unlocked.collectAsState()

    val usageSuggestions by vm.usageSuggestions.collectAsState()
    val railDoubleTapLock by vm.railDoubleTapLock.collectAsState()
    val waveIntensity by vm.waveIntensity.collectAsState()
    val railBothEdges by vm.railBothEdges.collectAsState()
    val railState = remember { RailState() }

    val railEntries = remember(appsByLetter.keys, hiddenApps.isNotEmpty(), usageSuggestions) {
        buildList {
            add(MinimalHomeVM.STAR)
            if (usageSuggestions) add(MinimalHomeVM.RECENT)
            addAll(appsByLetter.keys)
            if (hiddenApps.isNotEmpty()) add(MinimalHomeVM.HIDDEN)
        }
    }

    // Las ocultas solo se ven desbloqueadas; si el candado se cierra, se vuelve a favoritos.
    LaunchedEffect(hiddenUnlocked, selectedLetter, usageSuggestions) {
        if (selectedLetter == MinimalHomeVM.HIDDEN && !hiddenUnlocked) vm.selectLetter(null)
        if (selectedLetter == MinimalHomeVM.RECENT && !usageSuggestions) vm.selectLetter(null)
    }
    DisposableEffect(selectedLetter) {
        HiddenItemsLock.hiddenContentVisible = selectedLetter == MinimalHomeVM.HIDDEN
        onDispose { HiddenItemsLock.hiddenContentVisible = false }
    }

    BackHandler(enabled = selectedLetter != null) { vm.selectLetter(null) }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.selectLetter(null) }
    var reorderMode by remember { mutableStateOf(false) }
    LaunchedEffect(selectedLetter) { if (selectedLetter != null) reorderMode = false }

    var openFolder by remember { mutableStateOf<Tag?>(null) }
    val onLaunch: (SavableSearchable, IntRect?) -> Unit = { item, bounds ->
        if (item is Tag) openFolder = item
        else if (vm.launch(context, item, bounds)) vm.selectLetter(null)
    }

    // [fork] Fase 11: acciones (pasan por el scaffold cuando hace falta animarlas), diseño y menú rápido.
    val runAction: (GestureAction) -> Unit = { action ->
        when (action) {
            is GestureAction.Search -> openSearch()
            is GestureAction.Widgets -> openWidgets()
            else -> vm.runGestureAction(context, action)
        }
    }
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val storedLayout by remember(landscape) { vm.layout(landscape) }.collectAsState(HomeLayout())
    var editMode by remember { mutableStateOf(false) }
    var draftLayout by remember { mutableStateOf<HomeLayout?>(null) }
    val layout = draftLayout ?: storedLayout
    var quickMenuY by remember { mutableStateOf<Float?>(null) }
    var eventsPopup by remember { mutableStateOf(false) }
    var outerHeightPx by remember { mutableIntStateOf(1) }
    var outerWidthPx by remember { mutableIntStateOf(1) }
    val buttonPosition by vm.actionButtonPosition.collectAsState()
    // B9: mientras se edita, el scaffold no reacciona a gestos (lock/unlock).
    LaunchedEffect(editMode) { onEditModeChange(editMode) }
    val actionButtonEnabled by vm.actionButtonEnabled.collectAsState()
    val actionButtonActions by vm.actionButtonActions.collectAsState()
    val splitSwipeDown by vm.splitSwipeDown.collectAsState()
    val clockStyleNow by vm.clockStyle.collectAsState()
    // Botón de inicio estando en la home: volver a la vista principal (como Niagara).
    LaunchedEffect(homePresses) {
        homePresses.collect {
            vm.selectLetter(null)
            quickMenuY = null
            eventsPopup = false
            openFolder = null
            reorderMode = false
            if (editMode) { editMode = false; draftLayout = null }
        }
    }
    BackHandler(enabled = editMode || quickMenuY != null || eventsPopup || openFolder != null) {
        if (openFolder != null) openFolder = null else if (eventsPopup) eventsPopup = false else if (quickMenuY != null) quickMenuY = null else { editMode = false; draftLayout = null }
    }
    // B7: dónde va el botón según la posición elegida y el lado del alfabeto.
    val buttonAlign = when (buttonPosition) {
        ActionButtonPosition.RailSide -> if (railOnLeft) Alignment.BottomStart else Alignment.BottomEnd
        ActionButtonPosition.OppositeSide -> if (railOnLeft) Alignment.BottomEnd else Alignment.BottomStart
        ActionButtonPosition.Center -> Alignment.BottomCenter
    }
    // B6: si el botón queda a la izquierda o al centro, la lista le deja espacio abajo.
    val reserveForButton = actionButtonEnabled && buttonAlign != Alignment.BottomEnd && layout.buttonLift < 0.05f
    val listBottomSpace = if (reserveForButton) 104.dp else 32.dp
    fun updateDraft(transform: (HomeLayout) -> HomeLayout) {
        draftLayout = transform(draftLayout ?: storedLayout)
    }

    val iconStyle by vm.iconStyle.collectAsState()
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        dev.kbzon.launcher.ui.component.LocalIconGlyphColor provides
            if (iconStyle == dev.kbzon.launcher.preferences.home.HomeIconStyle.Original) null else contentColor,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { outerHeightPx = it.height.coerceAtLeast(1); outerWidthPx = it.width.coerceAtLeast(1) }
                .homeGestures(
                    splitSwipeDown = { vm.splitSwipeDown.value },
                    onSwipeDown = { leftHalf ->
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        if (leftHalf) vm.openNotifications() else vm.openQuickSettings()
                    },
                    onTwoFinger = { up ->
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        runAction(if (up) vm.twoFingerUp.value else vm.twoFingerDown.value)
                    },
                    enabled = { !editMode },
                    onLongPress = { pos ->
                        if (!editMode) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            quickMenuY = pos.y
                        }
                    },
                )
                .padding(insets)
        ) {
            // [fork] Pantallas anchas (Tab S9, plegables abiertos): dos columnas.
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val large = maxWidth >= 600.dp
                // Dos columnas solo en horizontal; la Tab en vertical usa una columna más grande.
                val wide = large && maxWidth > maxHeight
                // M1: con una letra elegida (o reordenando), la lista usa casi todo el alto.
                val focusedList = !wide && (selectedLetter != null || reorderMode)
                val listMaxHeight = when {
                    focusedList -> maxHeight * 0.82f
                    wide -> maxHeight * 0.8f
                    large -> maxHeight * 0.5f
                    else -> 460.dp
                }
                val clockScale = if (large) 1.5f else 1f
                val clockSection: @Composable () -> Unit = {
                    val clockStyle by vm.clockStyle.collectAsState()
                    val calendarApp by vm.calendarApp.collectAsState()
                    val clockApp by vm.clockApp.collectAsState()
                    HomeClock(
                        style = clockStyle,
                        shadow = shadow,
                        accent = accent,
                        onTimeClick = { clockApp?.let { vm.launch(context, it) } ?: context.openClockApp() },
                        onDateClick = { now ->
                            val app = calendarApp
                            if (app != null) vm.launch(context, app) else context.openCalendarAt(now)
                        },
                        scale = clockScale,
                    )
                    GlanceLine(vm = vm, shadow = shadow, onOpenEvents = { eventsPopup = true })
                    val mediaPlayerEnabled by vm.mediaPlayerEnabled.collectAsState()
                    if (mediaPlayerEnabled) HomeMediaPlayer(shadow = shadow)
                }
                val maxHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
                val clockBlock: @Composable () -> Unit = {
                    Column(
                        Modifier
                            .offset { IntOffset(0, (layout.clockOffset * maxHeightPx).roundToInt()) }
                            .then(
                                if (editMode) Modifier
                                    .border(1.5.dp, accent, RoundedCornerShape(16.dp))
                                    .padding(8.dp)
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures { change, dy ->
                                            change.consume()
                                            updateDraft { it.copy(clockOffset = (it.clockOffset + dy / maxHeightPx).coerceIn(-0.25f, 0.6f)) }
                                        }
                                    }
                                else Modifier
                            )
                    ) { clockSection() }
                }
                val listSection: @Composable () -> Unit = {
                    CompositionLocalProvider(LocalHomeListMaxHeight provides listMaxHeight) {
                        AnimatedContent(
                            targetState = selectedLetter,
                            transitionSpec = {
                                (fadeIn(spring(stiffness = 900f)) + slideInVertically(spring(dampingRatio = 0.85f, stiffness = 700f)) { it / 8 })
                                    .togetherWith(fadeOut(spring(stiffness = 1500f)) + slideOutVertically { -it / 12 })
                            },
                            contentAlignment = Alignment.BottomStart,
                            label = "homeList",
                        ) { letter ->
                            // B1: cada lista tiene su propio estado de scroll (antes se compartía entre la lista
                            // saliente y la entrante durante la transición, y los favoritos podían quedar desplazados).
                            val ownListState = rememberLazyListState()
                            if (letter == selectedLetter) SideEffect { onActiveListState(ownListState) }
                            val (header, items) = when (letter) {
                                null -> null to favorites
                                MinimalHomeVM.RECENT -> stringResource(R.string.fork_minimal_home_frequent) to frequentApps
                                MinimalHomeVM.HIDDEN -> stringResource(R.string.fork_minimal_home_hidden) to hiddenApps
                                else -> letter.toString() to appsByLetter[letter].orEmpty()
                            }
                            Column {
                                if (header != null) {
                                    Text(
                                        text = header,
                                        color = accent,
                                        style = TextStyle(fontFamily = LocalAppFontFamily.current,
                                            fontSize = if (header.length == 1) 44.sp else 28.sp,
                                            fontWeight = FontWeight.Light,
                                            shadow = shadow,
                                        ),
                                        modifier = Modifier.padding(bottom = 4.dp).combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { vm.selectLetter(null) },
                                        ),
                                    )
                                }
                                if (items.isEmpty()) {
                                    Text(
                                        text = stringResource(
                                            if (letter == null) R.string.fork_minimal_home_empty_favorites
                                            else R.string.fork_minimal_home_empty_section
                                        ),
                                        style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 15.sp, shadow = shadow),
                                        color = contentColor.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(bottom = 8.dp),
                                    )
                                } else if (letter == null && reorderMode) {
                                    HomeReorderFavorites(
                                        items = items,
                                        vm = vm,
                                        showIcons = showIcons,
                                        shadow = shadow,
                                        accent = accent,
                                        maxHeight = listMaxHeight,
                                        onDone = { vm.reorderFavorites(it); reorderMode = false },
                                    )
                                } else {
                                    HomeList(
                                        items = items,
                                        vm = vm,
                                        showIcons = showIcons,
                                        notifications = notificationsByPackage,
                                        showPreview = notificationPreviews,
                                        isHiddenSection = letter == MinimalHomeVM.HIDDEN,
                                        shadow = shadow,
                                        accent = accent,
                                        listState = ownListState,
                                        onLaunch = onLaunch,
                                        onReorderFavorites = if (letter == null) ({ reorderMode = true }) else null,
                                    )
                                }
                            }
                        }
                    }
                }
                // B3: margen simétrico del ancho del alfabeto, para deslizar cómodo en cualquier borde.
                val sidePadding = Modifier.padding(horizontal = 56.dp)
                if (wide) {
                    Row(Modifier.fillMaxSize().then(sidePadding).padding(horizontal = 24.dp)) {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            clockBlock()
                        }
                        Spacer(Modifier.width(48.dp))
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            Spacer(Modifier.weight(1f))
                            listSection()
                            Spacer(Modifier.height(listBottomSpace))
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize().then(sidePadding)) {
                        AnimatedVisibility(
                            visible = !focusedList,
                            enter = fadeIn(spring(stiffness = 600f)) + expandVertically(spring(dampingRatio = 0.9f, stiffness = 500f)),
                            exit = fadeOut(spring(stiffness = 900f)) + shrinkVertically(spring(dampingRatio = 0.9f, stiffness = 600f)),
                        ) {
                            Column {
                                Spacer(Modifier.height(40.dp))
                                clockBlock()
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        listSection()
                        Spacer(Modifier.height(listBottomSpace))
                    }
                }
            }

            val onRailLetter: (Char) -> Unit = { entry ->
                when (entry) {
                    MinimalHomeVM.STAR -> vm.selectLetter(null)
                    MinimalHomeVM.HIDDEN -> Unit // se resuelve al soltar, para no pedir la huella al pasar
                    else -> vm.selectLetter(entry)
                }
            }
            val onRailRelease: (Char) -> Unit = { entry ->
                if (entry == MinimalHomeVM.HIDDEN) {
                    HiddenItemsLock.requestUnlock { vm.selectLetter(MinimalHomeVM.HIDDEN) }
                }
            }
            val onRailDoubleTap: (() -> Unit)? = if (railDoubleTapLock) ({ vm.lockScreen(context) }) else null

            // Zona invisible en el borde contrario: el riel funciona desde ambos lados.
            if (railBothEdges && !editMode) {
                RailMirrorZone(
                    state = railState,
                    letters = railEntries,
                    onLetter = onRailLetter,
                    onRelease = onRailRelease,
                    onDoubleTap = onRailDoubleTap,
                    onDragStateChange = {},
                    accent = MaterialTheme.colorScheme.primary,
                    onAccent = MaterialTheme.colorScheme.onPrimary,
                    zoneOnLeft = !railOnLeft,
                    modifier = Modifier.align(BiasAlignment(if (railOnLeft) 1f else -1f, layout.railCenter * 2f - 1f)),
                    heightFraction = layout.railHeight,
                )
            }

            AlphabetRail(
                state = railState,
                letters = railEntries,
                selected = selectedLetter ?: MinimalHomeVM.STAR,
                onLetter = onRailLetter,
                onRelease = onRailRelease,
                onDoubleTap = onRailDoubleTap,
                onDragStateChange = {},
                color = contentColor,
                accent = accent,
                railOnLeft = railOnLeft,
                intensity = waveIntensity,
                heightFraction = layout.railHeight,
                bubbleColor = MaterialTheme.colorScheme.primary,
                onBubble = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(BiasAlignment(if (railOnLeft) -1f else 1f, layout.railCenter * 2f - 1f))
                    .padding(horizontal = 8.dp),
            )

            if (actionButtonEnabled && !editMode) {
                HomeActionButton(
                    actions = actionButtonActions,
                    onAction = runAction,
                    accent = accent,
                    modifier = Modifier
                        .align(buttonAlign)
                        .offset { IntOffset(0, -(layout.buttonLift * outerHeightPx).roundToInt()) }
                        // B8: más adentro, en la zona cómoda del pulgar.
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                )
            }
            if (actionButtonEnabled && editMode) {
                // Botón arrastrable en "Editar diseño": horizontal se acomoda a izquierda/centro/derecha, vertical lo sube.
                var dragX by remember { mutableFloatStateOf(0f) }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(buttonAlign)
                        .offset { IntOffset(dragX.roundToInt(), -(layout.buttonLift * outerHeightPx).roundToInt()) }
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                        .size(56.dp)
                        .border(1.5.dp, accent, CircleShape)
                        .pointerInput(railOnLeft, buttonPosition) {
                            detectDragGestures(
                                onDragEnd = {
                                    val base = when (buttonAlign) {
                                        Alignment.BottomStart -> 0f
                                        Alignment.BottomCenter -> 0.5f
                                        else -> 1f
                                    }
                                    val x = base + dragX / outerWidthPx
                                    val side = when {
                                        x < 0.33f -> 0 // izquierda
                                        x > 0.66f -> 2 // derecha
                                        else -> 1
                                    }
                                    vm.setActionButtonPosition(
                                        when (side) {
                                            1 -> ActionButtonPosition.Center
                                            0 -> if (railOnLeft) ActionButtonPosition.RailSide else ActionButtonPosition.OppositeSide
                                            else -> if (railOnLeft) ActionButtonPosition.OppositeSide else ActionButtonPosition.RailSide
                                        }
                                    )
                                    dragX = 0f
                                },
                            ) { change, amount ->
                                change.consume()
                                dragX += amount.x
                                updateDraft { it.copy(buttonLift = (it.buttonLift - amount.y / outerHeightPx).coerceIn(0f, 0.5f)) }
                            }
                        },
                ) {
                    Icon(painterResource(R.drawable.search_24px), contentDescription = null, tint = accent)
                }
            }

            if (editMode) {
                // Marco arrastrable sobre el alfabeto: mover (arrastrar) y cambiar el alto (asa inferior).
                Box(
                    Modifier
                        .align(BiasAlignment(if (railOnLeft) -1f else 1f, layout.railCenter * 2f - 1f))
                        .padding(horizontal = 4.dp)
                        .width(48.dp)
                        .railHeight(railEntries.size, layout.railHeight)
                        .border(1.5.dp, accent, RoundedCornerShape(16.dp))
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dy ->
                                change.consume()
                                updateDraft { it.copy(railCenter = (it.railCenter + dy / outerHeightPx).coerceIn(0.15f, 0.85f)) }
                            }
                        }
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 14.dp)
                            .size(width = 36.dp, height = 28.dp)
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { change, dy ->
                                    change.consume()
                                    updateDraft { it.copy(railHeight = (it.railHeight + dy / outerHeightPx).coerceIn(0.3f, 0.9f)) }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(width = 28.dp, height = 6.dp).background(accent, RoundedCornerShape(3.dp)))
                    }
                }
                // B11: barra superior compacta; la pista se oculta sola.
                var showHint by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) { kotlinx.coroutines.delay(3000); showHint = false }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                        AnimatedVisibility(showHint) {
                            Text(
                                stringResource(R.string.fork_edit_layout_hint_short),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 10.dp),
                            )
                        }
                        TextButton(onClick = { draftLayout = HomeLayout() }) { Text(stringResource(R.string.fork_edit_layout_reset)) }
                        TextButton(onClick = {
                            draftLayout?.let { vm.saveLayout(landscape, it) }
                            draftLayout = null
                            editMode = false
                        }) { Text(stringResource(R.string.fork_edit_layout_done)) }
                    }
                }
            }

            val eventsAhead by vm.eventsAhead.collectAsState()
            HomeFolderPopup(
                vm = vm,
                folder = openFolder,
                insets = insets,
                onLaunch = { item -> openFolder = null; vm.launch(context, item) },
                onDismiss = { openFolder = null },
            )
            HomeEventsPopup(
                visible = eventsPopup,
                events = eventsAhead,
                onEvent = { ev -> eventsPopup = false; vm.launch(context, ev) },
                onDismiss = { eventsPopup = false },
                insets = insets,
            )

            HomeQuickMenu(
                visible = quickMenuY != null,
                anchorY = quickMenuY ?: 0f,
                clockStyle = clockStyleNow,
                onClockStyle = vm::setClockStyle,
                railOnLeft = railOnLeft,
                onRailOnLeft = vm::setRailOnLeft,
                splitSwipeDown = splitSwipeDown,
                onSplitSwipeDown = vm::setSplitSwipeDown,
                showIcons = showIcons,
                onShowIcons = vm::setShowIcons,
                actionButton = actionButtonEnabled,
                onActionButton = vm::setActionButton,
                onEditLayout = { quickMenuY = null; editMode = true; draftLayout = storedLayout },
                onGestures = { quickMenuY = null; context.openSettings(SettingsActivity.ROUTE_GESTURES) },
                onSettings = { quickMenuY = null; context.openSettings(null) },
                onDismiss = { quickMenuY = null },
            )
        }
    }
}

private fun Context.openSettings(route: String?) {
    val intent = Intent(this, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (route != null) intent.putExtra(SettingsActivity.EXTRA_ROUTE, route)
    try { startActivity(intent) } catch (_: Exception) { }
}

@Composable
private fun HomeList(
    items: List<SavableSearchable>,
    vm: MinimalHomeVM,
    showIcons: Boolean,
    notifications: Map<String, List<Notification>>,
    showPreview: Boolean,
    isHiddenSection: Boolean,
    shadow: Shadow?,
    accent: Color,
    listState: LazyListState,
    onLaunch: (SavableSearchable, IntRect?) -> Unit,
    onReorderFavorites: (() -> Unit)? = null,
) {
    val swipeLeftTargets by vm.swipeLeftTargets.collectAsState()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth().heightIn(max = LocalHomeListMaxHeight.current),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(items, key = { it.key }) { item ->
            HomeAppRow(
                item = item,
                vm = vm,
                showIcons = showIcons,
                notifications = (item as? Application)?.let { notifications[it.componentName.packageName] }.orEmpty(),
                showPreview = showPreview,
                isHiddenItem = isHiddenSection,
                shadow = shadow,
                accent = accent,
                onLaunch = onLaunch,
                swipeLeftTarget = swipeLeftTargets[item.key],
                onReorderFavorites = onReorderFavorites,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

/** Próximo evento · clima · próxima alarma, cada uno abre su app. */
@Composable
private fun GlanceLine(vm: MinimalHomeVM, shadow: Shadow?, onOpenEvents: () -> Unit) {
    val context = LocalContext.current
    val ahead by vm.eventsAhead.collectAsState()
    val hasLocation by vm.hasLocationPermission.collectAsState()
    val audioDevice by remember { vm.bluetoothAudioDevice(context) }.collectAsState(null)
    val audioAppsEnabled by vm.audioMediaAppsEnabled.collectAsState()
    val mediaApps by vm.mediaApps.collectAsState()
    val weatherApp by vm.weatherApp.collectAsState()
    val forecast by vm.weather.collectAsState()
    val alarm by remember { vm.nextAlarm(context) }.collectAsState(null)
    val battery by remember { vm.battery(context) }.collectAsState(null)
    val measurementSystem = LocalMeasurementSystem.current
    val style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 14.sp, fontWeight = FontWeight.Medium, shadow = shadow)
    val dim = LocalContentColor.current.copy(alpha = 0.85f)

    Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // M2: próximo evento; al tocarlo, tarjeta con los de hoy y mañana.
        val next = ahead.firstOrNull { !it.allDay } ?: ahead.firstOrNull()
        next?.let { ev ->
            val more = ahead.size - 1
            GlanceItem(
                icon = R.drawable.event_24px,
                text = ev.glanceText(context) + if (more > 0) "   " + stringResource(R.string.fork_glance_more_events, more) else "",
                style = style,
                color = dim,
                onClick = onOpenEvents,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (forecast == null) {
                // Sin datos de clima: acceso directo para configurarlo (proveedor / ubicación).
                // B12: sin permiso de ubicación no hay datos; pedirlo aquí mismo.
                GlanceItem(
                    icon = R.drawable.light_mode_24px,
                    text = stringResource(if (hasLocation) R.string.fork_glance_setup_weather else R.string.fork_glance_allow_location),
                    style = style,
                    color = dim.copy(alpha = 0.6f),
                    onClick = {
                        if (!hasLocation) vm.requestLocationPermission(context) else context.tryStart(
                            Intent().setClassName(context, "dev.kbzon.launcher.ui.settings.SettingsActivity")
                                .putExtra(dev.kbzon.launcher.ui.settings.SettingsActivity.EXTRA_ROUTE, dev.kbzon.launcher.ui.settings.SettingsActivity.ROUTE_WEATHER_INTEGRATION)
                        )
                    },
                )
            }
            forecast?.let { f ->
                GlanceItem(
                    icon = null,
                    text = "${formatTemperature(context, f.temperature.toFloat(), measurementSystem)} ${f.condition}",
                    style = style,
                    color = dim,
                    onClick = { weatherApp?.let { vm.launch(context, it) } ?: context.openWeatherApp() },
                )
            }
            // Batería: solo cuando importa (cargando o por debajo del 20 %).
            battery?.takeIf { it.charging || it.percent <= 20 }?.let { b ->
                GlanceItem(
                    icon = if (b.charging) R.drawable.battery_charging_50_20px else R.drawable.battery_1_bar_24px,
                    text = "${b.percent}%",
                    style = style,
                    color = if (!b.charging && b.percent <= 20) MaterialTheme.colorScheme.error else dim,
                    onClick = { context.tryStart(Intent(Intent.ACTION_POWER_USAGE_SUMMARY)) },
                )
            }
            alarm?.takeIf { it - System.currentTimeMillis() < 24 * 60 * 60 * 1000L }?.let { t ->
                GlanceItem(
                    icon = R.drawable.alarm_24px,
                    text = DateFormat.getTimeFormat(context).format(Date(t)),
                    style = style,
                    color = dim,
                    onClick = { context.tryStart(Intent(AlarmClock.ACTION_SHOW_ALARMS)) },
                )
            }
        }
        // B14: audio Bluetooth conectado → acceso a las apps de música.
        if (audioAppsEnabled && audioDevice != null && mediaApps.isNotEmpty()) {
            val iconPx = with(LocalDensity.current) { 28.dp.roundToPx() }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(painterResource(R.drawable.headphones_24px), contentDescription = null, tint = dim, modifier = Modifier.size(16.dp))
                Text(audioDevice.orEmpty(), style = style, color = dim, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 140.dp))
                mediaApps.forEach { app ->
                    val icon by remember(app.key) { vm.icon(app, iconPx) }.collectAsState(null)
                    Box(Modifier.combinedClickable(interactionSource = null, indication = null, onClick = { vm.launch(context, app) })) {
                        ShapedLauncherIcon(size = 28.dp, icon = { icon })
                    }
                }
            }
        }
    }
}

@Composable
private fun GlanceItem(icon: Int?, text: String, style: TextStyle, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = style, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun CalendarEvent.glanceText(context: Context): String {
    val start = startTime
    val time = when {
        allDay || start == null -> ""
        else -> {
            val t = DateFormat.getTimeFormat(context).format(Date(start))
            if (android.text.format.DateUtils.isToday(start)) t
            else "${DateFormat.format("EEE", start)} $t"
        }
    }
    return listOf(time, labelOverride ?: label).filter { it.isNotBlank() }.joinToString("  ")
}

private fun Context.tryStart(intent: Intent) {
    try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
    }
}

private fun Context.openCalendarAt(time: Long) {
    val uri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").let {
        ContentUris.appendId(it, time)
    }.build()
    tryStart(Intent(Intent.ACTION_VIEW, uri))
}

/** Abre la app de reloj completa (la que atiende las alarmas), no solo la pantalla de alarmas. */
private fun Context.openClockApp() {
    val handler = packageManager.resolveActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS), 0)?.activityInfo?.packageName
    val launch = handler?.let { packageManager.getLaunchIntentForPackage(it) }
    tryStart(launch ?: Intent(AlarmClock.ACTION_SHOW_ALARMS))
}

/** Abre la app de clima instalada (Samsung, Breezy, Google), en ese orden. Configurable en la fase 6. */
private fun Context.openWeatherApp() {
    val candidates = listOf("com.sec.android.daemonapp", "org.breezyweather", "com.google.android.apps.weather")
    for (pkg in candidates) {
        val intent = packageManager.getLaunchIntentForPackage(pkg) ?: continue
        tryStart(intent)
        return
    }
}

/** Alto máximo de la lista de la home (más alto en pantallas anchas). */
private val LocalHomeListMaxHeight = staticCompositionLocalOf { 460.dp }
