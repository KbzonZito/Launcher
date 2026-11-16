package dev.kbzon.launcher.search.location

import android.net.Uri
import dev.kbzon.launcher.serialization.UriSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Attribution(
    val text: String? = null,
    @Serializable(with = UriSerializer::class)
    val iconUrl: Uri? = null,
    val url: String? = null,
)