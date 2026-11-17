package dev.kbzon.launcher.preferences.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** [fork] Fuente de toda la app (home, búsqueda, menús y ajustes). */
@Serializable
enum class AppFont {
    /** La del tema de tipografía elegido en Ajustes → Apariencia (Kvaesitso). */
    @SerialName("theme") Theme,
    @SerialName("system") System,
    @SerialName("manrope") Manrope,
    @SerialName("inter") Inter,
}

/** [fork] Estilo de los íconos (cuando están visibles). */
@Serializable
enum class HomeIconStyle {
    @SerialName("original") Original,
    /** Glifo monocromo (capa monocroma de Android 13+) sin fondo, del color del texto. */
    @SerialName("monochrome") Monochrome,
    /** Glifo monocromo dentro de un contorno fino. */
    @SerialName("outline") Outline,
}
