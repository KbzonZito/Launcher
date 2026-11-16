package dev.kbzon.launcher.search

import android.content.Context
import dev.kbzon.launcher.base.R
import dev.kbzon.launcher.icons.ColorLayer
import dev.kbzon.launcher.icons.StaticLauncherIcon
import dev.kbzon.launcher.icons.TextLayer
import dev.kbzon.launcher.icons.VectorLayer
import dev.kbzon.launcher.search.contact.CustomContactAction
import dev.kbzon.launcher.search.contact.EmailAddress
import dev.kbzon.launcher.search.contact.PhoneNumber
import dev.kbzon.launcher.search.contact.PostalAddress

interface Contact : SavableSearchable {
    val name: String
    val phoneNumbers: List<PhoneNumber>
    val emailAddresses: List<EmailAddress>
    val postalAddresses: List<PostalAddress>
    val customActions: List<CustomContactAction>

    override val label: String
        get() = name

    val summary: String
        get() {
            return (phoneNumbers.map { it.number } + emailAddresses.map { it.address })
                .joinToString(", ")
        }

    override fun getPlaceholderIcon(context: Context): StaticLauncherIcon {
        val letter = (labelOverride ?: label).firstOrNull()?.toString()

        if (letter != null) {
            return StaticLauncherIcon(
                foregroundLayer = TextLayer(text = letter, color = 0xFF2364AA.toInt()),
                backgroundLayer = ColorLayer(0xFF2364AA.toInt())
            )
        }

        return StaticLauncherIcon(
            foregroundLayer = VectorLayer(R.drawable.person_24px),
            backgroundLayer = ColorLayer(0xFF2364AA.toInt())
        )
    }

    override val preferDetailsOverLaunch: Boolean
        get() = true
}
