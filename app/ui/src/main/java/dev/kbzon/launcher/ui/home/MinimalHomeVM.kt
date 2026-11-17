package dev.kbzon.launcher.ui.home

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
import dev.kbzon.launcher.appshortcuts.AppShortcutRepository
import dev.kbzon.launcher.calendar.CalendarRepository
import dev.kbzon.launcher.globalactions.GlobalActionsService
import dev.kbzon.launcher.preferences.GestureAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import dev.kbzon.launcher.notifications.Notification
import dev.kbzon.launcher.notifications.NotificationRepository
import dev.kbzon.launcher.search.AppShortcut
import dev.kbzon.launcher.icons.IconService
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.home.MinimalClockStyle
import dev.kbzon.launcher.preferences.home.MinimalHomeSettings
import dev.kbzon.launcher.preferences.home.LetterSort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.CalendarEvent
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.SearchService
import dev.kbzon.launcher.search.Tag
import dev.kbzon.launcher.services.tags.TagsService
import dev.kbzon.launcher.searchable.PinnedLevel
import dev.kbzon.launcher.searchable.SavableSearchableRepository
import dev.kbzon.launcher.searchable.VisibilityLevel
import dev.kbzon.launcher.services.favorites.FavoritesService
import dev.kbzon.launcher.weather.Forecast
import dev.kbzon.launcher.weather.WeatherRepository
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
    private val context: Context by inject()
    private val notificationRepository: NotificationRepository by inject()
    private val appShortcutRepository: AppShortcutRepository by inject()
    private val globalActions: GlobalActionsService by inject()
    private val musicService: dev.kbzon.launcher.music.MusicService by inject()

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
                    .setClassName(context, "dev.kbzon.launcher.ui.assistant.AssistantActivity")
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
    val letterSort = homeSettings.letterSort.stateIn(viewModelScope, SharingStarted.Eagerly, LetterSort.Alphabetical)
    fun setLetterSort(value: LetterSort) = homeSettings.setLetterSort(value)
    val iconStyle = homeSettings.iconStyle.stateIn(viewModelScope, SharingStarted.Eagerly, dev.kbzon.launcher.preferences.home.HomeIconStyle.Original)

    /**
     * Apps agrupadas por su letra índice, en orden. Dentro de cada letra se ordenan según
     * [letterSort]; el orden alfabético de [apps] desempata (los sort de Kotlin son estables).
     */
    val appsByLetter: StateFlow<Map<Char, List<Application>>> = combine(apps, homeSettings.letterSort) { a, b -> a to b }
        .flatMapLatest { (list, sort) ->
            val keys = list.map { it.key }
            when (sort) {
                LetterSort.Alphabetical -> flowOf(list)
                LetterSort.MostUsed -> searchableRepository.getLaunchCounts(keys)
                    .map { counts -> list.sortedByDescending { counts[it.key] ?: 0 } }
                LetterSort.Recent -> searchableRepository.getWeights(keys)
                    .map { weights -> list.sortedByDescending { weights[it.key] ?: 0.0 } }
                LetterSort.Newest -> flow {
                    emit(list.sortedByDescending { installTime(it.componentName.packageName) })
                }.flowOn(Dispatchers.IO)
            }
        }
        .map { list -> list.groupBy { indexLetter(it.displayLabel) }.toSortedMap(letterOrder) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val installTimes = java.util.concurrent.ConcurrentHashMap<String, Long>()
    private fun installTime(pkg: String): Long = installTimes.getOrPut(pkg) {
        try {
            context.packageManager.getPackageInfo(pkg, 0).firstInstallTime
        } catch (e: Exception) {
            0L // p. ej. apps del perfil de trabajo que no están en el perfil personal
        }
    }

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
    // B2: los favoritos de la home tienen orden manual (no se reordenan por uso).
    fun pin(item: SavableSearchable) {
        val current = favorites.value
        if (current.any { it.key == item.key }) return
        favoritesService.updateFavorites(manuallySorted = current + item, automaticallySorted = emptyList())
    }

    fun unpin(item: SavableSearchable) {
        favoritesService.updateFavorites(
            manuallySorted = favorites.value.filterNot { it.key == item.key },
            automaticallySorted = emptyList(),
        )
    }

    /** Guarda un nuevo orden de favoritos (reordenar arrastrando). */
    fun reorderFavorites(ordered: List<SavableSearchable>) {
        favoritesService.updateFavorites(manuallySorted = ordered, automaticallySorted = emptyList())
    }

    init {
        // Migración única: los favoritos fijados antes quedaban en "orden automático" (por uso).
        // Se pasan a orden manual conservando el orden que se veía.
        val prefs = context.getSharedPreferences("fork_migrations", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("favorites_manual_v1", false)) {
            viewModelScope.launch {
                val current = favoritesService.getFavorites(
                    minPinnedLevel = PinnedLevel.AutomaticallySorted,
                    limit = 20,
                ).first()
                if (current.isNotEmpty()) {
                    favoritesService.updateFavorites(manuallySorted = current, automaticallySorted = emptyList())
                }
                prefs.edit().putBoolean("favorites_manual_v1", true).apply()
            }
        }
    }
    fun hide(item: SavableSearchable) = favoritesService.setVisibility(item, VisibilityLevel.Hidden)
    fun unhide(item: SavableSearchable) = favoritesService.setVisibility(item, VisibilityLevel.Default)

    // ---- Fase 12: carpetas. Una carpeta es una etiqueta de Kvaesitso fijada en favoritos. ----
    private val tagsService: TagsService by inject()

    /** Carpetas que hay en favoritos, en el orden en que aparecen. */
    val folders: StateFlow<List<Tag>> = favorites
        .map { list -> list.filterIsInstance<Tag>() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Contenido de una carpeta, ordenado alfabéticamente (las etiquetas no guardan orden). */
    fun folderItems(folder: Tag): Flow<List<SavableSearchable>> = tagsService.getTaggedItems(folder.tag)
        .map { items ->
            val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
            items.sortedWith { a, b -> collator.compare(a.displayLabel, b.displayLabel) }
        }

    private suspend fun uniqueFolderName(name: String): String {
        val existing = tagsService.getAllTags().first().toSet()
        val base = name.trim().ifEmpty { "Carpeta" }
        if (base !in existing) return base
        var n = 2
        while ("$base $n" in existing) n++
        return "$base $n"
    }

    /**
     * Crea una carpeta con [item] dentro. Si [item] era favorito, la carpeta ocupa su lugar;
     * si no, se añade al final de los favoritos.
     */
    fun createFolder(name: String, item: SavableSearchable) = viewModelScope.launch {
        val folder = Tag(uniqueFolderName(name))
        tagsService.createTag(folder.tag, listOf(item))
        val current = favorites.value
        val index = current.indexOfFirst { it.key == item.key }
        val updated = if (index >= 0) current.toMutableList().also { it[index] = folder } else current + folder
        favoritesService.updateFavorites(manuallySorted = updated, automaticallySorted = emptyList())
    }

    /** Mete [item] en [folder]; si estaba en favoritos, sale de ahí (ya está en la carpeta). */
    fun addToFolder(folder: Tag, item: SavableSearchable) = viewModelScope.launch {
        val items = tagsService.getTaggedItems(folder.tag).first()
        if (items.none { it.key == item.key }) {
            tagsService.updateTag(folder.tag, items = items + item)
        }
        if (favorites.value.any { it.key == item.key }) unpin(item)
    }

    fun removeFromFolder(folder: Tag, item: SavableSearchable) = viewModelScope.launch {
        val items = tagsService.getTaggedItems(folder.tag).first()
        tagsService.updateTag(folder.tag, items = items.filterNot { it.key == item.key })
    }

    fun renameFolder(folder: Tag, newName: String) = viewModelScope.launch {
        val name = newName.trim()
        if (name.isEmpty() || name == folder.tag) return@launch
        tagsService.updateTag(folder.tag, newName = uniqueFolderName(name))
    }

    /** Elimina la carpeta y devuelve sus apps a favoritos, en el lugar que ocupaba. */
    fun deleteFolder(folder: Tag) = viewModelScope.launch {
        val items = tagsService.getTaggedItems(folder.tag).first()
        val current = favorites.value
        val index = current.indexOfFirst { it.key == folder.key }.let { if (it < 0) current.size else it }
        val rest = current.filterNot { it.key == folder.key }
        val restored = items.filterNot { item -> rest.any { it.key == item.key } }
        val updated = rest.toMutableList().apply { addAll(index.coerceAtMost(size), restored) }
        favoritesService.updateFavorites(manuallySorted = updated, automaticallySorted = emptyList())
        tagsService.deleteTag(folder.tag)
    }

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
    val waveIntensity = homeSettings.waveIntensity.stateIn(viewModelScope, SharingStarted.Eagerly, dev.kbzon.launcher.preferences.home.WaveIntensity.Normal)
    val railBothEdges = homeSettings.railBothEdges.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // Fase 11: botón de acción, diseño y ajustes del menú rápido
    val actionButtonEnabled = homeSettings.actionButton.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val actionButtonActions = homeSettings.actionButtonActions
        .stateIn(viewModelScope, SharingStarted.Eagerly, dev.kbzon.launcher.preferences.home.ActionButtonActions())
    fun layout(landscape: Boolean): Flow<dev.kbzon.launcher.preferences.home.HomeLayout> = homeSettings.layout(landscape)
    fun saveLayout(landscape: Boolean, layout: dev.kbzon.launcher.preferences.home.HomeLayout) = homeSettings.setLayout(landscape, layout)
    fun setClockStyle(style: MinimalClockStyle) = homeSettings.setClockStyle(style)
    fun setRailOnLeft(left: Boolean) = homeSettings.setRailOnLeft(left)
    fun setShowIcons(show: Boolean) = homeSettings.setShowIcons(show)
    fun setSplitSwipeDown(enabled: Boolean) = homeSettings.setSplitSwipeDown(enabled)
    fun setActionButton(enabled: Boolean) = homeSettings.setActionButton(enabled)
    val actionButtonPosition = homeSettings.actionButtonPosition
        .stateIn(viewModelScope, SharingStarted.Eagerly, dev.kbzon.launcher.preferences.home.ActionButtonPosition.RailSide)
    fun setActionButtonPosition(p: dev.kbzon.launcher.preferences.home.ActionButtonPosition) = homeSettings.setActionButtonPosition(p)

    /**
     * Apaga la pantalla. Necesita el servicio de accesibilidad del launcher; si no está activo,
     * avisa y abre el ajuste.
     */
    fun lockScreen(context: Context) {
        if (permissionsManager.checkPermissionOnce(PermissionGroup.Accessibility)) {
            globalActions.lockScreen()
        } else {
            android.widget.Toast.makeText(context, dev.kbzon.launcher.ui.R.string.fork_lock_needs_accessibility, android.widget.Toast.LENGTH_LONG).show()
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

    /**
     * Próxima alarma del reloj. B13: el sistema devuelve la próxima alarma de *cualquier* app (p. ej. la
     * hora de dormir de Samsung); solo se muestra si la creó la app de reloj (o la elegida en "El reloj abre").
     */
    fun nextAlarm(context: Context): Flow<Long?> = callbackFlow {
        val alarmManager: AlarmManager = context.getSystemService() ?: run {
            trySend(null); awaitClose { }; return@callbackFlow
        }
        fun current(): Long? {
            val info = alarmManager.nextAlarmClock ?: return null
            val creator = info.showIntent?.creatorPackage ?: return null
            val clockPkg = context.packageManager
                .resolveActivity(Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS), 0)?.activityInfo?.packageName
            val chosenPkg = (clockApp.value as? Application)?.componentName?.packageName
            return if (creator == clockPkg || creator == chosenPkg) info.triggerTime else null
        }
        trySend(current())
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(current())
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

    /** Eventos de hoy y mañana para la tarjeta del reloj (M2). */
    val eventsAhead: StateFlow<List<CalendarEvent>> = permissionsManager
        .hasPermission(PermissionGroup.Calendar)
        .flatMapLatest { granted ->
            if (!granted) flowOf(emptyList())
            else {
                val now = System.currentTimeMillis()
                val endOfTomorrow = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, 2)
                    set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
                }.timeInMillis
                calendarRepository.findMany(from = now, to = endOfTomorrow).map { events ->
                    events.filter { it.isCompleted != true && it.endTime > now }
                        .sortedWith(compareBy<CalendarEvent> { it.startTime ?: it.endTime }.thenBy { !it.allDay })
                        .take(12)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasLocationPermission = permissionsManager.hasPermission(PermissionGroup.Location)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun requestLocationPermission(context: Context) {
        (context as? androidx.appcompat.app.AppCompatActivity)?.let {
            permissionsManager.requestPermission(it, PermissionGroup.Location)
        }
    }

    // B14: apps de música al conectar audio Bluetooth.
    val audioMediaAppsEnabled = homeSettings.audioMediaApps.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** Nombre del dispositivo de audio Bluetooth conectado, o null. Sin permisos (AudioDeviceCallback). */
    fun bluetoothAudioDevice(context: Context): Flow<String?> = callbackFlow {
        val audio: android.media.AudioManager = context.getSystemService() ?: run {
            trySend(null); awaitClose { }; return@callbackFlow
        }
        val types = setOf(
            android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            android.media.AudioDeviceInfo.TYPE_BLE_HEADSET,
            android.media.AudioDeviceInfo.TYPE_BLE_SPEAKER,
            android.media.AudioDeviceInfo.TYPE_HEARING_AID,
        )
        fun current() = audio.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS)
            .firstOrNull { it.type in types }?.productName?.toString()
        val callback = object : android.media.AudioDeviceCallback() {
            override fun onAudioDevicesAdded(added: Array<out android.media.AudioDeviceInfo>?) { trySend(current()) }
            override fun onAudioDevicesRemoved(removed: Array<out android.media.AudioDeviceInfo>?) { trySend(current()) }
        }
        trySend(current())
        audio.registerAudioDeviceCallback(callback, null)
        awaitClose { audio.unregisterAudioDeviceCallback(callback) }
    }

    /** Apps de música instaladas (la última usada primero), hasta 4. */
    val mediaApps: StateFlow<List<Application>> = combine(
        apps,
        kotlinx.coroutines.flow.flow { emit(musicService.getInstalledPlayerPackages()) },
    ) { all, players ->
        val last = musicService.lastPlayerPackage
        val pkgs = (listOfNotNull(last) + players).distinct()
        pkgs.mapNotNull { p -> all.firstOrNull { it.componentName.packageName == p } }.take(4)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
