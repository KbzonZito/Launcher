package dev.kbzon.launcher.ui.settings.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.preferences.ColorScheme
import dev.kbzon.launcher.preferences.ui.UiSettings
import dev.kbzon.launcher.themes.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppearanceSettingsScreenVM : ViewModel(), KoinComponent {
    private val uiSettings: UiSettings by inject()

    private val themeRepository: ThemeRepository by inject()

    val colorScheme = uiSettings.colorScheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setColorScheme(colorScheme: ColorScheme) {
        uiSettings.setColorScheme(colorScheme)
    }

    val colorThemeName = uiSettings.colorsId.flatMapLatest {
        themeRepository.colors.getOrDefault(it)
    }.map {
        it.name
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val shapeThemeName = uiSettings.shapesId.flatMapLatest {
        themeRepository.shapes.getOrDefault(it)
    }.map {
        it.name
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val typographyThemeName = uiSettings.typographyId.flatMapLatest {
        themeRepository.typographies.getOrDefault(it)
    }.map {
        it.name
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val transparencyThemeName = uiSettings.transparenciesId.flatMapLatest {
        themeRepository.transparencies.getOrDefault(it)
    }.map {
        it.name
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val compatModeColors = uiSettings.compatModeColors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun setCompatModeColors(enabled: Boolean) {
        uiSettings.setCompatModeColors(enabled)
    }
}