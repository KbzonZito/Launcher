package dev.kbzon.launcher.searchactions.actions

import android.content.Context
import dev.kbzon.launcher.ktx.isAtLeastApiLevel
import dev.kbzon.launcher.profiles.Profile
import dev.kbzon.launcher.profiles.ProfileManager

data class PrivateSpaceLockAction(
    override val label: String,
    val isLocked: Boolean,
    val profile: Profile,
    val profileManager: ProfileManager,
) : SearchAction {
    override val icon = SearchActionIcon.PrivateSpace
    override val iconColor = 0
    override val customIcon = null

    override fun start(context: Context) {
        if (isAtLeastApiLevel(28)) {
            if (isLocked) {
                profileManager.unlockProfile(profile)
            } else {
                profileManager.lockProfile(profile)
            }
        }
    }
}
