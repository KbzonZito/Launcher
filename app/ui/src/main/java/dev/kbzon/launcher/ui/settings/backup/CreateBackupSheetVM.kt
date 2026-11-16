package dev.kbzon.launcher.ui.settings.backup

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.backup.BackupManager
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CreateBackupSheetVM : ViewModel(), KoinComponent {

    private val backupManager: BackupManager by inject()

    val state = mutableStateOf(CreateBackupState.Ready)

    fun reset() {
        state.value = CreateBackupState.Ready
    }

    /** [fork] Contraseña opcional elegida antes de escoger el archivo. */
    var password: String = ""

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            state.value = CreateBackupState.BackingUp
            backupManager.backup(uri, password.takeIf { it.isNotEmpty() }?.toCharArray())
            password = ""
            state.value = CreateBackupState.BackedUp
        }
    }
}

enum class CreateBackupState {
    Ready,
    BackingUp,
    BackedUp,
}