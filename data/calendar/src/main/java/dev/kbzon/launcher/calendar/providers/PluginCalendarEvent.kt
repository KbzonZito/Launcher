package dev.kbzon.launcher.calendar.providers

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import dev.kbzon.launcher.calendar.PluginCalendarEventSerializer
import dev.kbzon.launcher.ktx.tryStartActivity
import dev.kbzon.launcher.plugin.config.StorageStrategy
import dev.kbzon.launcher.search.CalendarEvent
import dev.kbzon.launcher.search.File
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.SearchableSerializer
import dev.kbzon.launcher.search.UpdatableSearchable
import dev.kbzon.launcher.search.UpdateResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PluginCalendarEvent(
    val id: String,
    val authority: String,
    override val color: Int?,
    override val startTime: Long?,
    override val endTime: Long,
    override val allDay: Boolean,
    override val description: String?,
    override val calendarName: String?,
    override val location: String?,
    override val attendees: List<String>,
    val uri: Uri,
    override val isCompleted: Boolean?,
    override val label: String,
    override val labelOverride: String? = null,
    override val timestamp: Long,
    internal val storageStrategy: StorageStrategy,
    override val updatedSelf: (suspend (SavableSearchable) -> UpdateResult<CalendarEvent>)?,
) : CalendarEvent, UpdatableSearchable<CalendarEvent> {
    override val domain: String = Domain

    override val key: String
        get() = "$domain://$authority:$id"

    override fun overrideLabel(label: String): SavableSearchable {
        return copy(
            labelOverride = label
        )
    }

    override fun launch(context: Context, options: Bundle?): Boolean {
        return context.tryStartActivity(
            Intent(
                Intent.ACTION_VIEW
            ).apply {
                data = uri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }, options
        )
    }

    override fun getSerializer(): SearchableSerializer {
        return PluginCalendarEventSerializer()
    }

    override suspend fun getProviderIcon(context: Context): Drawable? {
        return withContext(Dispatchers.IO) {
            context.packageManager.resolveContentProvider(authority, 0)?.loadIcon(context.packageManager)
        }
    }

    companion object {
        const val Domain = "plugin.calendar"
    }
}