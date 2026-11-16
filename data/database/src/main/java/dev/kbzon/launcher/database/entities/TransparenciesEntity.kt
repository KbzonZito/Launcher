package dev.kbzon.launcher.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import dev.kbzon.launcher.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "Transparencies")
data class TransparenciesEntity(
    @Serializable(with = UUIDSerializer::class) @PrimaryKey val id: UUID,
    val name: String,

    val background: Float?,
    val surface: Float?,
    val elevatedSurface: Float?,
)