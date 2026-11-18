package dev.kbzon.launcher.weather.openmeteo

import dev.kbzon.launcher.serialization.Json
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
internal data class OpenMeteoResult(
    val hourly: OpenMeteoHourly? = null,
)

@Serializable
internal data class OpenMeteoHourly(
    val time: List<Long> = emptyList(),
    @SerialName("temperature_2m") val temperature: List<Double?> = emptyList(),
    @SerialName("relative_humidity_2m") val humidity: List<Double?> = emptyList(),
    @SerialName("precipitation_probability") val precipProbability: List<Int?> = emptyList(),
    val precipitation: List<Double?> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName("pressure_msl") val pressure: List<Double?> = emptyList(),
    @SerialName("cloud_cover") val cloudCover: List<Int?> = emptyList(),
    @SerialName("wind_speed_10m") val windSpeed: List<Double?> = emptyList(),
    @SerialName("wind_direction_10m") val windDirection: List<Double?> = emptyList(),
    @SerialName("uv_index") val uvIndex: List<Double?> = emptyList(),
    @SerialName("is_day") val isDay: List<Int?> = emptyList(),
)

/** [fork] F16: Open-Meteo — global, gratis y sin clave (https://open-meteo.com). */
internal class OpenMeteoApi {

    private val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json.Lenient)
            }
            defaultRequest {
                url("https://api.open-meteo.com/")
            }
        }
    }

    suspend fun forecast(lat: Double, lon: Double): OpenMeteoResult {
        return httpClient.get {
            url {
                path("v1", "forecast")
                // Coordenadas redondeadas a ~1 km, como el resto de proveedores.
                parameter("latitude", String.format(Locale.ROOT, "%.2f", lat))
                parameter("longitude", String.format(Locale.ROOT, "%.2f", lon))
                parameter(
                    "hourly",
                    "temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,weather_code," +
                        "pressure_msl,cloud_cover,wind_speed_10m,wind_direction_10m,uv_index,is_day"
                )
                parameter("wind_speed_unit", "ms")
                parameter("timeformat", "unixtime")
                parameter("forecast_days", 7)
                parameter("past_hours", 1)
            }
        }.body()
    }
}
