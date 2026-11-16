package dev.kbzon.launcher.searchable

import dev.kbzon.launcher.backup.Backupable
import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.Tag
import org.koin.core.qualifier.named
import org.koin.dsl.module

val searchableModule = module {
    factory <Backupable>(named<SavableSearchableRepository>()) { SavableSearchableRepositoryImpl(
        get(),
        get()
    ) }
    factory <SavableSearchableRepository> { SavableSearchableRepositoryImpl(get(), get()) }
}