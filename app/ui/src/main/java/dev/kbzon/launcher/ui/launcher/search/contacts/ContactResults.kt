package dev.kbzon.launcher.ui.launcher.search.contacts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.search.Contact
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.CompactPermissionRow
import dev.kbzon.launcher.ui.launcher.search.common.ShowAllButton
import dev.kbzon.launcher.ui.launcher.search.common.list.ListItem
import dev.kbzon.launcher.ui.launcher.search.common.list.ListResults
import kotlin.math.min

fun LazyListScope.ContactResults(
    contacts: List<Contact>,
    missingPermission: Boolean,
    onPermissionRequest: () -> Unit,
    onPermissionRequestRejected: () -> Unit,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    highlightedItem: Contact?,
    reverse: Boolean,
    truncate: Boolean,
    onShowAll: () -> Unit,
) {
    ListResults(
        items = contacts.subList(0, if (truncate) min(5, contacts.size) else contacts.size),
        key = "contact",
        reverse = reverse,
        selectedIndex = selectedIndex,
        itemContent = { contact, showDetails, index ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth(),
                item = contact,
                showDetails = showDetails,
                onShowDetails = { onSelect(if (it) index else -1) },
                highlight = highlightedItem?.key == contact.key
            )
        },
        before = if (missingPermission) {
            {
                CompactPermissionRow(
                    category = stringResource(R.string.fork_category_contacts),
                    onGrant = onPermissionRequest,
                    onTurnOff = onPermissionRequestRejected,
                )
            }
        } else null,
        after = if (truncate && contacts.size > 5) {
            {
                ShowAllButton(onShowAll = onShowAll)
            }
        } else null
    )
}