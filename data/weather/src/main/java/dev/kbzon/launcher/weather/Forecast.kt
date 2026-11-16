package dev.kbzon.launcher.weather

import dev.kbzon.launcher.database.entities.ForecastEntity
import dev.kbzon.launcher.weather.Forecast.Companion.CLEAR
import dev.kbzon.launcher.weather.Forecast.Companion.EXTREME_COLD
import dev.kbzon.launcher.weather.Forecast.Companion.EXTREME_HEAT
import dev.kbzon.launcher.weather.Forecast.Companion.FOG
import dev.kbzon.launcher.weather.Forecast.Companion.HAIL
import dev.kbzon.launcher.weather.Forecast.Companion.HAZE
import dev.kbzon.launcher.weather.Forecast.Companion.HEAVY_RAIN
import dev.kbzon.launcher.weather.Forecast.Companion.LIGHT_RAIN
import dev.kbzon.launcher.weather.Forecast.Companion.OVERCAST
import dev.kbzon.launcher.weather.Forecast.Companion.PARTLY_CLOUDY
import dev.kbzon.launcher.weather.Forecast.Companion.RAIN
import dev.kbzon.launcher.weather.Forecast.Companion.SLEET
import dev.kbzon.launcher.weather.Forecast.Companion.SNOW
import dev.kbzon.launcher.weather.Forecast.Companion.THUNDER
import dev.kbzon.launcher.weather.Forecast.Companion.THUNDERSTORM
import dev.kbzon.launcher.weather.Forecast.Companion.UNKNOWN
import dev.kbzon.launcher.weather.Forecast.Companion.WIND

data class Forecast(
    val timestamp: Long,
    /** The temperature, in Kelvin **/
    val temperature: Double,
    /** The min temperature, in Kelvin **/
    val minTemp: Double? = null,
    /** The max temperature, in Kelvin **/
    val maxTemp: Double? = null,
    /** The temperature, in hPa **/
    val pressure: Double? = null,
    /** The temperature, in percent **/
    val humidity: Double? = null,
    /** The icon, one of [UNKNOWN], [CLEAR], [OVERCAST], [EXTREME_COLD], [LIGHT_RAIN], [HAZE],
     * [FOG], [HAIL], [EXTREME_HEAT], [PARTLY_CLOUDY], [RAIN], [HEAVY_RAIN] [SLEET], [SNOW],
     * [THUNDER], [THUNDERSTORM], [WIND]**/
    val icon: Int,
    /** A text describing the current weather condition **/
    val condition: String,
    /** The clouds, percentage **/
    val clouds: Int? = null,
    /** Wind speed, in m/s **/
    val windSpeed: Double? = null,
    /** wind direction, in degrees **/
    val windDirection: Double? = null,
    /** rain, in mm per hour **/
    val precipitation: Double? = null,
    /** UV index **/
    val uvIndex: Double? = null,
    /** whether this forecast is during nighttime (whether a moon icon should be used instead of sun) **/
    val night: Boolean = false,
    /** Location string **/
    val location: String,
    /** Provider name **/
    val provider: String,
    /** Url to the provider and more weather information **/
    val providerUrl: String = "",
    /** Rain probability, in percent [0..100]. null if not available **/
    val precipProbability: Int? = null,
    /** Timestamp (in millis) when this forecast was created **/
    val updateTime: Long
) {
    fun toDatabaseEntity(): ForecastEntity {
        return ForecastEntity(
            timestamp = timestamp,
            clouds = clouds ?: -1,
            condition = condition,
            humidity = humidity ?: -1.0,
            icon = icon,
            location = location,
            maxTemp = maxTemp ?: -1.0,
            minTemp = minTemp ?: -1.0,
            night = night,
            pressure = pressure ?: -1.0,
            provider = provider,
            providerUrl = providerUrl,
            precipitation = precipitation ?: -1.0,
            precipProbability = precipProbability ?: -1,
            temperature = temperature,
            updateTime = updateTime,
            windDirection = windDirection ?: -1.0,
            windSpeed = windSpeed ?: -1.0,
            snow = -1.0,
            uvIndex = uvIndex ?: -1.0,
        )
    }

    constructor(entity: ForecastEntity) : this(
        timestamp = entity.timestamp,
        clouds = entity.clouds.takeIf { it >= 0 },
        condition = entity.condition,
        humidity = entity.humidity.takeIf { it >= 0.0 },
        icon = entity.icon,
        location = entity.location,
        maxTemp = entity.maxTemp.takeIf { it >= 0.0 },
        minTemp = entity.minTemp.takeIf { it >= 0.0 },
        night = entity.night,
        pressure = entity.pressure.takeIf { it >= 0.0 },
        provider = entity.provider,
        providerUrl = entity.providerUrl,
        precipitation = entity.precipitation.takeIf { it >= 0.0 },
        precipProbability = entity.precipProbability.takeIf { it >= 0 },
        temperature = entity.temperature,
        updateTime = entity.updateTime,
        windDirection = entity.windDirection.takeIf { it >= 0.0 },
        windSpeed = entity.windSpeed.takeIf { it >= 0.0 },
        uvIndex = entity.uvIndex.takeIf { it >= 0.0 },
    )

    companion object {
        const val UNKNOWN = -1
        const val CLEAR = 0
        const val OVERCAST = 1
        const val EXTREME_COLD = 2
        const val LIGHT_RAIN = 3
        const val HAZE = 4
        const val FOG = 5
        const val HAIL = 6
        const val EXTREME_HEAT = 9

        const val PARTLY_CLOUDY = 11
        const val RAIN = 12
        const val SLEET = 13
        const val SNOW = 14
        const val THUNDER = 16
        const val THUNDERSTORM = 17
        const val WIND = 18
        const val HEAVY_RAIN = 20

        @Deprecated("Deprecated", ReplaceWith("THUNDER"))
        const val HEAVY_THUNDERSTORM = 7

        @Deprecated("Deprecated", ReplaceWith("THUNDERSTORM"))
        const val HEAVY_THUNDERSTORM_WITH_RAIN = 8

        @Deprecated("Deprecated", ReplaceWith("PARTLY_CLOUDY"))
        const val MOSTLY_CLOUDY = 10

        @Deprecated("Deprecated", ReplaceWith("WIND"))
        const val STORM = 15

        @Deprecated("Deprecated", ReplaceWith("PARTLY_CLOUDY"))
        const val BROKEN_CLOUDS = 19

    }
}