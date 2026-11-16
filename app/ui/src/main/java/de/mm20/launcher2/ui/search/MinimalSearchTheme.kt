package de.mm20.launcher2.ui.search

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import de.mm20.launcher2.preferences.ui.UiSettings
import de.mm20.launcher2.themes.ThemeRepository
import de.mm20.launcher2.ui.locals.LocalDarkTheme
import de.mm20.launcher2.ui.theme.colorscheme.darkColorSchemeOf
import de.mm20.launcher2.ui.theme.transparency.LocalTransparencyScheme
import de.mm20.launcher2.ui.theme.transparency.TransparencyScheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.compose.koinInject

/**
 * [fork] Tema de la búsqueda minimalista: siempre oscuro (texto claro sobre el velo oscuro,
 * igual que la home sobre el fondo de pantalla) y filas casi transparentes en vez de tarjetas.
 * Mantiene los colores, formas y tipografía del tema elegido por el usuario.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun MinimalSearchTheme(content: @Composable () -> Unit) {
    val uiSettings: UiSettings = koinInject()
    val themeRepository: ThemeRepository = koinInject()
    val themeColors by remember {
        uiSettings.colorsId.flatMapLatest { themeRepository.colors.getOrDefault(it) }
    }.collectAsState(null)

    val colors = themeColors ?: run { content(); return }

    CompositionLocalProvider(
        LocalDarkTheme provides true,
        LocalTransparencyScheme provides TransparencyScheme(
            background = 0.88f,
            surface = 0.07f,
            elevatedSurface = 0.14f,
        ),
    ) {
        MaterialExpressiveTheme(
            colorScheme = darkColorSchemeOf(colors),
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes,
            content = content,
        )
    }
}
