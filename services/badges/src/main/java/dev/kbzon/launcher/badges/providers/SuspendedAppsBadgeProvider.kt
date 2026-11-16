package dev.kbzon.launcher.badges.providers

import dev.kbzon.launcher.badges.Badge
import dev.kbzon.launcher.badges.BadgeIcon
import dev.kbzon.launcher.badges.MutableBadge
import dev.kbzon.launcher.badges.R
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.Searchable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent

class SuspendedAppsBadgeProvider : BadgeProvider, KoinComponent {

    override fun getBadge(searchable: Searchable): Flow<Badge?> {
        return if (searchable is Application && searchable.isSuspended) {
            flowOf(MutableBadge(icon = BadgeIcon(R.drawable.hourglass_bottom_20px)))
        } else {
            flowOf(null)
        }
    }
}