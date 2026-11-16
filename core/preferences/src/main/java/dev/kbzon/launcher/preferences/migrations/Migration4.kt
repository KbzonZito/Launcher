package dev.kbzon.launcher.preferences.migrations

import androidx.datastore.core.DataMigration
import dev.kbzon.launcher.preferences.LauncherSettingsData

class Migration4 : DataMigration<LauncherSettingsData> {
    override suspend fun cleanUp() {
    }

    override suspend fun migrate(currentData: LauncherSettingsData): LauncherSettingsData {
        return currentData.copy(
            schemaVersion = 4,
            contactSearchProviders = setOfNotNull(
                if (currentData.contactSearchEnabled) "local" else null,
            )
        )
    }

    override suspend fun shouldMigrate(currentData: LauncherSettingsData): Boolean {
        return currentData.schemaVersion < 4
    }

}