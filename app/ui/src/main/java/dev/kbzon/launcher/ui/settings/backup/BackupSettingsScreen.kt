package dev.kbzon.launcher.ui.settings.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.common.RestoreBackupSheet
import dev.kbzon.launcher.ui.component.preferences.Preference
import dev.kbzon.launcher.ui.component.preferences.PreferenceCategory
import dev.kbzon.launcher.ui.component.preferences.PreferenceScreen
import kotlinx.serialization.Serializable

@Serializable
data object BackupSettingsRoute : NavKey

@Composable
fun BackupSettingsScreen() {

    var restoreUri by remember { mutableStateOf<Uri?>(null) }

    var showBackupSheet by remember { mutableStateOf(false) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = {
            restoreUri = it
        }
    )

    PreferenceScreen(stringResource(R.string.preference_screen_backup)) {
        item {
            PreferenceCategory {
                Preference(
                    title = stringResource(id = R.string.preference_backup),
                    summary = stringResource(id = R.string.preference_backup_summary),
                    onClick = {
                        showBackupSheet = true
                    })
                Preference(
                    title = stringResource(id = R.string.preference_restore),
                    summary = stringResource(id = R.string.preference_restore_summary),
                    onClick = {
                        restoreLauncher.launch(arrayOf("*/*"))
                    })
            }
        }
    }

    val uri = restoreUri

    RestoreBackupSheet(uri = uri, onDismissRequest = { restoreUri = null })

    CreateBackupSheet(
        expanded = showBackupSheet,
        onDismissRequest = {
            showBackupSheet = false
        })
}