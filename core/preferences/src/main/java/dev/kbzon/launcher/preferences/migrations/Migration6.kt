package dev.kbzon.launcher.preferences.migrations

import androidx.datastore.core.DataMigration
import dev.kbzon.launcher.preferences.BatteryStatusVisibility
import dev.kbzon.launcher.preferences.LauncherSettingsData

class Migration6 : DataMigration<LauncherSettingsData> {
    override suspend fun cleanUp() {
    }

    override suspend fun shouldMigrate(currentData: LauncherSettingsData): Boolean {
        return currentData.schemaVersion < 6
    }

    override suspend fun migrate(currentData: LauncherSettingsData): LauncherSettingsData {
        return currentData.copy(
            schemaVersion = 6,
            clockWidgetBatteryPart = if (currentData._clockWidgetBatteryPart) {
                BatteryStatusVisibility.Show
            } else {
                BatteryStatusVisibility.Hide
            }
        )
    }
}
