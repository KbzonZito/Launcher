package dev.kbzon.launcher.ui.settings.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbzon.launcher.applications.AppRepository
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.icons.IconService
import dev.kbzon.launcher.music.MusicService
import dev.kbzon.launcher.preferences.home.MinimalHomeSettings
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.DismissableBottomSheet
import dev.kbzon.launcher.ui.component.ShapedLauncherIcon
import org.koin.compose.koinInject
import java.text.Collator

/**
 * [fork] F16: elegir a mano las apps de la fila de música (y de la marca ♪ en la lista).
 * Arriba las elegidas, en su orden; luego las detectadas como reproductores; luego el resto.
 * Sin ninguna marcada se usa la detección automática.
 */
@Composable
internal fun MusicAppsSheet(expanded: Boolean, onDismiss: () -> Unit) {
    DismissableBottomSheet(expanded = expanded, onDismissRequest = onDismiss) {
        val settings: MinimalHomeSettings = koinInject()
        val appRepository: AppRepository = koinInject()
        val musicService: MusicService = koinInject()
        val iconService: IconService = koinInject()
        val chosen by settings.musicApps.collectAsStateWithLifecycle(emptyList())
        val apps by remember { appRepository.findMany() }.collectAsStateWithLifecycle(emptyList())
        val players by produceState<Set<String>?>(null) { value = musicService.getInstalledPlayerPackages() }

        // El orden se fija una sola vez, cuando ya cargaron las apps y los reproductores,
        // para que las filas no salten al marcar.
        var ordered by remember { mutableStateOf<List<Application>>(emptyList()) }
        val detected = players.orEmpty()
        if (ordered.isEmpty() && apps.isNotEmpty() && players != null) {
            val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
            val unique = apps.distinctBy { it.componentName.packageName }
                .sortedWith { a, b -> collator.compare(a.label, b.label) }
            val (found, rest) = unique.partition { it.componentName.packageName in detected }
            val chosenFirst = chosen.mapNotNull { p -> unique.firstOrNull { it.componentName.packageName == p } }
            ordered = (chosenFirst + found + rest).distinctBy { it.componentName.packageName }
        }

        Column {
            Text(
                stringResource(R.string.fork_music_apps_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp),
            )
            Text(
                stringResource(R.string.fork_music_apps_help),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            TextButton(
                onClick = { settings.setMusicApps(emptyList()) },
                enabled = chosen.isNotEmpty(),
                modifier = Modifier.padding(horizontal = 12.dp),
            ) { Text(stringResource(R.string.fork_preference_linked_app_auto)) }

            val iconPx = with(LocalDensity.current) { 32.dp.roundToPx() }
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(ordered, key = { it.componentName.packageName }) { app ->
                    val pkg = app.componentName.packageName
                    val checked = pkg in chosen
                    val toggle = { settings.setMusicApps(if (checked) chosen - pkg else chosen + pkg) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = toggle)
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                    ) {
                        val icon by remember(app.key) { iconService.getIcon(app, iconPx) }.collectAsState(null)
                        ShapedLauncherIcon(size = 32.dp, icon = { icon })
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(app.label, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (pkg in detected) {
                                Text(
                                    stringResource(R.string.fork_music_apps_detected),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        val position = chosen.indexOf(pkg)
                        if (position >= 0) {
                            Text("${position + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                        }
                        Checkbox(checked = checked, onCheckedChange = { toggle() })
                    }
                }
            }
        }
    }
}
