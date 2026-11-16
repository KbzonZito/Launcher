package dev.kbzon.launcher.themes.colors

import android.content.Context
import dev.kbzon.launcher.database.AppDatabase
import dev.kbzon.launcher.themes.BlackAndWhiteThemeId
import dev.kbzon.launcher.themes.DefaultThemeId
import dev.kbzon.launcher.themes.HighContrastThemeId
import dev.kbzon.launcher.themes.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class ColorsRepository(
    private val context: Context,
    private val database: AppDatabase,
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun getAll(): Flow<List<Colors>> {
        return database.themeDao().getAllColors().map {
            getBuiltIn() + it.map { Colors(it) }
        }
    }

    fun get(id: UUID): Flow<Colors?> {
        if (id == DefaultThemeId) return flowOf(default)
        if (id == HighContrastThemeId) return flowOf(highContrast)
        if (id == BlackAndWhiteThemeId) return flowOf(blackAndWhite)
        return database.themeDao().getColors(id).map { it?.let { Colors(it) } }
            .flowOn(Dispatchers.Default)
    }

    fun create(colors: Colors) {
        scope.launch {
            database.themeDao().insertColors(colors.toEntity())
        }
    }

    fun update(colors: Colors) {
        scope.launch {
            database.themeDao().updateColors(colors.toEntity())
        }
    }


    fun delete(colors: Colors) {
        scope.launch {
            database.themeDao().deleteColors(colors.id)
        }
    }

    fun getOrDefault(id: UUID?): Flow<Colors> {
        if (id == null) return flowOf(default)
        return get(id).map { it ?: default }
    }

    private fun getBuiltIn(): List<Colors> {
        return listOf(
            default,
            highContrast,
            blackAndWhite,
        )
    }

    private val default: Colors
        get() = Colors(
            id = DefaultThemeId,
            builtIn = true,
            name = context.getString(R.string.preference_colors_default),
            corePalette = EmptyCorePalette,
            lightColorScheme = DefaultLightColorScheme,
            darkColorScheme = DefaultDarkColorScheme,
        )


    private val highContrast: Colors
        get() = Colors(
            id = HighContrastThemeId,
            builtIn = true,
            name = context.getString(R.string.preference_colors_high_contrast),
            corePalette = EmptyCorePalette,
            lightColorScheme = HighContrastLightColorScheme,
            darkColorScheme = HighContrastDarkColorScheme,
        )


    private val blackAndWhite: Colors
        get() = Colors(
            id = BlackAndWhiteThemeId,
            builtIn = true,
            name = context.getString(R.string.preference_colors_bw),
            corePalette = EmptyCorePalette,
            lightColorScheme = BlackAndWhiteLightColorScheme,
            darkColorScheme = BlackAndWhiteDarkColorScheme,
        )
}