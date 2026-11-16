package dev.kbzon.launcher.ui.home

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val vm: MinimalHomeVM = viewModel()
    val context = LocalContext.current
    val view = LocalView.current

    val darkContent = LocalPreferDarkContentOverWallpaper.current
    val contentColor = if (darkContent) Color(0xE6000000) else Color.White
    val shadow = if (darkContent) null else Shadow(Color(0x66000000), blurRadius = 8f)
    val accent = MaterialTheme.colorScheme.primaryFixedDim

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
    LaunchedEffect(selectedLetter) { listState.scrollToItem(0) }

    val onLaunch: (SavableSearchable, IntRect?) -> Unit = { item, bounds ->
        if (vm.launch(context, item, bounds)) vm.selectLetter(null)
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .homeGestures(
                    splitSwipeDown = { vm.splitSwipeDown.value },
                    onSwipeDown = { leftHalf ->
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        if (leftHalf) vm.openNotifications() else vm.openQuickSettings()
                    },
                    onTwoFinger = { up ->
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        vm.runGestureAction(context, if (up) vm.twoFingerUp.value else vm.twoFingerDown.value)
                    },
                )
                .padding(insets)
        ) {
            // [fork] Pantallas anchas (Tab S9, plegables abiertos): dos columnas.
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val large = maxWidth >= 600.dp
                // Dos columnas solo en horizontal; la Tab en vertical usa una columna más grande.
                val wide = large && maxWidth > maxHeight
                val listMaxHeight = when {
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
                    GlanceLine(vm = vm, shadow = shadow)
                    val mediaPlayerEnabled by vm.mediaPlayerEnabled.collectAsState()
                    if (mediaPlayerEnabled) HomeMediaPlayer(shadow = shadow)
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
                                        style = TextStyle(
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
                                        style = TextStyle(fontSize = 15.sp, shadow = shadow),
                                        color = contentColor.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(bottom = 8.dp),
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
                                        listState = listState,
                                        onLaunch = onLaunch,
                                    )
                                }
                            }
                        }
                    }
                }
                val sidePadding = Modifier.padding(
                    start = if (railOnLeft) 56.dp else 28.dp,
                    end = if (railOnLeft) 28.dp else 56.dp,
                )
                if (wide) {
                    Row(Modifier.fillMaxSize().then(sidePadding).padding(horizontal = 24.dp)) {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            clockSection()
                        }
                        Spacer(Modifier.width(48.dp))
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            Spacer(Modifier.weight(1f))
                            listSection()
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize().then(sidePadding)) {
                        Spacer(Modifier.height(40.dp))
                        clockSection()
                        Spacer(Modifier.weight(1f))
                        listSection()
                        Spacer(Modifier.height(32.dp))
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
            if (railBothEdges) {
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
                    modifier = Modifier.align(if (railOnLeft) Alignment.CenterEnd else Alignment.CenterStart),
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
                modifier = Modifier
                    .align(if (railOnLeft) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 8.dp),
            )
        }
    }
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
                modifier = Modifier.animateItem(),
            )
        }
    }
}

/** Próximo evento · clima · próxima alarma, cada uno abre su app. */
@Composable
private fun GlanceLine(vm: MinimalHomeVM, shadow: Shadow?) {
    val context = LocalContext.current
    val event by vm.nextEvent.collectAsState()
    val upcoming by vm.upcomingEvents.collectAsState()
    val agenda by vm.agendaEnabled.collectAsState()
    val weatherApp by vm.weatherApp.collectAsState()
    val forecast by vm.weather.collectAsState()
    val alarm by remember { vm.nextAlarm(context) }.collectAsState(null)
    val battery by remember { vm.battery(context) }.collectAsState(null)
    val measurementSystem = LocalMeasurementSystem.current
    val style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, shadow = shadow)
    val dim = LocalContentColor.current.copy(alpha = 0.85f)

    Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val events = if (agenda) upcoming else listOfNotNull(event)
        events.forEach { ev ->
            GlanceItem(
                icon = R.drawable.event_24px,
                text = ev.glanceText(context),
                style = style,
                color = dim,
                onClick = { vm.launch(context, ev) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (forecast == null) {
                // Sin datos de clima: acceso directo para configurarlo (proveedor / ubicación).
                GlanceItem(
                    icon = R.drawable.light_mode_24px,
                    text = stringResource(R.string.fork_glance_setup_weather),
                    style = style,
                    color = dim.copy(alpha = 0.6f),
                    onClick = {
                        context.tryStart(
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
