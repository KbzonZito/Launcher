package dev.kbzon.launcher.contacts

import android.content.Context
import androidx.core.net.toUri
import dev.kbzon.launcher.contacts.providers.AndroidContact
import dev.kbzon.launcher.contacts.providers.AndroidContactProvider
import dev.kbzon.launcher.contacts.providers.PluginContact
import dev.kbzon.launcher.contacts.providers.PluginContactProvider
import dev.kbzon.launcher.ktx.jsonObjectOf
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.plugin.PluginRepository
import dev.kbzon.launcher.plugin.config.StorageStrategy
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.SearchableSerializer
import dev.kbzon.launcher.search.UpdateResult
import dev.kbzon.launcher.search.asUpdateResult
import dev.kbzon.launcher.search.contact.CustomContactAction
import dev.kbzon.launcher.search.contact.EmailAddress
import dev.kbzon.launcher.search.contact.PhoneNumber
import dev.kbzon.launcher.search.contact.PostalAddress
import dev.kbzon.launcher.serialization.Json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
internal data class SerializedPluginContact(
    val id: String? = null,
    val uri: String? = null,
    val name: String? = null,
    val phoneNumbers: List<PhoneNumber>? = null,
    val emailAddresses: List<EmailAddress>? = null,
    val postalAddresses: List<PostalAddress>? = null,
    val customActions: List<CustomContactAction>? = null,
    val photoUri: String? = null,
    val authority: String? = null,
    val strategy: StorageStrategy = StorageStrategy.StoreCopy,
    val timestamp: Long = 0L,
)

internal class AndroidContactSerializer : SearchableSerializer {
    override fun serialize(searchable: SavableSearchable): String {
        searchable as AndroidContact
        return jsonObjectOf(
            "id" to searchable.id
        ).toString()
    }

    override val typePrefix: String
        get() = AndroidContact.Domain
}


internal class PluginContactSerializer : SearchableSerializer {
    override fun serialize(searchable: SavableSearchable): String {
        searchable as PluginContact
        if (searchable.storageStrategy == StorageStrategy.StoreReference) {
            return Json.Lenient.encodeToString(
                SerializedPluginContact(
                    id = searchable.id,
                    authority = searchable.authority,
                    strategy = searchable.storageStrategy,
                )
            )
        } else {
            return Json.Lenient.encodeToString(
                SerializedPluginContact(
                    id = searchable.id,
                    uri = searchable.uri.toString(),
                    name = searchable.name,
                    phoneNumbers = searchable.phoneNumbers,
                    emailAddresses = searchable.emailAddresses,
                    postalAddresses = searchable.postalAddresses,
                    customActions = searchable.customActions,
                    photoUri = searchable.photoUri?.toString(),
                    authority = searchable.authority,
                    strategy = searchable.storageStrategy,
                    timestamp = searchable.timestamp,
                )
            )
        }
    }

    override val typePrefix: String
        get() = PluginContact.Domain
}

internal class AndroidContactDeserializer(
    private val context: Context,
    private val permissionsManager: PermissionsManager
) : SearchableDeserializer {

    override suspend fun deserialize(serialized: String): SavableSearchable? {
        if (!permissionsManager.checkPermissionOnce(PermissionGroup.Contacts)) return null
        val id = JSONObject(serialized).getLong("id")

        val androidContactProvider = AndroidContactProvider(context)

        return androidContactProvider.get(id)
    }
}


internal class PluginContactDeserializer(
    private val context: Context,
    private val pluginRepository: PluginRepository,
): SearchableDeserializer {
    override suspend fun deserialize(serialized: String): SavableSearchable? {
        val json = Json.Lenient.decodeFromString<SerializedPluginContact>(serialized)
        val authority = json.authority ?: return null
        val id = json.id ?: return null
        val strategy = json.strategy
        val plugin = pluginRepository.get(authority).firstOrNull() ?: return null
        if (!plugin.enabled) return null

        return when(strategy) {
            StorageStrategy.StoreReference -> {
                PluginContactProvider(context, authority).get(id).getOrNull()
            }

            else -> {
                val timestamp = json.timestamp

                PluginContact(
                    id = id,
                    name = json.name ?: return null,
                    uri = json.uri?.toUri() ?: return null,
                    phoneNumbers = json.phoneNumbers ?: emptyList(),
                    emailAddresses = json.emailAddresses ?: emptyList(),
                    postalAddresses = json.postalAddresses ?: emptyList(),
                    customActions = json.customActions ?: emptyList(),
                    photoUri = json.photoUri?.toUri(),
                    authority = authority,
                    storageStrategy = strategy,
                    timestamp = timestamp,
                    updatedSelf = {
                        if (it !is PluginContact) UpdateResult.TemporarilyUnavailable()
                        else PluginContactProvider(context, authority).refresh(it, timestamp).asUpdateResult()
                    }
                )
            }
        }
    }
}