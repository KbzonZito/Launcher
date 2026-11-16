package dev.kbzon.launcher.services.favorites

import org.koin.dsl.module

val favoritesModule = module {
    factory { FavoritesService(get(), get()) } // [fork] + PrivacySettings
}