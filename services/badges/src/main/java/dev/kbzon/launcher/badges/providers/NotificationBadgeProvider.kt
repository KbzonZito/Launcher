package dev.kbzon.launcher.badges.providers

import android.util.Log
import dev.kbzon.launcher.badges.Badge
import dev.kbzon.launcher.badges.MutableBadge
import dev.kbzon.launcher.notifications.NotificationRepository
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.Searchable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationBadgeProvider : BadgeProvider, KoinComponent {
    private val notificationRepository: NotificationRepository by inject()

    override fun getBadge(searchable: Searchable): Flow<Badge?> {
        if (searchable !is Application) return flowOf(null)

        val packageName = searchable.componentName.packageName
        return notificationRepository.notifications.map {
            it.filter { it.packageName == packageName && it.canShowBadge }
        }.map {
            if (it.isEmpty()) {
                return@map null
            } else {
                val badge = MutableBadge(
                    number = it.sumOf {
                        if (it.canShowBadge && !it.isGroupSummary) it.number
                        else 0
                    },
                    progress = it.mapNotNull {
                        val progress = it.progress ?: return@mapNotNull null
                        val progressMax = it.progressMax ?: return@mapNotNull null
                        return@mapNotNull progress.toFloat() / progressMax.toFloat()
                    }
                        .takeIf { it.isNotEmpty() }
                        ?.let {
                            it.sumOf { it.toDouble() }.toFloat() / it.size
                        }
                )
                return@map badge
            }
        }
    }
}