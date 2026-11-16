package dev.kbzon.launcher.ui.settings.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.DismissableBottomSheet
import dev.kbzon.launcher.ui.component.LargeMessage
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CreateBackupSheet(
    expanded: Boolean,
    onDismissRequest: () -> Unit
) {
    DismissableBottomSheet(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        val viewModel: CreateBackupSheetVM = viewModel()

        val backupLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/vnd.dev.kbzon.launcher.backup"),
            onResult = {
                if (it != null) viewModel.createBackup(it)
                else onDismissRequest()
            }
        )
        LaunchedEffect(null) {
            viewModel.reset()
        }
        // [fork] Antes de elegir el archivo: contraseña opcional para cifrar el backup.
        val startBackup: (String) -> Unit = { password ->
            viewModel.password = password
            val fileName = "launcher-${
                ZonedDateTime.now().format(
                    DateTimeFormatter.ISO_INSTANT
                ).replace(":", "_")
            }.backup"
            backupLauncher.launch(fileName)
        }

        val state by viewModel.state
        AnimatedContent(
            state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (it == CreateBackupState.Ready) {
                BackupPasswordStep(
                    title = stringResource(R.string.fork_backup_create_title),
                    description = stringResource(R.string.fork_backup_create_description),
                    buttonLabel = stringResource(R.string.fork_backup_create_button),
                    confirm = true,
                    allowEmpty = true,
                    onSubmit = startBackup,
                )
            } else if (it == CreateBackupState.BackingUp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else if (it == CreateBackupState.BackedUp) {
                LargeMessage(
                    modifier = Modifier.aspectRatio(1f),
                    icon = R.drawable.check_circle_48px,
                    text = stringResource(
                        id = R.string.backup_complete
                    )
                )
            }
        }
    }
}

