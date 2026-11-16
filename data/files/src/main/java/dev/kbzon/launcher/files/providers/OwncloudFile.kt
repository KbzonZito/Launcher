package dev.kbzon.launcher.files.providers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import dev.kbzon.launcher.files.OwncloudFileSerializer
import dev.kbzon.launcher.files.R
import dev.kbzon.launcher.ktx.tryStartActivity
import dev.kbzon.launcher.search.File
import dev.kbzon.launcher.search.SearchableSerializer
import dev.kbzon.launcher.search.file.FileMetadata

internal data class OwncloudFile(
    val fileId: Long,
    override val label: String,
    override val path: String,
    override val mimeType: String,
    override val size: Long,
    override val isDirectory: Boolean,
    val server: String,
    override val owner: String?,
    override val labelOverride: String? = null,
) : File {

    override fun overrideLabel(label: String): OwncloudFile {
        return this.copy(labelOverride = label)
    }

    override val domain: String = Domain

    override val key: String = "$domain://$server/$fileId"

    override val providerIconRes = R.drawable.ic_badge_owncloud

    override val metaData: FileMetadata = FileMetadata()

    private fun getLaunchIntent(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("$server/f/$fileId")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    override fun launch(context: Context, options: Bundle?): Boolean {
        return context.tryStartActivity(getLaunchIntent(), options)
    }

    override fun getSerializer(): SearchableSerializer {
        return OwncloudFileSerializer()
    }

    companion object {
        const val Domain = "owncloud"
    }
}