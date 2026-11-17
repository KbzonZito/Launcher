package dev.kbzon.launcher.files

import android.content.Context
import dev.kbzon.launcher.files.providers.LocalFileProvider
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.search.FileSearchSettings
import dev.kbzon.launcher.search.File
import dev.kbzon.launcher.search.Location
import dev.kbzon.launcher.search.SearchableRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class FileRepository(
    private val context: Context,
    private val permissionsManager: PermissionsManager,
    private val settings: FileSearchSettings,
) : SearchableRepository<File> {

    override fun search(
        query: String,
        allowNetwork: Boolean,
    ): Flow<ImmutableList<File>> {
        if (query.isBlank()) {
            return flowOf(persistentListOf())
        }

        val hasPermission = permissionsManager.hasPermission(PermissionGroup.ExternalStorage)


        return combineTransform(settings.enabledProviders, hasPermission) { providerIds, permission ->
            emit(persistentListOf())
            if (providerIds.isEmpty()) {
                return@combineTransform
            }
            val providers = providerIds.mapNotNull {
                when (it) {
                    "local" -> if (permission) LocalFileProvider(
                        context,
                        permissionsManager
                    ) else null

                    // [fork] Nextcloud/ownCloud eliminados (privacidad); los favoritos guardados siguen deserializándose.
                    "nextcloud", "owncloud" -> null
                    else -> null // [fork] plugins eliminados
                }
            }

            supervisorScope {
                val result = MutableStateFlow(persistentListOf<File>())

                for (provider in providers) {
                    launch {
                        val r = provider.search(
                            query,
                            allowNetwork,
                        )
                        result.update {
                            (it + r).toPersistentList()
                        }
                    }
                }
                emitAll(result)
            }

        }
    }
}