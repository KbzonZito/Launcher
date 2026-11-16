package dev.kbzon.launcher.services.tags

import dev.kbzon.launcher.services.tags.impl.TagsServiceImpl
import org.koin.dsl.module

val servicesTagsModule = module {
    single<TagsService> { TagsServiceImpl(get(), get()) }
}