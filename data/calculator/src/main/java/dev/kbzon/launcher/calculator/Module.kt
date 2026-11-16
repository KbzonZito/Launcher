package dev.kbzon.launcher.calculator

import org.koin.dsl.module

val calculatorModule = module {
    single<CalculatorRepository> { CalculatorRepositoryImpl(get()) }
}