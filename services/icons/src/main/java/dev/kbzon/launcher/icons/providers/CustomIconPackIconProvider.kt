package dev.kbzon.launcher.icons.providers

import dev.kbzon.launcher.data.customattrs.CustomIconPackIcon
import dev.kbzon.launcher.database.entities.IconEntity
import dev.kbzon.launcher.icons.IconPackAppIcon
import dev.kbzon.launcher.icons.IconPackManager
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.search.SavableSearchable

class CustomIconPackIconProvider(
    private val customIcon: CustomIconPackIcon,
    private val iconPackManager: IconPackManager,
) : IconProvider {
    override suspend fun getIcon(searchable: SavableSearchable, size: Int): LauncherIcon? {
        val ent = IconEntity(
            type = customIcon.type,
            drawable = customIcon.drawable,
            extras = customIcon.extras,
            iconPack = customIcon.iconPackPackage,
            themed = customIcon.allowThemed,
        )
        val icon = IconPackAppIcon(ent) ?: return null
        return iconPackManager.getIcon(
            customIcon.iconPackPackage,
            icon,
            customIcon.allowThemed,
        )
    }
}