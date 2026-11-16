package dev.kbzon.launcher.ktx

inline fun <T> T?.or(block: () -> T?): T? = this ?: block()
