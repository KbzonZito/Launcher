package dev.kbzon.launcher.contacts.providers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import coil.imageLoader
import coil.request.ImageRequest
import dev.kbzon.launcher.contacts.PluginContactSerializer
import dev.kbzon.launcher.icons.ColorLayer
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.icons.StaticIconLayer
import dev.kbzon.launcher.icons.StaticLauncherIcon
import dev.kbzon.launcher.ktx.tryStartActivity
import dev.kbzon.launcher.plugin.config.StorageStrategy
import dev.kbzon.launcher.search.Contact
import dev.kbzon.launcher.search.File
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.SearchableSerializer
import dev.kbzon.launcher.search.UpdatableSearchable
import dev.kbzon.launcher.search.UpdateResult
import dev.kbzon.launcher.search.contact.CustomContactAction
import dev.kbzon.launcher.search.contact.EmailAddress
import dev.kbzon.launcher.search.contact.PhoneNumber
import dev.kbzon.launcher.search.contact.PostalAddress

internal data class PluginContact(
    val id: String,
    val uri: Uri,
    override val name: String,
    override val phoneNumbers: List<PhoneNumber>,
    override val emailAddresses: List<EmailAddress>,
    override val postalAddresses: List<PostalAddress>,
    override val customActions: List<CustomContactAction>,
    val photoUri: Uri?,
    override val labelOverride: String? = null,
    val authority: String,
    val storageStrategy: StorageStrategy,
    override val timestamp: Long,
    override val updatedSelf: (suspend (SavableSearchable) -> UpdateResult<Contact>)?
) : Contact, UpdatableSearchable<Contact> {
    override val domain: String = Domain
    override fun getSerializer(): SearchableSerializer {
        return PluginContactSerializer()
    }

    override val key: String = "$domain://$authority:$id"
    override fun overrideLabel(label: String): SavableSearchable {
        return copy(labelOverride = label)
    }

    override fun launch(
        context: Context,
        options: Bundle?
    ): Boolean {
        return context.tryStartActivity(
            Intent(
                Intent.ACTION_VIEW
            ).apply {
                data = uri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }, options
        )
    }

    override suspend fun loadIcon(context: Context, size: Int, themed: Boolean): LauncherIcon? {
        if (photoUri != null) {
            val request = ImageRequest.Builder(context)
                .data(photoUri)
                .size(size)
                .build()
            val result = context.imageLoader.execute(request)
            val drawable = result.drawable ?: return null
            return StaticLauncherIcon(
                foregroundLayer = StaticIconLayer(icon = drawable),
                backgroundLayer = ColorLayer(),
            )
        }
        return super.loadIcon(context, size, themed)
    }

    companion object {
        const val Domain = "plugin.contact"
    }
}