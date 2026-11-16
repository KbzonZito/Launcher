package dev.kbzon.launcher.widgets

import android.content.Context
import dev.kbzon.launcher.database.entities.PartialWidgetEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class MusicWidgetConfig(
    val interactiveProgressBar: Boolean = false
)

data class MusicWidget(
    override val id: UUID,
    val config: MusicWidgetConfig = MusicWidgetConfig(),
) : Widget() {
    override fun toDatabaseEntity(): PartialWidgetEntity {
        return PartialWidgetEntity(
            id = id,
            type = Type,
            config = Json.encodeToString(config),
        )
    }

    override fun getLabel(context: Context): String {
        return context.getString(R.string.widget_name_music)
    }

    companion object {
        const val Type = "music"
    }
}