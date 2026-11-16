package dev.kbzon.launcher.database.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import dev.kbzon.launcher.ktx.toBytes
import dev.kbzon.launcher.preferences.WidgetScreenTarget

class Migration_31_32 : Migration(31, 32) {

    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            ALTER TABLE `forecasts` ADD COLUMN `uvIndex` REAL NOT NULL DEFAULT -1.0;
            """.trimIndent(),
        )
    }
}
