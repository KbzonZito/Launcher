package dev.kbzon.launcher.search

import android.content.Context
import android.os.Bundle
import dev.kbzon.launcher.base.R
import dev.kbzon.launcher.icons.ColorLayer
import dev.kbzon.launcher.icons.StaticLauncherIcon
import dev.kbzon.launcher.icons.VectorLayer

data class Tag(
    val tag: String,
    override val labelOverride: String? = null
) : SavableSearchable {

    override val domain: String = Domain

    override val key: String = "$domain://$tag"
    override val label: String = tag

    override val preferDetailsOverLaunch: Boolean = true

    override fun launch(context: Context, options: Bundle?): Boolean {
        return false
    }

    override fun overrideLabel(label: String): SavableSearchable {
        return this.copy(labelOverride = label)
    }

    override fun getPlaceholderIcon(context: Context): StaticLauncherIcon {
        return StaticLauncherIcon(
            foregroundLayer = VectorLayer(R.drawable.tag_20px),
            backgroundLayer = ColorLayer()
        )
    }

    override fun getSerializer(): SearchableSerializer {
        return TagSerializer()
    }

    companion object {
        const val Domain = "tag"
    }
}