package dev.kbzon.launcher.unitconverter

import dev.kbzon.launcher.currencies.CurrencyRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val unitConverterModule = module {
    single { CurrencyRepository(androidContext()) }
    single<UnitConverterRepository> { UnitConverterRepositoryImpl(androidContext(), get(), get()) }
}