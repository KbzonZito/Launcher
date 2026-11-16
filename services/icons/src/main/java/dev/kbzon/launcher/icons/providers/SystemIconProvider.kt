package dev.kbzon.launcher.icons.providers

import android.content.Context
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.search.SavableSearchable

class SystemIconProvider(
    private val context: Context,
    private val themedIcons: Boolean,
) : IconProvider {
    override suspend fun getIcon(searchable: SavableSearchable, size: Int): LauncherIcon? {
        return searchable.loadIcon(context, size, themedIcons)
    }
}