package dev.kbzon.launcher.ui.launcher.search.calendar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.search.CalendarEvent
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.CompactPermissionRow
import dev.kbzon.launcher.ui.launcher.search.common.ShowAllButton
import dev.kbzon.launcher.ui.launcher.search.common.list.ListItem
import dev.kbzon.launcher.ui.launcher.search.common.list.ListResults
import kotlin.math.min

fun LazyListScope.CalendarResults(
    events: List<CalendarEvent>,
    missingPermission: Boolean,
    onPermissionRequest: () -> Unit,
    onPermissionRequestRejected: () -> Unit,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    highlightedItem: CalendarEvent?,
    reverse: Boolean,
    truncate: Boolean,
    onShowAll: () -> Unit,
) {
    ListResults(
        items = events.subList(0, if (truncate) min(5, events.size) else events.size),
        key = "calendar",
        reverse = reverse,
        selectedIndex = selectedIndex,
        itemContent = { calendar, showDetails, index ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth(),
                item = calendar,
                showDetails = showDetails,
                onShowDetails = { onSelect(if(it) index else -1) },
                highlight = highlightedItem?.key == calendar.key
            )
        },
        before = if (missingPermission) {
            {
                CompactPermissionRow(
                    category = stringResource(R.string.fork_category_calendar),
                    onGrant = onPermissionRequest,
                    onTurnOff = onPermissionRequestRejected,
                )
            }
        } else null,
        after = if (truncate && events.size > 5) {
            {
                ShowAllButton(onShowAll = onShowAll)
            }
        } else null
    )
}