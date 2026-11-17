package dev.kbzon.launcher.files

import dev.kbzon.launcher.plugin.config.StorageStrategy
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import dev.kbzon.launcher.files.providers.LocalFile
import dev.kbzon.launcher.files.providers.NextcloudFile
import dev.kbzon.launcher.files.providers.OwncloudFile
import dev.kbzon.launcher.ktx.jsonObjectOf
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.SearchableSerializer
import dev.kbzon.launcher.search.UpdateResult
import dev.kbzon.launcher.search.asUpdateResult
import dev.kbzon.launcher.search.file.FileMetadata
import dev.kbzon.launcher.serialization.Json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import androidx.core.net.toUri

@Serializable
internal data class SerializedFile(
    val id: String? = null,
    val authority: String? = null,
    val strategy: StorageStrategy? = null,
    val path: String? = null,
    val mimeType: String? = null,
    val size: Long? = null,
    val label: String? = null,
    val uri: String? = null,
    val thumbnailUri: String? = null,
    val isDirectory: Boolean? = null,
    val owner: String? = null,
    @SerialName("metadata2")
    val metadata: FileMetadata? = null,
    val timestamp: Long? = null,
)

internal class LocalFileSerializer : SearchableSerializer {
    override fun serialize(searchable: SavableSearchable): String {
        searchable as LocalFile
        return jsonObjectOf(
            "id" to searchable.id
        ).toString()
    }

    override val typePrefix: String
        get() = "file"
}

internal class LocalFileDeserializer(
    val context: Context
) : SearchableDeserializer, KoinComponent {
    override suspend fun deserialize(serialized: String): SavableSearchable? {
        val permissionsManager: PermissionsManager = get()
        if (!permissionsManager.checkPermissionOnce(
                PermissionGroup.ExternalStorage
            )
        ) return null
        val json = JSONObject(serialized)
        val uri = MediaStore.Files.getContentUri("external")
        val proj = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        val sel = "${MediaStore.Files.FileColumns._ID} = ?"
        val selArgs = arrayOf(json.getLong("id").toString())
        val cursor = context.contentResolver.query(uri, proj, sel, selArgs, null) ?: return null
        cursor.use {
            if (cursor.moveToNext()) {
                val path = cursor.getStringOrNull(2) ?: return null
                if (!java.io.File(path).exists()) return null
                val directory = java.io.File(path).isDirectory
                val id = cursor.getLong(0)
                val mimeType = cursor.getStringOrNull(3).takeIf { it != "application/octet-stream" }
                    ?: if (directory) "resource/folder" else LocalFile.getMimetypeByFileExtension(
                        path.substringAfterLast(
                            '.'
                        )
                    )
                val size = cursor.getLong(1)
                return LocalFile(
                    path = path,
                    mimeType = mimeType,
                    size = size,
                    isDirectory = directory,
                    id = id,
                    metaData = LocalFile.getMetaData(context, mimeType, path)
                )
            }
        }
        return null
    }
}

internal class NextcloudFileSerializer : SearchableSerializer {
    override fun serialize(searchable: SavableSearchable): String {
        searchable as NextcloudFile
        return jsonObjectOf(
            "id" to searchable.fileId,
            "label" to searchable.label,
            "path" to searchable.path,
            "mimeType" to searchable.mimeType,
            "size" to searchable.size,
            "isDirectory" to searchable.isDirectory,
            "server" to searchable.server,
            "owner" to searchable.owner,
        ).toString()
    }

    override val typePrefix: String
        get() = "nextcloud"
}

internal class NextcloudFileDeserializer : SearchableDeserializer {
    override suspend fun deserialize(serialized: String): SavableSearchable {
        val json = JSONObject(serialized)
        val id = json.getLong("id")
        val label = json.getString("label")
        val path = json.getString("path")
        val mimeType = json.getString("mimeType")
        val size = json.getLong("size")
        val isDirectory = json.getBoolean("isDirectory")
        val server = json.getString("server")
        val owner = json.optString("owner").takeIf { it.isNotEmpty() }

        return NextcloudFile(
            fileId = id,
            label = label,
            path = path,
            mimeType = mimeType,
            size = size,
            isDirectory = isDirectory,
            server = server,
            owner = owner,

        )
    }
}

internal class OwncloudFileSerializer : SearchableSerializer {
    override fun serialize(searchable: SavableSearchable): String {
        searchable as OwncloudFile
        return jsonObjectOf(
            "id" to searchable.fileId,
            "label" to searchable.label,
            "path" to searchable.path,
            "mimeType" to searchable.mimeType,
            "size" to searchable.size,
            "isDirectory" to searchable.isDirectory,
            "server" to searchable.server,
            "owner" to searchable.owner,
        ).toString()
    }

    override val typePrefix: String
        get() = "owncloud"
}

internal class OwncloudFileDeserializer : SearchableDeserializer {
    override suspend fun deserialize(serialized: String): SavableSearchable {
        val json = JSONObject(serialized)
        val id = json.getLong("id")
        val label = json.getString("label")
        val path = json.getString("path")
        val mimeType = json.getString("mimeType")
        val size = json.getLong("size")
        val isDirectory = json.getBoolean("isDirectory")
        val server = json.getString("server")
        val owner = json.optString("owner").takeIf { it.isNotEmpty() }

        return OwncloudFile(
            fileId = id,
            label = label,
            path = path,
            mimeType = mimeType,
            size = size,
            isDirectory = isDirectory,
            server = server,
            owner = owner,
        )
    }
}

