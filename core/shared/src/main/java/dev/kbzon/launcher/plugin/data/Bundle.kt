package dev.kbzon.launcher.plugin.data

import android.os.Bundle
import dev.kbzon.launcher.plugin.contracts.Column

operator fun <T> Bundle.get(column: Column<T>): T? {
    return with(column) {
        if (!containsKey(column.name)) return null
        get()
    }
}

operator fun <T> Bundle.set(column: Column<T>, value: T?) {
    with(column) {
        put(value)
    }
}