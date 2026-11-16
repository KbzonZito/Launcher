package dev.kbzon.launcher.ui.launcher.search

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.devicepose.DevicePoseProvider
import dev.kbzon.launcher.ktx.isAtLeastApiLevel
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.search.CalendarSearchSettings
import dev.kbzon.launcher.preferences.search.ContactSearchSettings
import dev.kbzon.launcher.preferences.search.FileSearchSettings
import dev.kbzon.launcher.preferences.search.LocationSearchSettings
import dev.kbzon.launcher.preferences.search.SearchFilterSettings
import dev.kbzon.launcher.preferences.search.ShortcutSearchSettings
import dev.kbzon.launcher.preferences.ui.SearchUiSettings
import dev.kbzon.launcher.profiles.Profile
import dev.kbzon.launcher.profiles.ProfileManager
import dev.kbzon.launcher.search.AppShortcut
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.Article
import dev.kbzon.launcher.search.CalendarEvent
import dev.kbzon.launcher.search.Contact
import dev.kbzon.launcher.search.File
import dev.kbzon.launcher.search.Location
import dev.kbzon.launcher.search.ResultScore
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.SearchFilters
import dev.kbzon.launcher.search.SearchResults
import dev.kbzon.launcher.search.SearchService
import dev.kbzon.launcher.search.Searchable
import dev.kbzon.launcher.search.Website
import dev.kbzon.launcher.search.data.Calculator
import dev.kbzon.launcher.search.data.UnitConverter
import dev.kbzon.launcher.search.isUnspecified
import dev.kbzon.launcher.searchable.SavableSearchableRepository
import dev.kbzon.launcher.searchable.VisibilityLevel
import dev.kbzon.launcher.searchactions.actions.SearchAction
import dev.kbzon.launcher.services.favorites.FavoritesService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchVM : ViewModel(), KoinComponent {

    private val favoritesService: FavoritesService by inject()
    private val searchableRepository: SavableSearchableRepository by inject()
    private val permissionsManager: PermissionsManager by inject()
    private val profileManager: ProfileManager by inject()

    private val fileSearchSettings: FileSearchSettings by inject()
    private val contactSearchSettings: ContactSearchSettings by inject()
    private val calendarSearchSettings: CalendarSearchSettings by inject()
    private val shortcutSearchSettings: ShortcutSearchSettings by inject()
    private val searchUiSettings: SearchUiSettings by inject()
    private val locationSearchSettings: LocationSearchSettings by inject()
    private val devicePoseProvider: DevicePoseProvider by inject()
    private val searchFilterSettings: SearchFilterSettings by inject()

    val launchOnEnter = searchUiSettings.launchOnEnter
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val searchService: SearchService by inject()

    val searchQuery = mutableStateOf("")
    val isSearchEmpty = mutableStateOf(true)

    val expandedCategory = mutableStateOf<SearchCategory?>(null)

    val profiles = profileManager.profiles
    val profileStates = profileManager.profileStates

    val hasProfilesPermission = permissionsManager.hasPermission(PermissionGroup.ManageProfiles)

    fun setProfileLock(profile: Profile?, locked: Boolean) {
        if (isAtLeastApiLevel(28) && profile != null) {
            if (locked) {
                profileManager.lockProfile(profile)
            } else {
                profileManager.unlockProfile(profile)
            }
        }
    }

    val appResults = mutableStateListOf<Application>()
    val workAppResults = mutableStateListOf<Application>()
    val privateSpaceAppResults = mutableStateListOf<Application>()

    val appShortcutResults = mutableStateListOf<AppShortcut>()
    val fileResults = mutableStateListOf<File>()
    val contactResults = mutableStateListOf<Contact>()
    val calendarResults = mutableStateListOf<CalendarEvent>()
    val articleResults = mutableStateListOf<Article>()
    val websiteResults = mutableStateListOf<Website>()
    val calculatorResults = mutableStateListOf<Calculator>()
    val unitConverterResults = mutableStateListOf<UnitConverter>()
    val searchActionResults = mutableStateListOf<SearchAction>()
    val locationResults = mutableStateListOf<Location>()

    var previousResults: SearchResults? = null

    val hiddenResultsButton = searchUiSettings.hiddenItemsButton
    val hiddenResults = mutableStateListOf<SavableSearchable>()

    val favoritesEnabled = searchUiSettings.favorites
    val hideFavorites = mutableStateOf(false)

    val allAppsEnabled = searchUiSettings.allApps

    val showFilters = mutableStateOf(false)

    private val defaultFilters = searchFilterSettings.defaultFilter.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SearchFilters()
    )
    val filters = mutableStateOf(defaultFilters.value)
    val filterBar = searchFilterSettings.filterBar
    val filterBarItems = searchFilterSettings.filterBarItems

    val bestMatch = mutableStateOf<Searchable?>(null)

    val selectedAppProfileType = mutableStateOf(Profile.Type.Personal)

    init {
        search("", forceRestart = true)
    }

    fun launchBestMatchOrAction(context: Context) {
        val bestMatch = bestMatch.value
        if (bestMatch is SavableSearchable) {
            bestMatch.launch(context, null)
            favoritesService.reportLaunch(bestMatch)
            return
        } else if (bestMatch is SearchAction) {
            bestMatch.start(context)
            return
        }
    }

    fun setFilters(filters: SearchFilters) {
        // [fork] Mostrar apps ocultas requiere desbloquear (biometría si está activada).
        if (filters.hiddenItems && !this.filters.value.hiddenItems && !dev.kbzon.launcher.ui.privacy.HiddenItemsLock.unlocked.value) {
            dev.kbzon.launcher.ui.privacy.HiddenItemsLock.requestUnlock { setFilters(filters) }
            return
        }
        this.filters.value = filters
        search(searchQuery.value, forceRestart = true)
    }

    fun closeFilters() {
        showFilters.value = false
    }

    fun reset() {
        closeFilters()
        filters.value = defaultFilters.value
        search("")
    }

    private var searchJob: Job? = null
    private var filtersBeforePrefix: SearchFilters? = null // [fork]
    fun search(query: String, forceRestart: Boolean = false) {
        if (searchQuery.value == query && !forceRestart) return
        if (searchQuery.value != query) {
            showFilters.value = false
        }
        if (query.isEmpty() && searchQuery.value.isNotEmpty()) {
            filters.value = defaultFilters.value
        }
        searchQuery.value = query
        isSearchEmpty.value = query.isEmpty()

        // [fork] Prefijos ("c ana" = solo contactos). La barra sigue mostrando el texto original;
        // los filtros visibles pasan a la categoría del prefijo y se restauran al borrarlo.
        val prefixed = dev.kbzon.launcher.ui.search.SearchPrefixes.parse(query)
        if (prefixed != null) {
            val base = filtersBeforePrefix ?: filters.value.also { filtersBeforePrefix = it }
            filters.value = with(dev.kbzon.launcher.ui.search.SearchPrefixes) { prefixed.applyTo(base) }
        } else {
            filtersBeforePrefix?.let { filters.value = it }
            filtersBeforePrefix = null
        }
        val filters = filters.value
        @Suppress("NAME_SHADOWING") val query = prefixed?.query ?: query

        if (filters.enabledCategories == 1) {
            expandedCategory.value = when {
                filters.apps -> SearchCategory.Apps
                filters.events -> SearchCategory.Calendar
                filters.contacts -> SearchCategory.Contacts
                filters.files -> SearchCategory.Files
                filters.websites -> SearchCategory.Website
                filters.articles -> SearchCategory.Articles
                filters.places -> SearchCategory.Location
                filters.shortcuts -> SearchCategory.Shortcuts
                else -> null
            }
        } else {
            expandedCategory.value = null
        }

        if (isSearchEmpty.value)
            bestMatch.value = null
        try {
            searchJob?.cancel()
        } catch (_: CancellationException) {
        }
        hideFavorites.value = query.isNotEmpty()

        searchJob = viewModelScope.launch {
            if (query.isEmpty()) {
                val hiddenItemKeys = if (!filters.hiddenItems) {
                    searchableRepository.getKeys(
                        maxVisibility = VisibilityLevel.SearchOnly,
                        includeTypes = listOf("app"),
                    )
                } else {
                    flowOf(emptyList())
                }
                val allApps = searchService.getAllApps()

                allApps
                    .combine(hiddenItemKeys) { results, hiddenKeys -> results to hiddenKeys }
                    .collectLatest { (results, hiddenKeys) ->
                        val hiddenItems = mutableListOf<SavableSearchable>()

                        val (hiddenApps, apps) = results.standardProfileApps.partition {
                            hiddenKeys.contains(
                                it.key
                            )
                        }
                        hiddenItems += hiddenApps

                        val (hiddenWorkApps, workApps) = results.workProfileApps.partition {
                            hiddenKeys.contains(
                                it.key
                            )
                        }
                        hiddenItems += hiddenWorkApps

                        val (hiddenPrivateApps, privateApps) = results.privateSpaceApps.partition {
                            hiddenKeys.contains(
                                it.key
                            )
                        }
                        hiddenItems += hiddenPrivateApps
                        previousResults = SearchResults(apps = apps)

                        searchActionResults.clear()
                        appResults.updateItems(apps)
                        workAppResults.updateItems(workApps)
                        privateSpaceAppResults.updateItems(privateApps)
                        hiddenResults.updateItems(hiddenItems)
                    }

            } else {
                val hiddenItemKeys = if (!filters.hiddenItems) searchableRepository.getKeys(
                    maxVisibility = VisibilityLevel.Hidden,
                ) else flowOf(emptyList())
                searchService.search(
                    query,
                    filters = filters,
                    previousResults,
                )
                    .combine(hiddenItemKeys) { results, hiddenKeys -> results to hiddenKeys }
                    .collectLatest { (results, hiddenKeys) ->
                        previousResults = results

                        hiddenResults.clear()
                        workAppResults.clear()
                        privateSpaceAppResults.clear()

                        appResults.updateItems(
                            results.apps
                            ?.filterNot { hiddenKeys.contains(it.key) }
                            ?.applyRanking(query)
                        )
                        appShortcutResults.updateItems(
                            results.shortcuts
                            ?.filterNot { hiddenKeys.contains(it.key) }
                            ?.applyRanking(query)
                        )
                        fileResults.updateItems(
                            results.files
                            ?.filterNot { hiddenKeys.contains(it.key) }
                            ?.applyRanking(query)
                        )

                        contactResults.updateItems(
                            results.contacts?.filterNot { hiddenKeys.contains(it.key) }
                                ?.applyRanking(query)
                        )
                        calendarResults.updateItems(
                            results.calendars?.filterNot { hiddenKeys.contains(it.key) }
                                ?.applyRanking(query)
                        )
                        locationResults.updateItems(
                            results.locations?.filterNot { hiddenKeys.contains(it.key) }
                                ?.let { locations ->
                                    devicePoseProvider.lastCachedLocation?.let {
                                        locations.asSequence()
                                            .sortedWith { a, b ->
                                                a.distanceTo(it).compareTo(b.distanceTo(it))
                                            }
                                            .distinctBy { it.key }
                                            .toList()
                                    } ?: locations.applyRanking(query)
                                }
                        )
                        articleResults.updateItems(
                            results.wikipedia?.applyRanking(query)
                        )
                        websiteResults.updateItems(
                            results.websites?.applyRanking(query)
                        )
                        calculatorResults.updateItems(results.calculators)
                        unitConverterResults.updateItems(results.unitConverters)

                        if (results.searchActions != null) {
                            searchActionResults.updateItems(results.searchActions!!)
                        }

                        if (launchOnEnter.value) {
                            bestMatch.value = when {
                                appResults.isNotEmpty() -> appResults.first()
                                appShortcutResults.isNotEmpty() -> appShortcutResults.first()
                                calendarResults.isNotEmpty() -> calendarResults.first()
                                locationResults.isNotEmpty() -> locationResults.first()
                                contactResults.isNotEmpty() -> contactResults.first()
                                articleResults.isNotEmpty() -> articleResults.first()
                                websiteResults.isNotEmpty() -> websiteResults.first()
                                fileResults.isNotEmpty() -> fileResults.first()
                                searchActionResults.isNotEmpty() -> searchActionResults.first()
                                else -> null
                            }
                        } else {
                            bestMatch.value = null
                        }
                    }
            }
        }
    }

    val missingCalendarPermission = combine(
        permissionsManager.hasPermission(PermissionGroup.Calendar),
        calendarSearchSettings.providers,
    ) { perm, providers -> !perm && providers.contains("local") }

    fun requestCalendarPermission(context: AppCompatActivity) {
        permissionsManager.requestPermission(context, PermissionGroup.Calendar)
    }

    fun disableCalendarSearch() {
        calendarSearchSettings.setProviderEnabled("local", false)
    }

    val missingContactsPermission = combine(
        permissionsManager.hasPermission(PermissionGroup.Contacts),
        contactSearchSettings.isProviderEnabled("local")
    ) { perm, enabled -> !perm && enabled }

    fun requestContactsPermission(context: AppCompatActivity) {
        permissionsManager.requestPermission(context, PermissionGroup.Contacts)
    }

    fun disableContactsSearch() {
        contactSearchSettings.setProviderEnabled("local", false)
    }

    val missingLocationPermission = combine(
        permissionsManager.hasPermission(PermissionGroup.Location),
        locationSearchSettings.osmLocations.distinctUntilChanged()
    ) { perm, enabled -> !perm && enabled }

    fun requestLocationPermission(context: AppCompatActivity) {
        permissionsManager.requestPermission(context, PermissionGroup.Location)
    }

    fun disableLocationSearch() {
        locationSearchSettings.setOsmLocations(false)
    }

    val missingFilesPermission = combine(
        permissionsManager.hasPermission(PermissionGroup.ExternalStorage),
        fileSearchSettings.localFiles
    ) { perm, enabled -> !perm && enabled }

    fun requestFilesPermission(context: AppCompatActivity) {
        permissionsManager.requestPermission(context, PermissionGroup.ExternalStorage)
    }

    fun disableFilesSearch() {
        fileSearchSettings.setLocalFiles(false)
    }

    val missingAppShortcutPermission = combine(
        permissionsManager.hasPermission(PermissionGroup.AppShortcuts),
        shortcutSearchSettings.enabled,
    ) { perm, enabled -> !perm && enabled }

    fun requestAppShortcutPermission(context: AppCompatActivity) {
        permissionsManager.requestPermission(context, PermissionGroup.AppShortcuts)
    }

    fun disableAppShortcutSearch() {
        shortcutSearchSettings.setEnabled(false)
    }

    fun expandCategory(category: SearchCategory) {
        expandedCategory.value = category
    }

    private suspend fun <T : SavableSearchable> List<T>.applyRanking(query: String): List<T> {
        if (size <= 1) return this
        val sequence = asSequence()
        val weights = searchableRepository.getWeights(map { it.key }).first()
        val sorted = sequence.sortedWith { a, b ->
            val aWeight = weights[a.key] ?: 0.0
            val bWeight = weights[b.key] ?: 0.0

            val aScore = if (a.score.isUnspecified) {
                ResultScore.from(query = query, primaryFields = listOf(a.labelOverride ?: a.label)).score
            } else {
                a.score.score
            }

            val bScore = if (b.score.isUnspecified) {
                ResultScore.from(query = query, primaryFields = listOf(b.labelOverride ?: b.label)).score
            } else {
                b.score.score
            }

            val aTotal = aScore * 0.6f + aWeight.toFloat() * 0.4f
            val bTotal = bScore * 0.6f + bWeight.toFloat() * 0.4f

            bTotal.compareTo(aTotal)
        }
        return sorted.distinctBy { it.key }.toList()
    }

    /**
     * Merges a list of new items into the current SnapshotStateList.
     * It removes items that are in the current list but not in the new list.
     * Then, it updates existing items or adds new items from the new list.
     *
     * @param T The type of items in the list.
     * @param newItems The list of new items to merge with. If null, an empty list is used.
     */
    private fun <T> SnapshotStateList<T>.updateItems(newItems: List<T>?) {
        clear()
        addAll(newItems ?: emptyList())
    }
}


enum class SearchCategory {
    Apps,
    Calculator,
    Calendar,
    Contacts,
    Files,
    UnitConverter,
    Articles,
    Website,
    Location,
    Shortcuts,
}