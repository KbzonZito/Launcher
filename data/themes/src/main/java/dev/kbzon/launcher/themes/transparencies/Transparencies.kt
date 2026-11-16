package dev.kbzon.launcher.themes.transparencies

import dev.kbzon.launcher.database.entities.TransparenciesEntity
import dev.kbzon.launcher.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Transparencies(
    @Serializable(with = UUIDSerializer::class) val id: UUID = UUID.randomUUID(),
    val builtIn: Boolean = false,
    val name: String,
    val background: Float? = null,
    val surface: Float? = null,
    val elevatedSurface: Float? = null,
) {
    constructor(entity: TransparenciesEntity) : this(
        id = entity.id,
        builtIn = false,
        name = entity.name,
        background = entity.background,
        surface = entity.surface,
        elevatedSurface = entity.elevatedSurface,
    )

    internal fun toEntity(): TransparenciesEntity {
        return TransparenciesEntity(
            id = id,
            name = name,
            background = background,
            surface = surface,
            elevatedSurface = elevatedSurface,
        )
    }
}