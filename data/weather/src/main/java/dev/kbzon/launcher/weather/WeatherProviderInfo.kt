package dev.kbzon.launcher.weather

data class WeatherProviderInfo(
    val id: String,
    val name: String,
    val managedLocation: Boolean = false,
)