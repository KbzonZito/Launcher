package dev.kbzon.launcher.backup

import java.io.File

interface Backupable {
    suspend fun backup(toDir: File)
    suspend fun restore(fromDir: File)
}