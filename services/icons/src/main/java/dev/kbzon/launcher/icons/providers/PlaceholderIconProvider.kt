package dev.kbzon.launcher.icons.providers

import android.content.Context
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.search.SavableSearchable

class PlaceholderIconProvider(val context: Context) : IconProvider {
    override suspend fun getIcon(searchable: SavableSearchable, size: Int): LauncherIcon {
        return searchable.getPlaceholderIcon(context)
    }
}