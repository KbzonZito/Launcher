package dev.kbzon.launcher.search

import android.content.ComponentName
import android.content.Context
import android.os.Process
import android.os.UserHandle
import androidx.core.content.ContextCompat
import dev.kbzon.launcher.base.R
import dev.kbzon.launcher.icons.ColorLayer
import dev.kbzon.launcher.icons.StaticLauncherIcon
import dev.kbzon.launcher.icons.TintedIconLayer
import dev.kbzon.launcher.profiles.Profile

interface AppShortcut : SavableSearchable {

    val appName: String?
    val componentName: ComponentName?
    val packageName: String?
    val user: UserHandle
        get() = Process.myUserHandle()

    override val preferDetailsOverLaunch: Boolean
        get() = false

    override fun getPlaceholderIcon(context: Context): StaticLauncherIcon {
        return StaticLauncherIcon(
            foregroundLayer = TintedIconLayer(
                color = 0xFF3DDA84.toInt(),
                icon = ContextCompat.getDrawable(context, R.drawable.android_24px)!!,
                scale = 0.65f,
            ),
            backgroundLayer = ColorLayer(0xFF3DDA84.toInt()),
        )
    }

    val canDelete: Boolean
        get() = false

    suspend fun delete(context: Context) {}

    val isUnavailable: Boolean
        get() = false
}