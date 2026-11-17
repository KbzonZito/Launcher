package dev.kbzon.launcher.ui.settings.homescreen

import android.content.Context
import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.kbzon.launcher.ktx.isAtLeastApiLevel
import dev.kbzon.launcher.preferences.GestureAction
import dev.kbzon.launcher.preferences.ScreenOrientation
import dev.kbzon.launcher.preferences.SearchBarColors
import dev.kbzon.launcher.preferences.SearchBarStyle
import dev.kbzon.launcher.preferences.SystemBarColors
import dev.kbzon.launcher.preferences.ui.ClockWidgetSettings
import dev.kbzon.launcher.preferences.ui.GestureSettings
import dev.kbzon.launcher.preferences.ui.UiSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class HomescreenSettingsScreenVM(
    private val uiSettings: UiSettings,
    private val clockWidgetSettings: ClockWidgetSettings,
    private val gestureSettings: GestureSettings,
) : ViewModel() {

    val dimWallpaper = uiSettings.dimWallpaper
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun setDimWallpaper(dimWallpaper: Boolean) {
        uiSettings.setDimWallpaper(dimWallpaper)
    }

    val blurWallpaper = uiSettings.blurWallpaper
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun setBlurWallpaper(blurWallpaper: Boolean) {
        uiSettings.setBlurWallpaper(blurWallpaper)
    }

    val blurWallpaperRadius = uiSettings.wallpaperBlurRadius
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 32)

    fun setBlurWallpaperRadius(blurWallpaperRadius: Int) {
        uiSettings.setWallpaperBlurRadius(blurWallpaperRadius)
    }

    fun openWallpaperChooser(context: AppCompatActivity) {
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER), null))
    }

    fun isBlurAvailable(context: Context): Boolean {
        if (!isAtLeastApiLevel(31)) return false
        return context.getSystemService<WindowManager>()?.isCrossWindowBlurEnabled == true
    }

    val statusBarIcons = uiSettings.statusBarColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setLightStatusBar(statusBarColor: SystemBarColors) {
        uiSettings.setStatusBarColor(statusBarColor)
    }

    val navBarIcons = uiSettings.navigationBarColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setLightNavBar(navBarColors: SystemBarColors) {
        uiSettings.setNavigationBarColor(navBarColors)
    }

    val hideStatusBar = uiSettings.hideStatusBar
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setHideStatusBar(hideStatusBar: Boolean) {
        uiSettings.setHideStatusBar(hideStatusBar)
    }

    val hideNavBar = uiSettings.hideNavigationBar
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setHideNavBar(hideNavBar: Boolean) {
        uiSettings.setHideNavigationBar(hideNavBar)
    }

    val fixedRotation = uiSettings.orientation.map { it != ScreenOrientation.Auto }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setFixedRotation(fixedRotation: Boolean) {
        uiSettings.setOrientation(if (fixedRotation) ScreenOrientation.Portrait else ScreenOrientation.Auto)
    }

    val widgetEditButton = uiSettings.widgetEditButton
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setWidgetEditButton(editButton: Boolean) {
        uiSettings.setWidgetEditButton(editButton)
    }

    val chargingAnimation = uiSettings.chargingAnimation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setChargingAnimation(chargingAnimation: Boolean) {
        uiSettings.setChargingAnimation(chargingAnimation)
    }

    companion object : KoinComponent {
        val Factory = viewModelFactory {
            initializer {
                HomescreenSettingsScreenVM(
                    uiSettings = get(),
                    clockWidgetSettings = get(),
                    gestureSettings = get(),
                )
            }
        }
    }
}
