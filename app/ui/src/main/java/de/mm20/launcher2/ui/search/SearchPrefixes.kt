package de.mm20.launcher2.ui.search

import de.mm20.launcher2.search.SearchFilters

/**
 * [fork] Prefijos de búsqueda: una letra + espacio limita la búsqueda a una categoría.
 *
 *   a whatsapp   → solo apps
 *   c ana        → solo contactos
 *   e dentista   → solo eventos del calendario
 *   f factura    → solo archivos
 *   s nuevo chat → solo atajos de apps
 *   t 3 usd      → solo herramientas (calculadora, conversor)
 *
 * Si después del prefijo no hay texto, se busca normalmente.
 */
object SearchPrefixes {
    data class Parsed(val query: String, val category: Category)

    enum class Category(val prefix: Char) {
        Apps('a'), Contacts('c'), Events('e'), Files('f'), Shortcuts('s'), Tools('t');
    }

    fun parse(query: String): Parsed? {
        if (query.length < 3 || query[1] != ' ') return null
        val category = Category.entries.firstOrNull { it.prefix == query[0].lowercaseChar() } ?: return null
        val rest = query.substring(2).trimStart()
        if (rest.isEmpty()) return null
        return Parsed(rest, category)
    }

    fun Parsed.applyTo(filters: SearchFilters): SearchFilters {
        val none = filters.copy(
            apps = false, websites = false, articles = false, places = false, files = false,
            shortcuts = false, contacts = false, events = false, tools = false,
        )
        return when (category) {
            Category.Apps -> none.copy(apps = true)
            Category.Contacts -> none.copy(contacts = true)
            Category.Events -> none.copy(events = true)
            Category.Files -> none.copy(files = true)
            Category.Shortcuts -> none.copy(shortcuts = true)
            Category.Tools -> none.copy(tools = true)
        }
    }
}
