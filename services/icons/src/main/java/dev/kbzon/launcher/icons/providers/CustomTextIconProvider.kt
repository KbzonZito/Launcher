package dev.kbzon.launcher.icons.providers

import dev.kbzon.launcher.data.customattrs.CustomIconPackIcon
import dev.kbzon.launcher.data.customattrs.CustomTextIcon
import dev.kbzon.launcher.icons.ColorLayer
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.icons.StaticLauncherIcon
import dev.kbzon.launcher.icons.TextLayer
import dev.kbzon.launcher.search.SavableSearchable

class CustomTextIconProvider(
    private val customIcon: CustomTextIcon,
): IconProvider {
    override suspend fun getIcon(
        searchable: SavableSearchable,
        size: Int
    ): LauncherIcon? {
        return StaticLauncherIcon(
            foregroundLayer = TextLayer(
                text = customIcon.text,
                color = customIcon.color,
            ),
            backgroundLayer = ColorLayer(
                color = customIcon.color,
            ),
        )
    }

}