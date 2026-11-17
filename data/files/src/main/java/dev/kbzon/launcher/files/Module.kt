package dev.kbzon.launcher.files

import dev.kbzon.launcher.files.providers.LocalFile
import dev.kbzon.launcher.files.providers.NextcloudFile
import dev.kbzon.launcher.files.providers.OwncloudFile
import dev.kbzon.launcher.search.File
import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.SearchableRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val filesModule = module {
    factory<SearchableRepository<File>>(named<File>()) {
        FileRepository(
            androidContext(),
            get(),
            get()
        )
    }
    factory<SearchableDeserializer>(named(LocalFile.Domain)) { LocalFileDeserializer(androidContext()) }
    factory<SearchableDeserializer>(named(OwncloudFile.Domain)) { OwncloudFileDeserializer() }
    factory<SearchableDeserializer>(named(NextcloudFile.Domain)) { NextcloudFileDeserializer() }
}