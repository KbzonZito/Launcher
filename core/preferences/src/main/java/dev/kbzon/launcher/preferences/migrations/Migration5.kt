package dev.kbzon.launcher.preferences.migrations

import androidx.datastore.core.DataMigration
import dev.kbzon.launcher.preferences.BaseLayout
import dev.kbzon.launcher.preferences.GestureAction
import dev.kbzon.launcher.preferences.LauncherSettingsData

class Migration5  : DataMigration<LauncherSettingsData>  {
    override suspend fun cleanUp() {
    }

    override suspend fun migrate(currentData: LauncherSettingsData): LauncherSettingsData {
        return currentData.copy(
            schemaVersion = 5,
            gesturesSwipeDown = if (currentData.uiBaseLayout == BaseLayout.PullDown) GestureAction.Search else currentData.gesturesSwipeDown,
            gesturesSwipeLeft = if (currentData.uiBaseLayout == BaseLayout.Pager) GestureAction.Search else currentData.gesturesSwipeLeft,
            gesturesSwipeRight = if (currentData.uiBaseLayout == BaseLayout.PagerReversed) GestureAction.Search else currentData.gesturesSwipeRight,
            gesturesSwipeUp = GestureAction.Widgets(),
            homeScreenWidgets = !currentData.clockWidgetFillHeight,
        )
    }

    override suspend fun shouldMigrate(currentData: LauncherSettingsData): Boolean {
        return currentData.schemaVersion < 5
    }
}
