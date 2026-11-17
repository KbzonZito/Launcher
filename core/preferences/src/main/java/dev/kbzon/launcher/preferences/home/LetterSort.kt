package dev.kbzon.launcher.preferences.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** [fork] Orden de las apps dentro de cada letra del riel. */
@Serializable
enum class LetterSort {
    @SerialName("alphabetical") Alphabetical,
    /** Más abiertas primero (contador total de aperturas). */
    @SerialName("most_used") MostUsed,
    /** Usadas recientemente primero (peso de uso con decaimiento de Kvaesitso). */
    @SerialName("recent") Recent,
    /** Instaladas más recientemente primero. */
    @SerialName("newest") Newest,
}
