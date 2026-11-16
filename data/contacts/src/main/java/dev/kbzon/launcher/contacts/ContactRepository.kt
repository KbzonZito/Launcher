package dev.kbzon.launcher.contacts

import android.content.Context
import dev.kbzon.launcher.contacts.providers.AndroidContactProvider
import dev.kbzon.launcher.contacts.providers.PluginContactProvider
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.search.ContactSearchSettings
import dev.kbzon.launcher.search.Contact
import dev.kbzon.launcher.search.SearchableRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ContactRepository(
    private val context: Context,
    private val permissionsManager: PermissionsManager,
    private val settings: ContactSearchSettings,
) : SearchableRepository<Contact> {

    override fun search(query: String, allowNetwork: Boolean): Flow<List<Contact>> {
        val hasPermission = permissionsManager.hasPermission(PermissionGroup.Contacts)

        if (query.length < 2) {
            return flow {
                emit(persistentListOf())
            }
        }

        return hasPermission.combineTransform(settings.enabledProviders) { perm, providerIds ->
            val providers = providerIds.mapNotNull {
                when (it) {
                    "local" -> if (perm) AndroidContactProvider(context) else null
                    else -> PluginContactProvider(context, it)
                }
            }

            supervisorScope {
                val result = MutableStateFlow(listOf<Contact>())

                for (provider in providers) {
                    launch {
                        val r = provider.search(
                            query,
                            allowNetwork = allowNetwork,
                        )
                        result.update { it + r }
                    }
                }
                emitAll(result)
            }
        }
    }
}