package dev.kbzon.launcher.appshortcuts

import dev.kbzon.launcher.search.AppShortcut
import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.SearchableRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appShortcutsModule = module {
    single<AppShortcutRepository> { AppShortcutRepositoryImpl(androidContext(), get(), get(), get(), get()) }
    factory<SearchableRepository<AppShortcut>>(named<AppShortcut>()) { get<AppShortcutRepository>() }
    factory<SearchableDeserializer>(named(LauncherShortcut.Domain)) { LauncherShortcutDeserializer(androidContext()) }
    factory<SearchableDeserializer>(named(LegacyShortcut.Domain)) { LegacyShortcutDeserializer(androidContext()) }
}