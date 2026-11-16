package dev.kbzon.launcher.ui.launcher.search.common.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import dev.kbzon.launcher.search.AppShortcut
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.Article
import dev.kbzon.launcher.search.CalendarEvent
import dev.kbzon.launcher.search.Contact
import dev.kbzon.launcher.search.File
import dev.kbzon.launcher.search.Location
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.Website
import dev.kbzon.launcher.ui.ktx.toPixels
import dev.kbzon.launcher.ui.launcher.search.apps.AppItem
import dev.kbzon.launcher.ui.launcher.search.calendar.CalendarItem
import dev.kbzon.launcher.ui.launcher.search.common.SearchableItemVM
import dev.kbzon.launcher.ui.launcher.search.contacts.ContactItem
import dev.kbzon.launcher.ui.launcher.search.files.FileItem
import dev.kbzon.launcher.ui.launcher.search.listItemViewModel
import dev.kbzon.launcher.ui.launcher.search.location.LocationItem
import dev.kbzon.launcher.ui.launcher.search.shortcut.AppShortcutItem
import dev.kbzon.launcher.ui.launcher.search.website.WebsiteItem
import dev.kbzon.launcher.ui.launcher.search.wikipedia.ArticleItem
import dev.kbzon.launcher.ui.locals.LocalGridSettings

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    item: SavableSearchable,
    highlight: Boolean = false,
    showDetails: Boolean,
    onShowDetails: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val viewModel: SearchableItemVM = listItemViewModel(key = "search-${item.key}")
    val iconSize = LocalGridSettings.current.iconSize.dp.toPixels()

    LaunchedEffect(item, iconSize) {
        viewModel.init(item, iconSize.toInt())
    }

    LaunchedEffect(showDetails) {
        if (showDetails) viewModel.requestUpdatedSearchable(context)
    }

    val background by animateColorAsState(
        if (highlight && !showDetails) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(
            alpha = 0f
        )
    )

    var bounds by remember { mutableStateOf(IntRect.Zero) }
    Box(
        modifier = modifier
            .background(background)
            .onGloballyPositioned {
                bounds = it.boundsInWindow().roundToIntRect()
            },
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface
        ) {
            when (item) {
                is Application -> {
                    AppItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 9999.dp) // we have infinite space, but there is an inner scroll that needs a constraint
                            .combinedClickable(
                                enabled = !showDetails,
                                onClick = {
                                    if (!viewModel.launch(context, bounds)) {
                                        onShowDetails(true)
                                    }
                                },
                                onLongClick = { onShowDetails(true) }
                            ),
                        app = item,
                        showDetails = showDetails,
                        onBack = { onShowDetails(false) }
                    )
                }
                is Contact -> {
                    ContactItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                enabled = !showDetails,
                                onClick = { onShowDetails(true) },
                                onLongClick = { onShowDetails(true) }
                            ),
                        contact = item,
                        showDetails = showDetails,
                        onBack = { onShowDetails(false) }
                    )
                }

                is File -> {
                    FileItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                enabled = !showDetails,
                                onClick = {
                                    if (!viewModel.launch(context, bounds)) {
                                        onShowDetails(true)
                                    }
                                },
                                onLongClick = { onShowDetails(true) }
                            ),
                        file = item,
                        showDetails = showDetails,
                        onBack = { onShowDetails(false) }
                    )
                }

                is CalendarEvent -> {
                    CalendarItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                enabled = !showDetails,
                                onClick = { onShowDetails(true) },
                                onLongClick = { onShowDetails(true) }
                            ),
                        calendar = item,
                        showDetails = showDetails,
                        onBack = { onShowDetails(false) }
                    )
                }

                is Location -> {
                    LocationItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                enabled = !showDetails,
                                onClick = { onShowDetails(true) },
                                onLongClick = { onShowDetails(true) }),
                        location = item,
                        showDetails = showDetails,
                        onBack = { onShowDetails(false) }
                    )
                }

                is AppShortcut -> {
                    AppShortcutItem(
                        shortcut = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                enabled = !showDetails,
                                onClick = {
                                    if (!viewModel.launch(context, bounds)) {
                                        onShowDetails(true)
                                    }
                                },
                                onLongClick = { onShowDetails(true) }
                            ),
                        showDetails = showDetails,
                        onBack = { onShowDetails(false) }
                    )
                }

                is Article -> {
                    ArticleItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                enabled = !showDetails,
                                onClick = {
                                    if (!viewModel.launch(context, bounds)) {
                                        onShowDetails(true)
                                    }
                                },
                                onLongClick = { onShowDetails(true) }),
                        showDetails = showDetails,
                        onBack = { onShowDetails(false) },
                        article = item,
                    )
                }

                is Website -> {
                    WebsiteItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                enabled = !showDetails,
                                onClick = {
                                    if (!viewModel.launch(context, bounds)) {
                                        onShowDetails(true)
                                    }
                                },
                                onLongClick = { onShowDetails(true) }),
                        website = item,
                        onBack = { onShowDetails(false) },
                        showDetails = showDetails,
                    )
                }
            }

        }
    }
}