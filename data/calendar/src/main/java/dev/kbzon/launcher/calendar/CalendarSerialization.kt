package dev.kbzon.launcher.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import dev.kbzon.launcher.calendar.providers.AndroidCalendarEvent
import dev.kbzon.launcher.calendar.providers.AndroidCalendarProvider
import dev.kbzon.launcher.calendar.providers.PluginCalendarEvent
import dev.kbzon.launcher.calendar.providers.PluginCalendarProvider
import dev.kbzon.launcher.calendar.providers.TasksCalendarEvent
import dev.kbzon.launcher.calendar.providers.TasksCalendarProvider
import dev.kbzon.launcher.plugin.PluginRepository
import dev.kbzon.launcher.plugin.config.StorageStrategy
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

class PluginCalendarEventSerializer: SearchableSerializer {
    override fun serialize(searchable: SavableSearchable): String {
        searchable as PluginCalendarEvent
        if (searchable.storageStrategy == StorageStrategy.StoreCopy) {
            return Json.Lenient.encodeToString(
                SerializedCalendarEvent(
                    id = searchable.id,
                    authority = searchable.authority,
                    color = searchable.color,
                    startTime = searchable.startTime,
                    endTime = searchable.endTime,
                    allDay = searchable.allDay,
                    description = searchable.description,
                    calendarName = searchable.calendarName,
                    location = searchable.location,
                    attendees = searchable.attendees,
                    uri = searchable.uri.toString(),
                    completed = searchable.isCompleted,
                    label = searchable.label,
                    strategy = searchable.storageStrategy,
                    timestamp = searchable.timestamp,
                )
            )
        } else {
            return Json.Lenient.encodeToString(
                SerializedCalendarEvent(
                    id = searchable.id,
                    authority = searchable.authority,
                    strategy = searchable.storageStrategy,
                )
            )
        }
    }

    override val typePrefix: String
        get() = PluginCalendarEvent.Domain
}

class PluginCalendarEventDeserializer(
    val context: Context,
    private val pluginRepository: PluginRepository,
): SearchableDeserializer {
    override suspend fun deserialize(serialized: String): SavableSearchable? {
        val json = Json.Lenient.decodeFromString<SerializedCalendarEvent>(serialized)
        val authority = json.authority ?: return null
        val id = json.id ?: return null
        val strategy = json.strategy
        val plugin = pluginRepository.get(authority).firstOrNull() ?: return null
        if (!plugin.enabled) return null

        return when(strategy) {
            StorageStrategy.StoreReference -> {
                PluginCalendarProvider(context, authority).get(id).getOrNull()
            }
            else -> {
                val timestamp = json.timestamp
                PluginCalendarEvent(
                    id = id,
                    color = json.color,
                    startTime = json.startTime,
                    endTime = json.endTime ?: return null,
                    allDay = json.allDay == true,
                    description = json.description,
                    calendarName = json.calendarName,
                    location = json.location,
                    attendees = json.attendees ?: emptyList(),
                    label = json.label ?: return null,
                    uri = Uri.parse(json.uri ?: return null),
                    isCompleted = json.completed,
                    storageStrategy = StorageStrategy.StoreCopy,
                    authority = authority,
                    timestamp = timestamp,
                    updatedSelf = {
                        if (it !is PluginCalendarEvent) UpdateResult.TemporarilyUnavailable()
                        else PluginCalendarProvider(context, authority).refresh(it, timestamp).asUpdateResult()
                    }
                )
            }
        }
    }

}