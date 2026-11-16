package dev.kbzon.launcher.ui.launcher.scaffold

import androidx.compose.runtime.compositionLocalOf

enum class ScaffoldPage {
    Home,
    Secondary,
}

val LocalScaffoldPage = compositionLocalOf<ScaffoldPage?> { null }