package dev.kbzon.launcher.ui.settings.contacts

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.search.ContactSearchSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ContactsSettingsScreenVM : ViewModel(), KoinComponent {

    private val settings: ContactSearchSettings by inject()
    private val permissionsManager: PermissionsManager by inject()

    val hasCallPermission = permissionsManager.hasPermission(PermissionGroup.Call)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun requestCallPermission(activity: AppCompatActivity) =
        permissionsManager.requestPermission(activity, PermissionGroup.Call)

    val callOnTap = settings.callOnTap
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setCallOnTap(callOnTap: Boolean) =
        settings.setCallOnTap(callOnTap)

    val hasContactsPermission = permissionsManager.hasPermission(PermissionGroup.Contacts)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    fun requestContactsPermission(activity: AppCompatActivity) {
        permissionsManager.requestPermission(activity, PermissionGroup.Contacts)
    }


    val enabledProviders = settings.enabledProviders

    fun setProviderEnabled(providerId: String, enabled: Boolean) {
        settings.setProviderEnabled(providerId, enabled)
    }
}