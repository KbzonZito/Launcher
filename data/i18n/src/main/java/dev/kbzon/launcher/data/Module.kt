package dev.kbzon.launcher.data

import dev.kbzon.launcher.ktx.isAtLeastApiLevel
import dev.kbzon.launcher.search.StringNormalizer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val i18nDataModule = module {
    single<StringNormalizer> {
        if (isAtLeastApiLevel(29)) IcuStringNormalizer(androidContext(), get())
        else CompatStringNormalizer()
    }
}