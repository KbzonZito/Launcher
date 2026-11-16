package dev.kbzon.launcher.search

import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.UserHandle
import androidx.core.content.ContextCompat
import dev.kbzon.launcher.base.R
import dev.kbzon.launcher.icons.ColorLayer
import dev.kbzon.launcher.icons.StaticLauncherIcon
import dev.kbzon.launcher.icons.TintedIconLayer
import dev.kbzon.launcher.profiles.Profile

interface Application: SavableSearchable {
    override val preferDetailsOverLaunch: Boolean
        get() = false

    val componentName: ComponentName
    val isSuspended: Boolean

    /**
     * If true, the app's identity should not be revealed to the user.
     */
    val isPrivate: Boolean
        get() = false
    val user: UserHandle
    val versionName: String?

    override fun getPlaceholderIcon(context: Context): StaticLauncherIcon {
        return StaticLauncherIcon(
            foregroundLayer = TintedIconLayer(
                icon = ContextCompat.getDrawable(context, R.drawable.android_24px)!!,
                scale = 0.65f,
                color = 0xff3dda84.toInt(),
            ),
            backgroundLayer = ColorLayer(0xff3dda84.toInt())
        )
    }

    val canUninstall: Boolean
    fun uninstall(context: Context)
    fun openAppDetails(context: Context)

    val canShareApk: Boolean
    suspend fun shareApkFile(context: Context) {}

    fun getStoreDetails(context: Context): StoreLink? = null

    fun getActivityInfo(context: Context): ActivityInfo? {
        return try {
            context.packageManager.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}

data class StoreLink(
    val label: String,
    val url: String
)