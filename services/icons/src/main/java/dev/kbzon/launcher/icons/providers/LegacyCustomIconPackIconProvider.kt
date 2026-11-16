package dev.kbzon.launcher.icons.providers

import dev.kbzon.launcher.data.customattrs.LegacyCustomIconPackIcon
import dev.kbzon.launcher.icons.IconPackManager
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.search.SavableSearchable

@Deprecated("Use CustomIconPackIconProvider instead")
class LegacyCustomIconPackIconProvider(
    private val customIcon: LegacyCustomIconPackIcon,
    private val iconPackManager: IconPackManager,
) : IconProvider {
    override suspend fun getIcon(searchable: SavableSearchable, size: Int): LauncherIcon? {
        return iconPackManager.getIcon(
            customIcon.iconPackPackage,
            customIcon.iconPackageName,
            customIcon.iconActivityName,
            customIcon.allowThemed,
        )
    }
}