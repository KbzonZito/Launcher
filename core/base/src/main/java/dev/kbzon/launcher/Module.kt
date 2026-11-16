package dev.kbzon.launcher

import android.content.pm.PackageManager
import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.Tag
import dev.kbzon.launcher.search.TagDeserializer
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val baseModule = module {
    factory<PackageManager> { androidContext().packageManager }
    factory<SearchableDeserializer>(named(Tag.Domain)) { TagDeserializer() }
}