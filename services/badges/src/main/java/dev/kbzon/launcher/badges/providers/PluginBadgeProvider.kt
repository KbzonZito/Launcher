package dev.kbzon.launcher.badges.providers

import android.content.Context
import dev.kbzon.launcher.badges.Badge
import dev.kbzon.launcher.badges.BadgeIcon
import dev.kbzon.launcher.badges.MutableBadge
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.Searchable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class PluginBadgeProvider(private val context: Context): BadgeProvider {
    override fun getBadge(searchable: Searchable): Flow<Badge?> {
        if (searchable !is SavableSearchable) return flowOf(null)
        return flow {
            emit(null)
            val icon = searchable.getProviderIcon(context)
            if (icon != null) {
                emit(MutableBadge(icon = BadgeIcon(icon)))
            }
        }
    }
}