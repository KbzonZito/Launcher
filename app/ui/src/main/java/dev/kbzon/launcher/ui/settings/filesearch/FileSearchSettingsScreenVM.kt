package dev.kbzon.launcher.ui.settings.filesearch

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.search.FileSearchSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FileSearchSettingsScreenVM : ViewModel(), KoinComponent {
    private val fileSearchSettings: FileSearchSettings by inject()
    private val permissionsManager: PermissionsManager by inject()

    val hasFilePermission = permissionsManager.hasPermission(PermissionGroup.ExternalStorage)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val loading = mutableStateOf(true)




    fun onResume() {
        viewModelScope.launch {
            loading.value = false
        }
    }

    val localFiles = fileSearchSettings.localFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    fun setLocalFiles(localFiles: Boolean) {
        fileSearchSettings.setLocalFiles(localFiles)
    }

    val gdrive = fileSearchSettings.gdriveFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    fun setGdrive(gdrive: Boolean) {
        fileSearchSettings.setGdriveFiles(gdrive)
    }

    fun requestFilePermission(context: AppCompatActivity) {
        permissionsManager.requestPermission(context, PermissionGroup.ExternalStorage)
    }

}