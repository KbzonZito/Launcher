package dev.kbzon.launcher.ui.launcher.search.files

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.search.File
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.CompactPermissionRow
import dev.kbzon.launcher.ui.launcher.search.common.ShowAllButton
import dev.kbzon.launcher.ui.launcher.search.common.list.ListItem
import dev.kbzon.launcher.ui.launcher.search.common.list.ListResults
import kotlin.math.min

fun LazyListScope.FileResults(
    files: List<File>,
    missingPermission: Boolean,
    onPermissionRequest: () -> Unit,
    onPermissionRequestRejected: () -> Unit,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    highlightedItem: File? = null,
    reverse: Boolean,
    truncate: Boolean,
    onShowAll: () -> Unit,
) {
    ListResults(
        items = files.subList(0, if (truncate) min(5, files.size) else files.size),
        key = "file",
        reverse = reverse,
        selectedIndex = selectedIndex,
        itemContent = { file, showDetails, index ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth(),
                item = file,
                showDetails = showDetails,
                onShowDetails = { onSelect(if (it) index else -1) },
                highlight = highlightedItem?.key == file.key
            )
        },
        before = if (missingPermission) {
            {
                CompactPermissionRow(
                    category = stringResource(R.string.fork_category_files),
                    onGrant = onPermissionRequest,
                    onTurnOff = onPermissionRequestRejected,
                )
            }
        } else null,
        after = if (truncate && files.size > 5) {
            {
                ShowAllButton(onShowAll = onShowAll)
            }
        } else null
    )
}