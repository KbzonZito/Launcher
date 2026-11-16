package dev.kbzon.launcher.files.providers

import dev.kbzon.launcher.search.File

internal interface FileProvider {
    suspend fun search(query: String, allowNetwork: Boolean): List<File>
}