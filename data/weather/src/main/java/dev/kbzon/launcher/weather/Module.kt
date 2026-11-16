package dev.kbzon.launcher.weather

import dev.kbzon.launcher.weather.breezy.BreezyWeatherProvider
import dev.kbzon.launcher.weather.brightsky.BrightSkyProvider
import dev.kbzon.launcher.weather.metno.MetNoProvider
import dev.kbzon.launcher.weather.openweathermap.OpenWeatherMapProvider
import dev.kbzon.launcher.weather.plugin.PluginWeatherProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val weatherModule = module {
    single<WeatherRepository> { WeatherRepositoryImpl(androidContext(), get(), get(), get()) }
    factory<WeatherProvider> { (providerId: String) ->
        when (providerId) {
            OpenWeatherMapProvider.Id -> OpenWeatherMapProvider(androidContext())
            MetNoProvider.Id -> MetNoProvider(androidContext(), get())
            BrightSkyProvider.Id -> BrightSkyProvider(androidContext())
            BreezyWeatherProvider.Id -> BreezyWeatherProvider(androidContext())
            else -> PluginWeatherProvider(androidContext(), providerId)
        }
    }
}