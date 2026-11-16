package dev.kbzon.launcher.preferences.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** [fork] Estilos de reloj de la home minimalista. */
@Serializable
enum class MinimalClockStyle {
    @SerialName("thin") Thin,
    @SerialName("bold") Bold,
    @SerialName("stacked") Stacked,
    @SerialName("words") Words,
    @SerialName("analog") Analog,
    @SerialName("day_progress") DayProgress,
}
