package dev.kbzon.launcher.themes

import dev.kbzon.launcher.backup.Backupable
import org.koin.core.qualifier.named
import org.koin.dsl.module

val themesModule = module {
    factory<Backupable>(named<ThemeRepository>()) { ThemeRepository(get(), get()) }
    factory { ThemeRepository(get(), get()) }
}