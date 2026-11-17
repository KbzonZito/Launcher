package dev.kbzon.launcher.weather

import android.content.Context
import android.util.Log
import androidx.work.*
import dev.kbzon.launcher.database.AppDatabase
import dev.kbzon.launcher.devicepose.DevicePoseProvider
import dev.kbzon.launcher.ktx.or
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.plugin.PluginApi
import dev.kbzon.launcher.plugin.PluginRepository
import dev.kbzon.launcher.plugin.PluginType
import dev.kbzon.launcher.preferences.LatLon
import dev.kbzon.launcher.preferences.weather.WeatherLocation
import dev.kbzon.launcher.preferences.weather.WeatherSettings
import dev.kbzon.launcher.weather.breezy.BreezyWeatherProvider
import dev.kbzon.launcher.weather.brightsky.BrightSkyProvider
import dev.kbzon.launcher.weather.metno.MetNoProvider
import dev.kbzon.launcher.weather.openweathermap.OpenWeatherMapProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

interface WeatherRepository {
    fun getActiveProvider(): Flow<WeatherProviderInfo?>
    fun getProviders(): Flow<List<WeatherProviderInfo>>
    fun searchLocations(query: String): Flow<List<WeatherLocation>>

    fun getForecasts(limit: Int? = null): Flow<List<Forecast>>
    fun getDailyForecasts(): Flow<List<DailyForecast>>

    fun deleteForecasts()
}

internal class WeatherRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase,
    private val settings: WeatherSettings,
    private val pluginRepository: PluginRepository
) : WeatherRepository, KoinComponent {

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private val permissionsManager: PermissionsManager by inject()

    private val hasLocationPermission = permissionsManager.hasPermission(PermissionGroup.Location)

    override fun getForecasts(limit: Int?): Flow<List<Forecast>> {
        return database.weatherDao().getForecasts(limit ?: 99999)
            .map { it.map { Forecast(it) } }
    }

    override fun getDailyForecasts(): Flow<List<DailyForecast>> {
        return database.weatherDao().getForecasts()
            .map { it.map { Forecast(it) } }
            .map {
                groupForecastsPerDay(it)
            }
    }

    override fun searchLocations(query: String): Flow<List<WeatherLocation>> {
        return settings.map {
            val provider = WeatherProvider.getInstance(it.provider)
            provider.findLocation(query)
        }
    }

    init {

        scope.launch {
            hasLocationPermission.collectLatest {
                if (it) requestUpdate()
            }
        }
        scope.launch {
            settings.collectLatest {
                val provider =  WeatherProvider.getInstance(it.provider)
                val weatherRequest =
                    PeriodicWorkRequestBuilder<WeatherUpdateWorker>(Duration.ofMillis(provider.getUpdateInterval()))
                        .build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "weather",
                    ExistingPeriodicWorkPolicy.UPDATE, weatherRequest
                )
                requestUpdate()
            }
        }
    }

    private fun groupForecastsPerDay(forecasts: List<Forecast>): List<DailyForecast> {
        val dailyForecasts = mutableListOf<DailyForecast>()
        val calendar = Calendar.getInstance()
        var currentDay = 0
        var currentDayForecasts: MutableList<Forecast> = mutableListOf()
        for (fc in forecasts) {
            calendar.timeInMillis = fc.timestamp
            if (currentDay != calendar.get(Calendar.DAY_OF_YEAR)) {
                if (currentDayForecasts.isNotEmpty()) {
                    dailyForecasts.add(
                        DailyForecast(
                            timestamp = currentDayForecasts.first().timestamp,
                            minTemp = currentDayForecasts.minByOrNull { it.temperature }?.temperature
                                ?: 0.0,
                            maxTemp = currentDayForecasts.maxByOrNull { it.temperature }?.temperature
                                ?: 0.0,
                            hourlyForecasts = currentDayForecasts
                        )
                    )
                    currentDayForecasts = mutableListOf()
                }
                currentDay = calendar.get(Calendar.DAY_OF_YEAR)
            }
            currentDayForecasts.add(fc)
        }
        if (currentDayForecasts.isNotEmpty()) {
            dailyForecasts.add(
                DailyForecast(
                    timestamp = currentDayForecasts.first().timestamp,
                    minTemp = currentDayForecasts.minByOrNull { it.temperature }?.temperature
                        ?: 0.0,
                    maxTemp = currentDayForecasts.maxByOrNull { it.temperature }?.temperature
                        ?: 0.0,
                    hourlyForecasts = currentDayForecasts
                )
            )
        }
        return dailyForecasts
    }


    private fun requestUpdate() {
        val weatherRequest = OneTimeWorkRequestBuilder<WeatherUpdateWorker>()
            .addTag("weather")
            .build()
        WorkManager.getInstance(context).enqueue(weatherRequest)
    }

    override fun deleteForecasts() {
        scope.launch {
            withContext(Dispatchers.IO) {
                database.weatherDao().deleteAll()
                settings.setLastUpdate(0L)
            }
        }
    }

    override fun getActiveProvider(): Flow<WeatherProviderInfo?> {
        return settings.providerId.flatMapLatest { id ->
            getProviders().map {
                it.find { it.id == id }
            }
        }
    }

    override fun getProviders(): Flow<List<WeatherProviderInfo>> {
        val providers = mutableListOf<WeatherProviderInfo>()
        providers.add(
            WeatherProviderInfo(
                BrightSkyProvider.Id,
                context.getString(R.string.provider_brightsky)
            )
        )
        if (OpenWeatherMapProvider.isAvailable(context)) {
            providers.add(
                WeatherProviderInfo(
                    OpenWeatherMapProvider.Id,
                    context.getString(R.string.provider_openweathermap)
                )
            )
        }
        if (MetNoProvider.isAvailable(context)) {
            providers.add(
                WeatherProviderInfo(
                    MetNoProvider.Id,
                    context.getString(R.string.provider_metno)
                )
            )
        }
        if (BreezyWeatherProvider.isAvailable(context)) {
            providers.add(
                WeatherProviderInfo(
                    BreezyWeatherProvider.Id,
                    context.getString(R.string.provider_breezy),
                    managedLocation = true
                )
            )
        }
        val pluginProviders = pluginRepository.findMany(type = PluginType.Weather, enabled = true)
        return pluginProviders.map {
            providers + it.mapNotNull {
                val config = PluginApi(it.authority, context.contentResolver).getWeatherPluginConfig() ?: return@mapNotNull null
                WeatherProviderInfo(it.authority, it.label, config.managedLocation)
            }
        }
    }
}

class WeatherUpdateWorker(
    val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val appDatabase: AppDatabase by inject()
    private val settings: WeatherSettings by inject()
    private val locationProvider: DevicePoseProvider by inject()
    private val privacySettings: dev.kbzon.launcher.preferences.privacy.PrivacySettings by inject() // [fork]

    override suspend fun doWork(): Result {
        Log.d("WeatherUpdateWorker", "Requesting weather data")
        val settingsData = settings.first()
        val provider = WeatherProvider.getInstance(settingsData.provider)

        // [fork] Breezy Weather y los plugins leen de apps locales; el resto usa internet.
        if (settingsData.provider in setOf("metno", "owm", "brightsky") && privacySettings.offlineMode.first()) {
            Log.d("WeatherUpdateWorker", "Offline mode: skipping weather update")
            return Result.success()
        }

        val updateInterval = provider.getUpdateInterval()
        val lastUpdate = settingsData.lastUpdate

        if (lastUpdate + updateInterval > System.currentTimeMillis()) {
            Log.d("WeatherUpdateWorker", "No weather update required")
            return Result.failure()
        }

        val weatherData = if (settingsData.autoLocation) {
            val latLon = getLastKnownLocation() ?: settingsData.lastLocation
            if (latLon == null) {
                Log.e("WeatherUpdateWorker", "Could not get location")
                return Result.failure()
            }
            settings.setLastLocation(latLon)
            provider.getWeatherData(latLon.lat, latLon.lon)
        } else {
            val location = settings.location.first()
            if (location == null) {
                Log.e("WeatherUpdateWorker", "Location not set")
                return Result.failure()
            }
            provider.getWeatherData(location)
        }

        return if (weatherData == null) {
            Log.w("WeatherUpdateWorker", "Weather update failed")
            Result.retry()
        } else {
            Log.i("WeatherUpdateWorker", "Weather update succeeded")
            val in7Days = System.currentTimeMillis() + Duration.ofDays(7).toMillis()
            appDatabase.weatherDao()
                .replaceAll(weatherData.takeWhile { it.timestamp < in7Days  }.map { it.toDatabaseEntity() })
            settings.setLastUpdate(System.currentTimeMillis())
            Result.success()
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun getLastKnownLocation(): LatLon? = locationProvider.getLocation(skipCache = true)
        .timeout(30.seconds) // [fork] antes 10 min: el clima quedaba esperando la ubicación
        .firstOrNull()
        .or { locationProvider.lastCachedLocation }
        ?.let { LatLon(it.latitude, it.longitude) }
}