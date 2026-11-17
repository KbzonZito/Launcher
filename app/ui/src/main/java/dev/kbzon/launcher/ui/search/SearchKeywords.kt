package dev.kbzon.launcher.ui.search

import dev.kbzon.launcher.searchactions.builders.AppSearchActionBuilder
import dev.kbzon.launcher.searchactions.builders.CustomWebsearchActionBuilder
import dev.kbzon.launcher.searchactions.builders.SearchActionBuilder

/**
 * [fork] Fase 14: atajos de búsqueda con palabra clave.
 *
 *   yt gatos   → buscar "gatos" en YouTube
 *   p spotify  → buscar en Google Play
 *   g clima    → búsqueda web
 *
 * Cada acción de búsqueda (Ajustes → Búsqueda → Acciones) puede tener una palabra clave;
 * si el usuario no la cambió, se sugiere una según la acción. Enter lanza esa acción.
 */
object SearchKeywords {

    /** Palabra clave por defecto de una acción, o null si no tiene una evidente. */
    fun defaultKeyword(builder: SearchActionBuilder): String? {
        val label = builder.label.lowercase()
        val target = when (builder) {
            is CustomWebsearchActionBuilder -> builder.urlTemplate.lowercase()
            is AppSearchActionBuilder -> builder.key.lowercase()
            else -> builder.key.lowercase()
        }
        return when {
            builder.key == "websearch" -> "g"
            "youtube" in target || "youtube" in label -> "yt"
            "com.android.vending" in target || "play" in label -> "p"
            "wikipedia" in target || "wikipedia" in label -> "w"
            "maps" in target || "maps" in label -> "m"
            "spotify" in target || "spotify" in label -> "sp"
            "amazon" in target || "amazon" in label -> "az"
            "duckduckgo" in target -> "dd"
            else -> null
        }
    }

    /** Palabra clave efectiva: la elegida por el usuario ("" = ninguna) o la sugerida. */
    fun keywordFor(builder: SearchActionBuilder, overrides: Map<String, String>): String? {
        val custom = overrides[builder.key]
        return if (custom != null) custom.trim().lowercase().ifEmpty { null } else defaultKeyword(builder)
    }

    data class Match(val builder: SearchActionBuilder, val query: String)

    /** "yt gatos" → (YouTube, "gatos"). Necesita palabra clave + espacio + texto. */
    fun match(query: String, builders: List<SearchActionBuilder>, overrides: Map<String, String>): Match? {
        val space = query.indexOf(' ')
        if (space <= 0) return null
        val word = query.substring(0, space).lowercase()
        val rest = query.substring(space + 1).trim()
        if (rest.isEmpty()) return null
        val builder = builders.firstOrNull { keywordFor(it, overrides) == word } ?: return null
        return Match(builder, rest)
    }
}
