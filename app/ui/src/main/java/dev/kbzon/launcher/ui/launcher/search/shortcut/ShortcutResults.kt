package dev.kbzon.launcher.ui.launcher.search.shortcut

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.search.AppShortcut
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.CompactPermissionRow
import dev.kbzon.launcher.ui.launcher.search.common.ShowAllButton
import dev.kbzon.launcher.ui.launcher.search.common.list.ListItem
import dev.kbzon.launcher.ui.launcher.search.common.list.ListResults
import kotlin.math.min

fun LazyListScope.ShortcutResults(
    shortcuts: List<AppShortcut>,
    missingPermission: Boolean,
    onPermissionRequest: () -> Unit,
    onPermissionRequestRejected: () -> Unit,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    highlightedItem: AppShortcut?,
    reverse: Boolean,
    truncate: Boolean,
    onShowAll: () -> Unit,
) {
    ListResults(
        items = shortcuts.subList(0, if (truncate) min(5, shortcuts.size) else shortcuts.size),
        key = "shortcut",
        reverse = reverse,
        selectedIndex = selectedIndex,
        itemContent = { shortcut, showDetails, index ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth(),
                item = shortcut,
                showDetails = showDetails,
                onShowDetails = { onSelect(if (it) index else -1) },
                highlight = highlightedItem?.key == shortcut.key
            )
        },
        before = if (missingPermission) {
            {
                CompactPermissionRow(
                    category = stringResource(R.string.fork_category_shortcuts),
                    onGrant = onPermissionRequest,
                    onTurnOff = onPermissionRequestRejected,
                )
            }
        } else null,
        after = if (truncate && shortcuts.size > 5) {
            {
                ShowAllButton(onShowAll = onShowAll)
            }
        } else null
    )
}