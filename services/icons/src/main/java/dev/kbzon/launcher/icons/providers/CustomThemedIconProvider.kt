package dev.kbzon.launcher.icons.providers

import dev.kbzon.launcher.data.customattrs.CustomThemedIcon
import dev.kbzon.launcher.icons.IconPackManager
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.search.SavableSearchable

class CustomThemedIconProvider(
    private val customIcon: CustomThemedIcon,
    private val iconPackManager: IconPackManager,
): IconProvider {
    override suspend fun getIcon(searchable: SavableSearchable, size: Int): LauncherIcon? {
        return null //iconPackManager.getThemedIcon(customIcon.iconPackageName)
    }
}