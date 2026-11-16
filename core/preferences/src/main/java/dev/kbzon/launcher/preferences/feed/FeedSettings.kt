package dev.kbzon.launcher.preferences.feed

import dev.kbzon.launcher.preferences.GestureAction
import dev.kbzon.launcher.preferences.LauncherDataStore
import kotlinx.coroutines.flow.map

class FeedSettings internal constructor(
    private val launcherDataStore: LauncherDataStore,
) {

    val enabled
        get() = launcherDataStore.data.map { it.gesturesSwipeRight is GestureAction.Feed }

    fun setEnabled(enabled: Boolean) {
        launcherDataStore.update {
            it.copy(gesturesSwipeRight = if (enabled) GestureAction.Feed else GestureAction.NoAction)
        }
    }

    val providerPackage
        get() = launcherDataStore.data.map { it.feedProviderPackage }

    fun setProviderPackage(providerPackage: String?) {
        launcherDataStore.update {
            it.copy(feedProviderPackage = providerPackage)
        }
    }

}