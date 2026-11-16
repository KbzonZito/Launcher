package dev.kbzon.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.kbzon.launcher.preferences.ui.UiSettings
import dev.kbzon.launcher.themes.ThemeRepository
import dev.kbzon.launcher.ui.locals.LocalDarkTheme
import dev.kbzon.launcher.ui.theme.colorscheme.darkColorSchemeOf
import dev.kbzon.launcher.ui.theme.colorscheme.lightColorSchemeOf
import dev.kbzon.launcher.ui.theme.shapes.shapesOf
import dev.kbzon.launcher.ui.theme.transparency.LocalTransparencyScheme
import dev.kbzon.launcher.ui.theme.transparency.transparencySchemeOf
import dev.kbzon.launcher.ui.theme.typography.typographyOf
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.compose.koinInject
import dev.kbzon.launcher.preferences.ColorScheme as ColorSchemePref


@Composable
fun LauncherTheme(
    content: @Composable () -> Unit
) {
    val uiSettings: UiSettings = koinInject()
    val themeRepository: ThemeRepository = koinInject()

    val themeColors by remember {
        uiSettings.colorsId.flatMapLatest {
            themeRepository.colors.getOrDefault(it)
        }
    }.collectAsState(null)

    val themeShapes by remember {
        uiSettings.shapesId.flatMapLatest {
            themeRepository.shapes.getOrDefault(it)
        }
    }.collectAsState(null)

    val themeTypography by remember {
        uiSettings.typographyId.flatMapLatest {
            themeRepository.typographies.getOrDefault(it)
        }
    }.collectAsState(null)

    val themeTransparencies by remember {
        uiSettings.transparenciesId.flatMapLatest {
            themeRepository.transparencies.getOrDefault(it)
        }
    }.collectAsState(null)

    val colorSchemePref by remember { uiSettings.colorScheme }.collectAsState(
        ColorSchemePref.System
    )
    val darkTheme =
        colorSchemePref == ColorSchemePref.Dark || colorSchemePref == ColorSchemePref.System && isSystemInDarkTheme()

    if (themeColors == null || themeShapes == null || themeTransparencies == null || themeTypography == null) {
        return
    }

    val colorScheme = if (darkTheme) {
        darkColorSchemeOf(themeColors!!)
    } else {
        lightColorSchemeOf(themeColors!!)
    }

    val shapes = shapesOf(themeShapes!!)
    val typography = typographyOf(themeTypography!!)

    val transparencyScheme = transparencySchemeOf(themeTransparencies!!)


    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalTransparencyScheme provides transparencyScheme,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

