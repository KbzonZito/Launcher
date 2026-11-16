package dev.kbzon.launcher.applications

import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.SearchableRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val applicationsModule = module {
    factory<SearchableRepository<Application>>(named<Application>()) { get<AppRepository>() }
    single<AppRepository> { AppRepositoryImpl(androidContext(), get(), get()) }
    factory<SearchableDeserializer>(named(LauncherApp.Domain)) { LauncherAppDeserializer(androidContext()) }
}