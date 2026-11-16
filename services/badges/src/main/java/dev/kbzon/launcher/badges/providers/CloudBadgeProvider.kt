package dev.kbzon.launcher.badges.providers

import android.content.Context
import dev.kbzon.launcher.badges.Badge
import dev.kbzon.launcher.badges.BadgeIcon
import dev.kbzon.launcher.badges.MutableBadge
import dev.kbzon.launcher.search.File
import dev.kbzon.launcher.search.Searchable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CloudBadgeProvider(
    private val context: Context
) : BadgeProvider {
    override fun getBadge(searchable: Searchable): Flow<Badge?> {
        if (searchable is File) {
            val iconResId = searchable.providerIconRes
            if (iconResId != null) {
                return flowOf(MutableBadge(icon = BadgeIcon(context.getDrawable(iconResId)!!)))
            }
        }
        return flowOf(null)
    }
}