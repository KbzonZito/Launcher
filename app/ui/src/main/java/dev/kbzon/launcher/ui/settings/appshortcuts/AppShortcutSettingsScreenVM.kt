package dev.kbzon.launcher.ui.settings.appshortcuts

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.applications.AppRepository
import dev.kbzon.launcher.badges.Badge
import dev.kbzon.launcher.badges.BadgeService
import dev.kbzon.launcher.icons.IconService
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.search.ShortcutSearchSettings
import dev.kbzon.launcher.search.SavableSearchable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppShortcutSettingsScreenVM : ViewModel(), KoinComponent {
    private val appRepository by inject<AppRepository>()
    private val settings by inject<ShortcutSearchSettings>()
    private val permissionsManager by inject<PermissionsManager>()
    private val iconService by inject<IconService>()
    private val badgeService by inject<BadgeService>()

    val blocklist =
        settings.blocklist.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptySet())

    val apps = appRepository.findMany().map { it.sorted() }.flowOn(Dispatchers.Default)

    val isShortcutSearchEnabled = settings.enabled

    val hasAppShortcutPermission = permissionsManager.hasPermission(PermissionGroup.AppShortcuts)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setShortcutSearchEnabled(enabled: Boolean) {
        settings.setEnabled(enabled)
    }

    fun setAppBlocklisted(appId: String, blocked: Boolean) {
        val blocklist = blocklist.value.toMutableSet()
        if (blocked) {
            blocklist.add(appId)
        } else {
            blocklist.remove(appId)
        }
        settings.setBlocklist(blocklist)
    }

    fun requestAppShortcutsPermission(activity: AppCompatActivity) {
        permissionsManager.requestPermission(activity, PermissionGroup.AppShortcuts)
    }

    fun getIcon(app: SavableSearchable, size: Int): Flow<LauncherIcon?> {
        return iconService.getIcon(app, size)
    }

    fun getBadge(app: SavableSearchable): Flow<Badge?> {
        return badgeService.getBadge(app)
    }
}