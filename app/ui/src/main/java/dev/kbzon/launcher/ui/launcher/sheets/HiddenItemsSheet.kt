package dev.kbzon.launcher.ui.launcher.sheets

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.ui.component.DismissableBottomSheet
import dev.kbzon.launcher.ui.launcher.search.common.grid.SearchResultGrid

@Composable
fun HiddenItemsSheet(
    expanded: Boolean,
    items: List<SavableSearchable>,
    onDismiss: () -> Unit
) {
    DismissableBottomSheet(expanded = expanded, onDismissRequest = onDismiss) {
        SearchResultGrid(
            items,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding()
        )
    }
}
