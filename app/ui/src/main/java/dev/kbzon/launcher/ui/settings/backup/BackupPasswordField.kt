package dev.kbzon.launcher.ui.settings.backup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.ui.R

/**
 * [fork] Paso de contraseña para crear un backup cifrado (opcional) o restaurar uno.
 * @param confirm pide repetir la contraseña (al crear).
 */
@Composable
fun BackupPasswordStep(
    title: String,
    description: String,
    buttonLabel: String,
    confirm: Boolean,
    error: String? = null,
    allowEmpty: Boolean,
    onSubmit: (String) -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    val mismatch = confirm && password.isNotEmpty() && repeat != password
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.fork_backup_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        if (confirm && password.isNotEmpty()) {
            OutlinedTextField(
                value = repeat,
                onValueChange = { repeat = it },
                label = { Text(stringResource(R.string.fork_backup_password_repeat)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = mismatch,
                supportingText = if (mismatch) ({ Text(stringResource(R.string.fork_backup_password_mismatch)) }) else null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onSubmit(password) },
            enabled = !mismatch && (allowEmpty || password.isNotEmpty()),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(buttonLabel) }
    }
}
