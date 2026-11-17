package dev.kbzon.launcher.calendar

import dev.kbzon.launcher.plugin.config.StorageStrategy
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import dev.kbzon.launcher.calendar.providers.AndroidCalendarEvent
import dev.kbzon.launcher.calendar.providers.AndroidCalendarProvider
import dev.kbzon.launcher.calendar.providers.TasksCalendarEvent
import dev.kbzon.launcher.calendar.providers.TasksCalendarProvider
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.SearchableSerializer
import dev.kbzon.launcher.search.UpdateResult
import dev.kbzon.launcher.search.asUpdateResult
import dev.kbzon.launcher.serialization.Json
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.json.JSONObject

class AndroidCalendarEventSerializer: SearchableSerializer {
    override fun serialize(searchable: SavableSearchable): String {
        searchable as AndroidCalendarEvent
        val json = JSONObject()
        json.put("id", searchable.id)
        return json.toString()
    }

    override val typePrefix: String
        get() = AndroidCalendarEvent.Domain
}

class AndroidCalendarEventDeserializer(val context: Context): SearchableDeserializer {
    override suspend fun deserialize(serialized: String): SavableSearchable? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) return null
        val json = JSONObject(serialized)
        val id = json.getLong("id")
        return AndroidCalendarProvider(context).get(id)
    }
}

class TasksCalendarEventSerializer: SearchableSerializer {
    override fun serialize(searchable: SavableSearchable): String {
        searchable as TasksCalendarEvent
        val json = JSONObject()
        json.put("id", searchable.id)
        return json.toString()
    }

    override val typePrefix: String
        get() = TasksCalendarEvent.Domain
}

class TasksCalendarEventDeserializer(val context: Context): SearchableDeserializer {
    override suspend fun deserialize(serialized: String): SavableSearchable? {
        if (ContextCompat.checkSelfPermission(context, "org.tasks.permission.READ_TASKS") != PackageManager.PERMISSION_GRANTED) return null
        val json = JSONObject(serialized)
        val id = json.getLong("id")
        return TasksCalendarProvider(context).get(id)
    }
}



@Serializable
internal data class SerializedCalendarEvent(
    val id: String? = null,
    val authority: String? = null,
    val color: Int? = null,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val allDay: Boolean? = null,
    val description: String? = null,
    val calendarName: String? = null,
    val location: String? = null,
    val attendees: List<String>? = null,
    val uri: String? = null,
    val completed: Boolean? = null,
    val label: String? = null,
    val timestamp: Long = 0L,
    val strategy: StorageStrategy = StorageStrategy.StoreCopy,
)

