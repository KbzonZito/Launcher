package dev.kbzon.launcher.ui.settings.debug

import androidx.lifecycle.ViewModel
import dev.kbzon.launcher.data.customattrs.CustomAttributesRepository
import dev.kbzon.launcher.icons.IconService
import dev.kbzon.launcher.preferences.BaseLayout
import dev.kbzon.launcher.preferences.ui.UiSettings
import dev.kbzon.launcher.searchable.SavableSearchableRepository
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DebugSettingsScreenVM: ViewModel(), KoinComponent {

    private val searchableRepository: SavableSearchableRepository by inject()
    private val customAttributesRepository: CustomAttributesRepository by inject()
    private val iconService: IconService by inject()

    private val uiSettings: UiSettings by inject()

    suspend fun cleanUpDatabase(): Int {
        var removed = searchableRepository.cleanupDatabase()
        removed += customAttributesRepository.cleanupDatabase()
        return removed
    }

    fun reinstallIconPacks() {
        iconService.reinstallAllIconPacks()
    }
}