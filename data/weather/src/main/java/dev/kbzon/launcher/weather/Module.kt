package dev.kbzon.launcher.weather

import dev.kbzon.launcher.weather.breezy.BreezyWeatherProvider
import dev.kbzon.launcher.weather.brightsky.BrightSkyProvider
import dev.kbzon.launcher.weather.metno.MetNoProvider
import dev.kbzon.launcher.weather.openweathermap.OpenWeatherMapProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val weatherModule = module {
    single<WeatherRepository> { WeatherRepositoryImpl(androidContext(), get(), get()) }
    factory<WeatherProvider> { (providerId: String) ->
        when (providerId) {
            OpenWeatherMapProvider.Id -> OpenWeatherMapProvider(androidContext())
            MetNoProvider.Id -> MetNoProvider(androidContext(), get())
            BrightSkyProvider.Id -> BrightSkyProvider(androidContext())
            // [fork] plugins eliminados: un proveedor desconocido vuelve a MET Norway.
            BreezyWeatherProvider.Id -> BreezyWeatherProvider(androidContext())
            else -> MetNoProvider(androidContext(), get())
        }
    }
}