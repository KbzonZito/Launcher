package dev.kbzon.launcher.preferences

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import dev.kbzon.launcher.preferences.migrations.Migration2
import dev.kbzon.launcher.preferences.migrations.Migration3
import dev.kbzon.launcher.preferences.migrations.Migration4
import dev.kbzon.launcher.preferences.migrations.Migration5
import dev.kbzon.launcher.preferences.migrations.Migration6
import dev.kbzon.launcher.settings.BaseSettings

internal class LauncherDataStore(
    private val context: Context,
): BaseSettings<LauncherSettingsData>(
    context,
    fileName = "settings.json",
    serializer = LauncherSettingsDataSerializer(context),
    migrations = listOf(
        Migration2(),
        Migration3(),
        Migration4(),
        Migration5(),
        Migration6(),
    ),
    corruptionHandler = ReplaceFileCorruptionHandler { LauncherSettingsData() }
) {

    val data
        get() = context.dataStore.data

    fun update(block: (LauncherSettingsData) -> LauncherSettingsData) {
        updateData(block)
    }
}
