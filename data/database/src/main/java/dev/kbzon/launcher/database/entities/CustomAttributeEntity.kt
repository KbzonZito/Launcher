package dev.kbzon.launcher.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "CustomAttributes")
data class CustomAttributeEntity(
    val key: String,
    val type: String,
    val value: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
)