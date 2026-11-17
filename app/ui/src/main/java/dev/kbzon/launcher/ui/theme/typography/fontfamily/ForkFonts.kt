package dev.kbzon.launcher.ui.theme.typography.fontfamily

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import dev.kbzon.launcher.preferences.home.AppFont
import dev.kbzon.launcher.ui.R

/** [fork] Familia tipográfica de la fuente elegida, o null para dejar la del tema. */
fun fontFamilyOf(font: AppFont): FontFamily? = when (font) {
    AppFont.Theme -> null
    AppFont.System -> FontFamily.Default
    AppFont.Manrope -> createVariableFontFamily(R.font.manrope, emptyMap())
    AppFont.Inter -> createVariableFontFamily(R.font.inter, emptyMap())
}

/**
 * Fuente de la app para los textos que no salen de [Typography] (la home usa estilos propios).
 * null = la por defecto de la plataforma.
 */
val LocalAppFontFamily = staticCompositionLocalOf<FontFamily?> { null }

private fun TextStyle.withFamily(f: FontFamily) = copy(fontFamily = f)

fun Typography.withFontFamily(f: FontFamily): Typography = copy(
    displayLarge = displayLarge.withFamily(f), displayMedium = displayMedium.withFamily(f), displaySmall = displaySmall.withFamily(f),
    headlineLarge = headlineLarge.withFamily(f), headlineMedium = headlineMedium.withFamily(f), headlineSmall = headlineSmall.withFamily(f),
    titleLarge = titleLarge.withFamily(f), titleMedium = titleMedium.withFamily(f), titleSmall = titleSmall.withFamily(f),
    bodyLarge = bodyLarge.withFamily(f), bodyMedium = bodyMedium.withFamily(f), bodySmall = bodySmall.withFamily(f),
    labelLarge = labelLarge.withFamily(f), labelMedium = labelMedium.withFamily(f), labelSmall = labelSmall.withFamily(f),
    displayLargeEmphasized = displayLargeEmphasized.withFamily(f), displayMediumEmphasized = displayMediumEmphasized.withFamily(f),
    displaySmallEmphasized = displaySmallEmphasized.withFamily(f), headlineLargeEmphasized = headlineLargeEmphasized.withFamily(f),
    headlineMediumEmphasized = headlineMediumEmphasized.withFamily(f), headlineSmallEmphasized = headlineSmallEmphasized.withFamily(f),
    titleLargeEmphasized = titleLargeEmphasized.withFamily(f), titleMediumEmphasized = titleMediumEmphasized.withFamily(f),
    titleSmallEmphasized = titleSmallEmphasized.withFamily(f), bodyLargeEmphasized = bodyLargeEmphasized.withFamily(f),
    bodyMediumEmphasized = bodyMediumEmphasized.withFamily(f), bodySmallEmphasized = bodySmallEmphasized.withFamily(f),
    labelLargeEmphasized = labelLargeEmphasized.withFamily(f), labelMediumEmphasized = labelMediumEmphasized.withFamily(f),
    labelSmallEmphasized = labelSmallEmphasized.withFamily(f),
)
