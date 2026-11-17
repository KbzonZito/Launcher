package dev.kbzon.launcher.themes.colors

import android.content.Context
import dev.kbzon.launcher.database.AppDatabase
import dev.kbzon.launcher.themes.BlackAndWhiteThemeId
import dev.kbzon.launcher.themes.DefaultThemeId
import dev.kbzon.launcher.themes.HighContrastThemeId
import dev.kbzon.launcher.themes.GraphiteThemeId
import dev.kbzon.launcher.themes.MidnightOledThemeId
import dev.kbzon.launcher.themes.PaperThemeId
import dev.kbzon.launcher.themes.ForestThemeId
import dev.kbzon.launcher.themes.SandThemeId
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
        forkPresets.firstOrNull { it.id == id }?.let { return flowOf(it) }
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
        ) + forkPresets
    }

    /**
     * [fork] Temas propios: paletas de semilla fija (no dependen del fondo de pantalla),
     * con tonos neutros de poca saturación para el estilo minimalista con tarjetas.
     */
    private val forkPresets: List<Colors>
        get() = listOf(
            preset(GraphiteThemeId, R.string.fork_theme_graphite, 0xFF7D8794, 0xFF878B92, 0xFF928A97, 0xFF8E8F93, 0xFF8C9096),
            preset(
                MidnightOledThemeId, R.string.fork_theme_midnight, 0xFF6F8FD8, 0xFF7C869C, 0xFF9A88C4, 0xFF8C8D93, 0xFF8A8E99,
                dark = OledDarkColorScheme,
            ),
            preset(PaperThemeId, R.string.fork_theme_paper, 0xFF8B6F4E, 0xFF8F8270, 0xFF6F8466, 0xFF958F86, 0xFF968E83),
            preset(ForestThemeId, R.string.fork_theme_forest, 0xFF4F7A5A, 0xFF6B7D6C, 0xFF4C7A80, 0xFF8D918C, 0xFF8B928A),
            preset(SandThemeId, R.string.fork_theme_sand, 0xFFB08A55, 0xFF9D8C73, 0xFF7E9070, 0xFF97918A, 0xFF99907F),
        )

    private fun preset(
        id: UUID,
        name: Int,
        primary: Long,
        secondary: Long,
        tertiary: Long,
        neutral: Long,
        neutralVariant: Long,
        dark: ColorScheme = DefaultDarkColorScheme,
    ) = Colors(
        id = id,
        builtIn = true,
        name = context.getString(name),
        corePalette = CorePalette(
            primary = primary.toInt(),
            secondary = secondary.toInt(),
            tertiary = tertiary.toInt(),
            neutral = neutral.toInt(),
            neutralVariant = neutralVariant.toInt(),
        ),
        lightColorScheme = DefaultLightColorScheme,
        darkColorScheme = dark,
    )

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