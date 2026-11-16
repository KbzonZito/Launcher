package dev.kbzon.launcher.services.tags.impl

import dev.kbzon.launcher.data.customattrs.CustomAttributesRepository
import dev.kbzon.launcher.searchable.SavableSearchableRepository
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.Tag
import dev.kbzon.launcher.searchable.PinnedLevel
import dev.kbzon.launcher.services.tags.TagsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class TagsServiceImpl(
    private val customAttributesRepository: CustomAttributesRepository,
    private val searchableRepository: SavableSearchableRepository,
) : TagsService {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    override fun getAllTags(startsWith: String?): Flow<List<String>> {
        return customAttributesRepository.getAllTags(startsWith)
    }

    override fun deleteTag(tag: String) {
        searchableRepository.delete(Tag(tag))
        customAttributesRepository.deleteTag(tag)
    }

    override fun cloneTag(tag: String, newTag: String) {
        scope.launch {
            val items = getTaggedItems(tag).first()
            createTag(newTag, items)
        }
    }

    override fun getTaggedItems(tag: String): Flow<List<SavableSearchable>> {
        return customAttributesRepository.getItemsForTag(tag)
    }

    override fun updateTag(tag: String, newName: String?, items: List<SavableSearchable>?) {
        scope.launch {
            if (items != null) {
                customAttributesRepository.setItemsForTag(tag, items).join()
            }
            if (newName != null && newName != tag) {
                customAttributesRepository.renameTag(tag, newName).join()
                searchableRepository.replace(Tag(tag).key, Tag(newName))
            }

        }
    }

    override fun createTag(tag: String, items: List<SavableSearchable>) {
        scope.launch {
            customAttributesRepository.setItemsForTag(tag, items)
        }
    }

    override fun getTags(it: SavableSearchable): Flow<List<String>> {
        return customAttributesRepository.getTags(it)
    }
}