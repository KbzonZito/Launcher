package dev.kbzon.launcher.database.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import dev.kbzon.launcher.ktx.toBytes
import dev.kbzon.launcher.preferences.WidgetScreenTarget

class Migration_30_31 : Migration(30, 31) {

    override suspend fun migrate(connection: SQLiteConnection) {

        connection.prepare(
            """
            UPDATE Widget 
            SET parentId = ? 
            WHERE parentId IS NULL    
            """.trimIndent()
        ).use {
            it.bindBlob(1, WidgetScreenTarget.Default.id.toBytes())
            it.step()
        }
    }
}
