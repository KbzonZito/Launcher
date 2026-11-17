package dev.kbzon.launcher.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.ui.R

@Composable
fun MissingPermissionBanner(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    secondaryAction: @Composable () -> Unit = {}
) {
    Banner(
        modifier = modifier,
        text = text,
        icon = R.drawable.lock_24px,
        primaryAction = {
            Button(
                modifier = Modifier.padding(start = 8.dp),
                onClick = onClick
            ) {
                Text(
                    stringResource(R.string.grant_permission),
                )
            }
        },
        secondaryAction = secondaryAction
    )
}
/**
 * [fork] Fase 14: aviso de permiso en una sola línea para la búsqueda minimalista:
 * candado · "Contactos sin permiso" · Permitir · ×(desactivar esa búsqueda).
 */
@Composable
fun CompactPermissionRow(
    modifier: Modifier = Modifier,
    category: String,
    onGrant: () -> Unit,
    onTurnOff: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.padding(start = 16.dp, end = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Icon(
            androidx.compose.ui.res.painterResource(R.drawable.lock_24px),
            contentDescription = null,
            modifier = Modifier.padding(end = 12.dp).size(18.dp),
            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.fork_permission_missing_short, category),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        androidx.compose.material3.TextButton(onClick = onGrant) {
            Text(stringResource(R.string.fork_permission_allow))
        }
        androidx.compose.material3.IconButton(onClick = onTurnOff) {
            androidx.compose.material3.Icon(
                androidx.compose.ui.res.painterResource(R.drawable.close_24px),
                contentDescription = stringResource(R.string.turn_off),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
