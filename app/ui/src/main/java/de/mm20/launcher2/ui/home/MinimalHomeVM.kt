package de.mm20.launcher2.ui.home

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.ActivityOptionsCompat
import androidx.compose.ui.unit.IntRect
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mm20.launcher2.appshortcuts.AppShortcutRepository
import de.mm20.launcher2.calendar.CalendarRepository
import de.mm20.launcher2.globalactions.GlobalActionsService
import de.mm20.launcher2.preferences.GestureAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import de.mm20.launcher2.notifications.Notification
import de.mm20.launcher2.notifications.NotificationRepository
import de.mm20.launcher2.search.AppShortcut
import de.mm20.launcher2.icons.IconService
import de.mm20.launcher2.icons.LauncherIcon
import de.mm20.launcher2.permissions.PermissionGroup
import de.mm20.launcher2.permissions.PermissionsManager
import de.mm20.launcher2.preferences.home.MinimalClockStyle
import de.mm20.launcher2.preferences.home.MinimalHomeSettings
import de.mm20.launcher2.search.Application
import de.mm20.launcher2.search.CalendarEvent
import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.search.SearchService
import de.mm20.launcher2.searchable.PinnedLevel
import de.mm20.launcher2.searchable.SavableSearchableRepository
import de.mm20.launcher2.searchable.VisibilityLevel
import de.mm20.launcher2.services.favorites.FavoritesService
import de.mm20.launcher2.weather.Forecast
import de.mm20.launcher2.weather.WeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.Collator
import java.text.Normalizer

/**
 * [fork] Estado de la home minimalista: favoritos, apps agrupadas por letra y la "línea de un vistazo".
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MinimalHomeVM : ViewModel(), KoinComponent {
    private val searchService: SearchService by inject()
    private val searchableRepository: SavableSearchableRepository by inject()
    private val favoritesService: FavoritesService by inject()
    private val iconService: IconService by inject()
    private val calendarRepository: CalendarRepository by inject()
    private val weatherRepository: WeatherRepository by inject()
    private val permissionsManager: PermissionsManager by inject()
    private val homeSettings: MinimalHomeSettings by inject()
    private val notificationRepository: NotificationRepository by inject()
    private val appShortcutRepository: AppShortcutRepository by inject()
    private val globalActions: GlobalActionsService by inject()

    val showIcons = homeSettings.showIcons.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val railOnLeft = homeSettings.railOnLeft.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val notificationPreviews = homeSettings.notificationPreviews.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val splitSwipeDown = homeSettings.splitSwipeDown.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val twoFingerUp = homeSettings.twoFingerUp.stateIn(viewModelScope, SharingStarted.Eagerly, GestureAction.NoAction)
    val twoFingerDown = homeSettings.twoFingerDown.stateIn(viewModelScope, SharingStarted.Eagerly, GestureAction.NoAction)

    /** Destino al deslizar cada app a la izquierda (clave de app → destino resuelto). */
    val swipeLeftTargets: StateFlow<Map<String, SavableSearchable>> = homeSettings.appSwipeLeft
        .flatMapLatest { map ->
            if (map.isEmpty()) flowOf(emptyMap())
            else searchableRepository.getByKeys(map.values.distinct()).map { targets ->
                val byKey = targets.associateBy { it.key }
                map.mapNotNull { (app, target) -> byKey[target]?.let { app to it } }.toMap()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun setSwipeLeftTarget(app: SavableSearchable, target: SavableSearchable?) {
        // Guardarlo en la base de búsqueda para poder resolverlo luego por su clave.
        target?.let { searchableRepository.insert(it) }
        homeSettings.setAppSwipeLeft(app.key, target?.key)
    }

    fun openNotifications() = globalActions.openNotificationDrawer()
    fun openQuickSettings() = globalActions.openQuickSettings()

    /** Ejecuta una acción de gesto desde la home (sin pasar por el scaffold). */
    fun runGestureAction(context: Context, action: GestureAction) {
        when (action) {
            is GestureAction.Notifications -> globalActions.openNotificationDrawer()
            is GestureAction.QuickSettings -> globalActions.openQuickSettings()
            is GestureAction.ScreenLock -> globalActions.lockScreen()
            is GestureAction.PowerMenu -> globalActions.openPowerDialog()
            is GestureAction.Recents -> globalActions.openRecents()
            is GestureAction.Search -> context.startActivity(
                Intent(Intent.ACTION_ASSIST)
                    .setClassName(context, "de.mm20.launcher2.ui.assistant.AssistantActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            is GestureAction.Launch -> {
                val key = action.key ?: return
                viewModelScope.launch {
                    searchableRepository.getByKeys(listOf(key)).first().firstOrNull()?.let { launch(context, it) }
                }
            }
            else -> Unit
        }
    }

    private val allApps = searchService.getAllApps()
    private val hiddenAppKeys = searchableRepository.getKeys(
        maxVisibility = VisibilityLevel.SearchOnly,
        includeTypes = listOf("app"),
    )

    /** Apps visibles (personal + trabajo), sin las ocultas, ordenadas alfabéticamente. */
    val apps: StateFlow<List<Application>> = combine(allApps, hiddenAppKeys) { all, hiddenKeys ->
        val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
        (all.standardProfileApps + all.workProfileApps)
            .filterNot { hiddenKeys.contains(it.key) }
            .sortedWith { a, b -> collator.compare(a.displayLabel, b.displayLabel) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Apps agrupadas por su letra índice, en orden. */
    val appsByLetter: StateFlow<Map<Char, List<Application>>> = apps
        .map { list -> list.groupBy { indexLetter(it.displayLabel) }.toSortedMap(letterOrder) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Favoritos fijados por el usuario. */
    val favorites: StateFlow<List<SavableSearchable>> = favoritesService.getFavorites(
        minPinnedLevel = PinnedLevel.AutomaticallySorted,
        limit = 20,
    ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Apps ocultas (solo se muestran tras desbloquear). */
    val hiddenApps: StateFlow<List<Application>> = combine(allApps, hiddenAppKeys) { all, hiddenKeys ->
        val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
        // Ocultas de cualquier perfil + todas las del Espacio privado de Android (siempre tras el candado).
        ((all.standardProfileApps + all.workProfileApps).filter { hiddenKeys.contains(it.key) } + all.privateSpaceApps)
            .distinctBy { it.key }
            .sortedWith { a, b -> collator.compare(a.displayLabel, b.displayLabel) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Apps usadas con más frecuencia (aprendidas por Kvaesitso; vacío en modo incógnito si no hay historial). */
    val frequentApps: StateFlow<List<SavableSearchable>> = favoritesService.getFavorites(
        includeTypes = listOf("app"),
        minPinnedLevel = PinnedLevel.FrequentlyUsed,
        maxPinnedLevel = PinnedLevel.FrequentlyUsed,
        limit = 10,
    ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Notificaciones visibles agrupadas por paquete, la más reciente primero. */
    val notificationsByPackage: StateFlow<Map<String, List<Notification>>> = notificationRepository.notifications
        .map { list ->
            list.filter { !it.isGroupSummary && (it.title != null || it.text != null) }
                .sortedByDescending { it.postTime }
                .groupBy { it.packageName }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun shortcuts(app: Application): Flow<List<AppShortcut>> = appShortcutRepository.findMany(
        componentName = app.componentName,
        user = app.user,
        manifest = true,
        dynamic = true,
        limit = 5,
    )

    fun isPinned(item: SavableSearchable): Flow<Boolean> = favoritesService.isPinned(item)
    fun pin(item: SavableSearchable) = favoritesService.pinItem(item)
    fun unpin(item: SavableSearchable) = favoritesService.unpinItem(item)
    fun hide(item: SavableSearchable) = favoritesService.setVisibility(item, VisibilityLevel.Hidden)
    fun unhide(item: SavableSearchable) = favoritesService.setVisibility(item, VisibilityLevel.Default)

    /** Letra seleccionada en el riel; null = se muestran los favoritos. */
    val selectedLetter = MutableStateFlow<Char?>(null)

    fun selectLetter(letter: Char?) {
        selectedLetter.value = letter
    }

    val clockStyle = homeSettings.clockStyle.stateIn(viewModelScope, SharingStarted.Eagerly, MinimalClockStyle.Thin)
    val agendaEnabled = homeSettings.agenda.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val mediaPlayerEnabled = homeSettings.mediaPlayer.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private fun resolveKey(keyFlow: Flow<String?>): StateFlow<SavableSearchable?> = keyFlow
        .flatMapLatest { key ->
            if (key == null) flowOf(null)
            else searchableRepository.getByKeys(listOf(key)).map { it.firstOrNull() }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Apps que abren el clima y la fecha (null = automático). */
    val weatherApp: StateFlow<SavableSearchable?> = resolveKey(homeSettings.weatherAppKey)
    val calendarApp: StateFlow<SavableSearchable?> = resolveKey(homeSettings.calendarAppKey)
    val clockApp: StateFlow<SavableSearchable?> = resolveKey(homeSettings.clockAppKey)

    fun setClockApp(app: SavableSearchable?) {
        app?.let { searchableRepository.insert(it) }
        homeSettings.setClockAppKey(app?.key)
    }

    val usageSuggestions = homeSettings.usageSuggestions.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val railDoubleTapLock = homeSettings.railDoubleTapLock.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val waveIntensity = homeSettings.waveIntensity.stateIn(viewModelScope, SharingStarted.Eagerly, de.mm20.launcher2.preferences.home.WaveIntensity.Normal)
    val railBothEdges = homeSettings.railBothEdges.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /**
     * Apaga la pantalla. Necesita el servicio de accesibilidad del launcher; si no está activo,
     * avisa y abre el ajuste.
     */
    fun lockScreen(context: Context) {
        if (permissionsManager.checkPermissionOnce(PermissionGroup.Accessibility)) {
            globalActions.lockScreen()
        } else {
            android.widget.Toast.makeText(context, de.mm20.launcher2.ui.R.string.fork_lock_needs_accessibility, android.widget.Toast.LENGTH_LONG).show()
            (context as? androidx.appcompat.app.AppCompatActivity)?.let {
                permissionsManager.requestPermission(it, PermissionGroup.Accessibility)
            }
        }
    }

    fun setWeatherApp(app: SavableSearchable?) {
        app?.let { searchableRepository.insert(it) }
        homeSettings.setWeatherAppKey(app?.key)
    }

    fun setCalendarApp(app: SavableSearchable?) {
        app?.let { searchableRepository.insert(it) }
        homeSettings.setCalendarAppKey(app?.key)
    }

    /** Próximos eventos (hasta 3) en las siguientes 36 h, incluidos los de todo el día de hoy. */
    val upcomingEvents: StateFlow<List<CalendarEvent>> = permissionsManager
        .hasPermission(PermissionGroup.Calendar)
        .flatMapLatest { granted ->
            if (!granted) flowOf(emptyList())
            else {
                val now = System.currentTimeMillis()
                calendarRepository.findMany(from = now, to = now + 36 * 60 * 60 * 1000L)
                    .map { events ->
                        events
                            .filter { it.isCompleted != true && it.endTime > now }
                            .sortedWith(compareBy<CalendarEvent> { !it.allDay }.thenBy { it.startTime ?: it.endTime })
                            .take(3)
                    }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nextEvent: StateFlow<CalendarEvent?> = permissionsManager
        .hasPermission(PermissionGroup.Calendar)
        .flatMapLatest { granted ->
            if (!granted) flowOf(null)
            else {
                val now = System.currentTimeMillis()
                calendarRepository.findMany(from = now, to = now + 24 * 60 * 60 * 1000L)
                    .map { events ->
                        events
                            .filter { it.isCompleted != true && !it.allDay && (it.endTime > now) }
                            .minByOrNull { it.startTime ?: it.endTime }
                    }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val weather: StateFlow<Forecast?> = weatherRepository.getForecasts(limit = 1)
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun nextAlarm(context: Context): Flow<Long?> = callbackFlow {
        val alarmManager: AlarmManager = context.getSystemService() ?: run {
            trySend(null); awaitClose { }; return@callbackFlow
        }
        trySend(alarmManager.nextAlarmClock?.triggerTime)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(alarmManager.nextAlarmClock?.triggerTime)
            }
        }
        context.registerReceiver(receiver, IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED))
        awaitClose { context.unregisterReceiver(receiver) }
    }

    data class BatteryState(val percent: Int, val charging: Boolean)

    /** Batería del teléfono (sin permisos: intent "sticky" ACTION_BATTERY_CHANGED). */
    fun battery(context: Context): Flow<BatteryState?> = callbackFlow {
        fun parse(intent: Intent?): BatteryState? {
            intent ?: return null
            val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100)
            val status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
            if (level < 0 || scale <= 0) return null
            val charging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == android.os.BatteryManager.BATTERY_STATUS_FULL
            return BatteryState(level * 100 / scale, charging)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(parse(intent))
            }
        }
        trySend(parse(context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))))
        awaitClose { context.unregisterReceiver(receiver) }
    }

    fun icon(searchable: SavableSearchable, size: Int): Flow<LauncherIcon?> =
        iconService.getIcon(searchable, size)

    fun launch(context: Context, searchable: SavableSearchable, bounds: IntRect? = null): Boolean {
        // La app se abre expandiéndose desde la fila tocada (si se conocen sus límites).
        val view = (context as? android.app.Activity)?.window?.decorView
        val options = if (bounds != null && view != null) {
            ActivityOptionsCompat.makeScaleUpAnimation(view, bounds.left, bounds.top, bounds.width, bounds.height)
        } else {
            ActivityOptionsCompat.makeBasic()
        }.toBundle()
        return if (searchable.launch(context, options)) {
            favoritesService.reportLaunch(searchable)
            true
        } else false
    }

    companion object {
        /** Entradas especiales del riel. */
        const val STAR = '★'
        const val RECENT = '◷'
        const val HIDDEN = '\uE000' // se dibuja como un candado

        /** '#' para todo lo que no empieza con letra; las letras con tilde van con su letra base (Á → A, Ñ queda Ñ). */
        fun indexLetter(label: String): Char {
            val first = label.trimStart().firstOrNull() ?: return '#'
            if (first == 'ñ' || first == 'Ñ') return 'Ñ'
            val base = Normalizer.normalize(first.toString(), Normalizer.Form.NFD).first().uppercaseChar()
            return if (base.isLetter()) base else '#'
        }

        private val letterOrder = Comparator<Char> { a, b ->
            when {
                a == b -> 0
                a == '#' -> 1
                b == '#' -> -1
                else -> Collator.getInstance().compare(a.toString(), b.toString())
            }
        }
    }
}

internal val SavableSearchable.displayLabel: String
    get() = labelOverride ?: label
