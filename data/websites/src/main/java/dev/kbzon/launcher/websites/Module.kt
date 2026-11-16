package dev.kbzon.launcher.websites

import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.SearchableRepository
import dev.kbzon.launcher.search.Website
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val websitesModule = module {
    single<SearchableRepository<Website>>(named<Website>()) { WebsiteRepository(androidContext(), get()) }
    factory<SearchableDeserializer>(named(WebsiteImpl.Domain)) { WebsiteDeserializer() }
}