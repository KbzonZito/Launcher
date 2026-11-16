package de.mm20.launcher2.ui.home

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mm20.launcher2.calendar.CalendarRepository
import de.mm20.launcher2.icons.IconService
import de.mm20.launcher2.icons.LauncherIcon
import de.mm20.launcher2.permissions.PermissionGroup
import de.mm20.launcher2.permissions.PermissionsManager
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

    val showIcons = homeSettings.showIcons.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val railOnLeft = homeSettings.railOnLeft.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Apps visibles (personal + trabajo), sin las ocultas, ordenadas alfabéticamente. */
    val apps: StateFlow<List<Application>> = combine(
        searchService.getAllApps(),
        searchableRepository.getKeys(
            maxVisibility = VisibilityLevel.SearchOnly,
            includeTypes = listOf("app"),
        ),
    ) { all, hiddenKeys ->
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

    /** Letra seleccionada en el riel; null = se muestran los favoritos. */
    val selectedLetter = MutableStateFlow<Char?>(null)

    fun selectLetter(letter: Char?) {
        selectedLetter.value = letter
    }

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

    fun icon(searchable: SavableSearchable, size: Int): Flow<LauncherIcon?> =
        iconService.getIcon(searchable, size)

    fun launch(context: Context, searchable: SavableSearchable): Boolean {
        val options = ActivityOptionsCompat.makeBasic().toBundle()
        return if (searchable.launch(context, options)) {
            favoritesService.reportLaunch(searchable)
            true
        } else false
    }

    companion object {
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
