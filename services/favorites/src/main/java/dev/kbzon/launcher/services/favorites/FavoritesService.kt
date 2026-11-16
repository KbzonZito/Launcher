package dev.kbzon.launcher.services.favorites

import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.first

import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.searchable.PinnedLevel
import dev.kbzon.launcher.searchable.SavableSearchableRepository
import dev.kbzon.launcher.searchable.VisibilityLevel
import kotlinx.coroutines.flow.Flow

class FavoritesService(
    private val searchableRepository: SavableSearchableRepository,
    private val privacySettings: dev.kbzon.launcher.preferences.privacy.PrivacySettings, // [fork]
) {
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob()) // [fork]

    fun getFavorites(
        includeTypes: List<String>? = null,
        excludeTypes: List<String>? = null,
        minPinnedLevel: PinnedLevel = PinnedLevel.FrequentlyUsed,
        maxPinnedLevel: PinnedLevel = PinnedLevel.ManuallySorted,
        limit: Int = 100,
    ): Flow<List<SavableSearchable>> {
        return searchableRepository.get(
            includeTypes = includeTypes,
            excludeTypes = excludeTypes,
            minPinnedLevel = minPinnedLevel,
            maxPinnedLevel = maxPinnedLevel,
            limit = limit,
            minVisibility = VisibilityLevel.SearchOnly,
        )
    }

    fun isPinned(searchable: SavableSearchable): Flow<Boolean> {
        return searchableRepository.isPinned(searchable)
    }

    fun getVisibility(searchable: SavableSearchable): Flow<VisibilityLevel> {
        return searchableRepository.getVisibility(searchable)
    }

    fun pinItem(searchable: SavableSearchable) {
        searchableRepository.upsert(
            searchable,
            pinned = true,
        )
    }

    fun reset(searchable: SavableSearchable) {
        searchableRepository.update(
            searchable,
            pinned = false,
            visibility = VisibilityLevel.Default,
            weight = 0.0,
            launchCount = 0,
        )
    }

    fun unpinItem(searchable: SavableSearchable) {
        searchableRepository.upsert(
            searchable,
            pinned = false,
        )
    }

    fun setVisibility(searchable: SavableSearchable, visibility: VisibilityLevel) {
        searchableRepository.upsert(
            searchable,
            visibility = visibility,
        )
    }

    /** [fork] Olvida qué apps se usan más (no afecta favoritos fijados ni ocultos). */
    fun resetUsageStats() = searchableRepository.resetUsageStats()

    fun reportLaunch(searchable: SavableSearchable) {
        // [fork] Modo incógnito: no se registra la apertura.
        scope.launch {
            if (!privacySettings.incognito.first()) searchableRepository.touch(searchable)
        }
    }

    fun updateFavorites(
        manuallySorted: List<SavableSearchable>,
        automaticallySorted: List<SavableSearchable>
    ) {
        searchableRepository.updateFavorites(
            manuallySorted = manuallySorted,
            automaticallySorted = automaticallySorted
        )
    }

    fun delete(searchable: SavableSearchable) {
        searchableRepository.delete(searchable)
    }

    fun upsert(searchable: SavableSearchable) {
        searchableRepository.upsert(searchable)
    }
}