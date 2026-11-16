package dev.kbzon.launcher.feed

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val feedModule = module {
    single { FeedService(androidContext()) }
}