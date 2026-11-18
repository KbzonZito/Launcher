package dev.kbzon.launcher.weather.openmeteo

import android.content.Context
import android.util.Log
import dev.kbzon.launcher.crashreporter.CrashReporter
import dev.kbzon.launcher.preferences.weather.WeatherLocation
import dev.kbzon.launcher.weather.Forecast
import dev.kbzon.launcher.weather.GeocoderWeatherProvider
import dev.kbzon.launcher.weather.R

/** [fork] F16: proveedor predeterminado. Datos horarios de 7 días, códigos de tiempo WMO. */
internal class OpenMeteoProvider(
    private val context: Context,
) : GeocoderWeatherProvider(context) {

    private val api = OpenMeteoApi()

    override suspend fun getWeatherData(location: WeatherLocation): List<Forecast>? {
        return when (location) {
            is WeatherLocation.LatLon -> getWeatherData(location.lat, location.lon, location.name)
            else -> {
                Log.e("OpenMeteoProvider", "Unsupported location type: $location")
                null
            }
        }
    }

    override suspend fun getWeatherData(lat: Double, lon: Double): List<Forecast>? {
        return getWeatherData(lat, lon, getLocationName(lat, lon))
    }

    private suspend fun getWeatherData(lat: Double, lon: Double, locationName: String): List<Forecast>? {
        val hourly = runCatching { api.forecast(lat, lon).hourly }.getOrElse {
            CrashReporter.logException(Exception(it))
            return null
        } ?: return null

        val updateTime = System.currentTimeMillis()
        return hourly.time.indices.mapNotNull { i ->
            val celsius = hourly.temperature.getOrNull(i) ?: return@mapNotNull null
            val code = hourly.weatherCode.getOrNull(i) ?: return@mapNotNull null
            val kelvin = celsius + 273.15
            Forecast(
                timestamp = hourly.time[i] * 1000L,
                temperature = kelvin,
                minTemp = kelvin,
                maxTemp = kelvin,
                pressure = hourly.pressure.getOrNull(i),
                humidity = hourly.humidity.getOrNull(i),
                icon = iconFor(code),
                condition = context.getString(conditionFor(code)),
                clouds = hourly.cloudCover.getOrNull(i),
                windSpeed = hourly.windSpeed.getOrNull(i),
                windDirection = hourly.windDirection.getOrNull(i),
                precipitation = hourly.precipitation.getOrNull(i),
                uvIndex = hourly.uvIndex.getOrNull(i),
                night = hourly.isDay.getOrNull(i) == 0,
                location = locationName,
                provider = "Open-Meteo",
                providerUrl = "https://open-meteo.com/",
                precipProbability = hourly.precipProbability.getOrNull(i),
                updateTime = updateTime,
            )
        }
    }

    private fun iconFor(code: Int): Int = when (code) {
        0, 1 -> Forecast.CLEAR
        2 -> Forecast.PARTLY_CLOUDY
        3 -> Forecast.OVERCAST
        45, 48 -> Forecast.FOG
        51, 53, 55, 61, 80 -> Forecast.LIGHT_RAIN
        63, 81 -> Forecast.RAIN
        65, 82 -> Forecast.HEAVY_RAIN
        56, 57, 66, 67 -> Forecast.SLEET
        71, 73, 75, 77, 85, 86 -> Forecast.SNOW
        95 -> Forecast.THUNDERSTORM
        96, 99 -> Forecast.HAIL
        else -> Forecast.UNKNOWN
    }

    private fun conditionFor(code: Int): Int = when (code) {
        0 -> R.string.weather_condition_clearsky
        1 -> R.string.weather_condition_fair
        2 -> R.string.weather_condition_partlycloudy
        3 -> R.string.weather_condition_cloudy
        45, 48 -> R.string.weather_condition_fog
        51, 53, 55, 61 -> R.string.weather_condition_lightrain
        63 -> R.string.weather_condition_rain
        65 -> R.string.weather_condition_heavyrain
        56, 57, 66, 67 -> R.string.weather_condition_sleet
        71 -> R.string.weather_condition_lightsnow
        73, 77 -> R.string.weather_condition_snow
        75 -> R.string.weather_condition_heavysnow
        80 -> R.string.weather_condition_lightrainshowers
        81 -> R.string.weather_condition_rainshowers
        82 -> R.string.weather_condition_heavyrainshowers
        85, 86 -> R.string.weather_condition_snowshowers
        95 -> R.string.weather_condition_thunderstorm
        96, 99 -> R.string.weather_condition_hail
        else -> R.string.weather_condition_unknown
    }

    companion object {
        internal const val Id = "openmeteo"
    }
}
