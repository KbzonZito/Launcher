package dev.kbzon.launcher.contacts

import android.content.Context
import androidx.core.net.toUri
import dev.kbzon.launcher.contacts.providers.AndroidContact
import dev.kbzon.launcher.contacts.providers.AndroidContactProvider
import dev.kbzon.launcher.ktx.jsonObjectOf
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
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

