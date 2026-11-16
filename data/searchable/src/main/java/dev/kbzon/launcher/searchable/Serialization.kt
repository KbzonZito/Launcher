package dev.kbzon.launcher.searchable

import dev.kbzon.launcher.search.SavableSearchable

internal fun SavableSearchable.serialize(): String? {
    val serializer = getSerializer()
    return serializer.serialize(this)
}
