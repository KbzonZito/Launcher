package dev.kbzon.launcher.ui.settings.weather

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.weather.WeatherSettings
import dev.kbzon.launcher.weather.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherIntegrationSettingsScreenVM : ViewModel(), KoinComponent {
    private val repository: WeatherRepository by inject()
    private val weatherSettings: WeatherSettings by inject()
    private val permissionsManager: PermissionsManager by inject()

    val availableProviders = repository.getProviders()

    val weatherProvider = weatherSettings.providerId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setWeatherProvider(provider: String) {
        weatherSettings.setProvider(provider)
    }

    val autoLocation = weatherSettings.autoLocation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun setAutoLocation(autoLocation: Boolean) {
        weatherSettings.setAutoLocation(autoLocation)
    }

    val location = weatherSettings.autoLocation.flatMapLatest {
        if (it) {
            repository.getForecasts(limit = 1).map { it.firstOrNull()?.location }
        } else {
            weatherSettings.location.map { it?.name }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val hasLocationPermission = permissionsManager.hasPermission(PermissionGroup.Location)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun requestLocationPermission(activity: AppCompatActivity) {
        permissionsManager.requestPermission(activity, PermissionGroup.Location)
    }

    fun clearWeatherData() {
        repository.deleteForecasts()
    }

}