package dev.kbzon.launcher.preferences.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** [fork] Cuánto se agranda y desplaza la letra bajo el dedo en el riel. */
@Serializable
enum class WaveIntensity(val bulgeDp: Float, val scale: Float) {
    @SerialName("soft") Soft(32f, 1.2f),
    @SerialName("normal") Normal(56f, 1.6f),
    @SerialName("strong") Strong(80f, 2.0f),
}
