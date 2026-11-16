package dev.kbzon.launcher.ui.privacy

import android.view.WindowManager
import androidx.compose.runtime.snapshotFlow
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.kbzon.launcher.preferences.privacy.PrivacySettings
import dev.kbzon.launcher.ui.launcher.search.SearchVM
import dev.kbzon.launcher.ui.launcher.sheets.LauncherBottomSheetManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * [fork] Privacidad local del launcher:
 * - atiende las peticiones de [HiddenItemsLock] con autenticación biométrica,
 * - aplica FLAG_SECURE mientras hay una búsqueda o apps ocultas en pantalla,
 * - vuelve a bloquear y esconde las apps ocultas cuando el launcher deja de verse.
 */
class LauncherPrivacyGuard(
    private val activity: FragmentActivity,
    private val searchVM: SearchVM,
    private val sheetManager: LauncherBottomSheetManager,
) : KoinComponent {
    private val privacySettings: PrivacySettings by inject()

    fun attach() {
        activity.lifecycleScope.launch {
            HiddenItemsLock.requests.collect { onUnlocked ->
                val authorized = !privacySettings.protectHiddenItems.first() || activity.authenticateUser()
                if (authorized) {
                    HiddenItemsLock.markUnlocked()
                    onUnlocked()
                }
            }
        }

        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val sensitiveOnScreen = snapshotFlow {
                    searchVM.searchQuery.value.isNotEmpty() ||
                            searchVM.filters.value.hiddenItems ||
                            sheetManager.hiddenItemsSheetShown.value ||
                            HiddenItemsLock.hiddenContentVisible
                }
                combine(sensitiveOnScreen, privacySettings.secureScreens) { sensitive, enabled ->
                    sensitive && enabled
                }.distinctUntilChanged().collect { secure ->
                    if (secure) activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    else activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                HiddenItemsLock.lock()
                sheetManager.dismissHiddenItemsSheet()
                if (searchVM.filters.value.hiddenItems) {
                    searchVM.setFilters(searchVM.filters.value.copy(hiddenItems = false))
                }
            }
        })
    }
}
