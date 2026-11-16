package dev.kbzon.launcher.ui.settings.tags

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.data.customattrs.CustomAttributesRepository
import dev.kbzon.launcher.icons.IconService
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.search.Tag
import dev.kbzon.launcher.services.tags.TagsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TagsSettingsScreenVM: ViewModel(), KoinComponent {
    private val tagsService: TagsService by inject()
    private val iconService: IconService by inject()

    val tags = tagsService.getAllTags()

    var editTag = mutableStateOf<String?>(null)
    var createTag = mutableStateOf(false)

    fun duplicateTag(tag: String) {
        viewModelScope.launch {
            val allTags = tags.first()
            var i = 2
            var newName = "$tag ($i)"
            while(allTags.contains(newName)) {
                i++
                newName = "$tag ($i)"
            }
            tagsService.cloneTag(tag, newName)
        }
    }

    fun deleteTag(tag: String) {
        tagsService.deleteTag(tag)
    }

    fun getIcon(tag: String): Flow<LauncherIcon?> {
        return iconService.getIcon(Tag(tag), 1)
    }

}