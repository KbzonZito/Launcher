package dev.kbzon.launcher.widgets

import android.content.Context
import dev.kbzon.launcher.database.entities.PartialWidgetEntity
import dev.kbzon.launcher.database.entities.WidgetEntity
import dev.kbzon.launcher.ktx.decodeFromStringOrNull
import kotlinx.serialization.json.Json
import java.util.UUID

sealed class Widget {

    abstract val id: UUID
    internal fun toDatabaseEntity(position: Int, parentId: UUID? = null): WidgetEntity {
        return toDatabaseEntity().let {
            WidgetEntity(
                id = it.id,
                type = it.type,
                config = it.config,
                position = position,
                parentId = parentId,
            )
        }
    }

    abstract fun getLabel(context: Context): String

    abstract fun toDatabaseEntity(): PartialWidgetEntity

    companion object {
        fun fromDatabaseEntity(entity: WidgetEntity): Widget? {
            return when (entity.type) {
                WeatherWidget.Type -> {
                    val config: WeatherWidgetConfig =
                        Json.decodeFromStringOrNull(entity.config?.takeIf { it.isNotBlank() })
                            ?: WeatherWidgetConfig()
                    WeatherWidget(entity.id, config)
                }
                MusicWidget.Type -> MusicWidget(
                    entity.id,
                    Json.decodeFromStringOrNull(entity.config?.takeIf { it.isNotBlank() })
                        ?: MusicWidgetConfig(),
                )
                CalendarWidget.Type -> {
                    val config: CalendarWidgetConfig =
                        Json.decodeFromStringOrNull(entity.config?.takeIf { it.isNotBlank() })
                            ?: CalendarWidgetConfig()
                    CalendarWidget(entity.id, config)
                }
                AppsWidget.Type -> {
                    val config: FavoritesWidgetConfig =
                        Json.decodeFromStringOrNull(entity.config?.takeIf { it.isNotBlank() })
                            ?: FavoritesWidgetConfig()
                    AppsWidget(entity.id, config)
                }
                AppWidget.Type -> {
                    val config: AppWidgetConfig =
                        Json.decodeFromStringOrNull(entity.config?.takeIf { it.isNotBlank() })
                            ?: return null
                    AppWidget(
                        entity.id,
                        config,
                    )
                }
                NotesWidget.Type -> {
                    val config: NotesWidgetConfig =
                        Json.decodeFromStringOrNull(entity.config?.takeIf { it.isNotBlank() })
                            ?: NotesWidgetConfig()
                    NotesWidget(entity.id, config)
                }

                else -> null
            }
        }
    }
}



enum class WidgetType(val value: String) {
    INTERNAL("internal"),
    THIRD_PARTY("3rdparty")
}