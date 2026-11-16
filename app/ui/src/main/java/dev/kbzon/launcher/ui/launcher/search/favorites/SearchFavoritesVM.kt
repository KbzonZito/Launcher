package dev.kbzon.launcher.ui.launcher.search.favorites

import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.preferences.ui.UiState
import dev.kbzon.launcher.ui.common.FavoritesVM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class SearchFavoritesVM : FavoritesVM() {
    private val uiState: UiState by inject()

    override val tagsExpanded: Flow<Boolean> = uiState.favoritesTagsExpanded

    override fun setTagsExpanded(expanded: Boolean) {
        uiState.setFavoritesTagsExpanded(expanded)
    }

    override val compactTags: Flow<Boolean> = settings.compactTags

}