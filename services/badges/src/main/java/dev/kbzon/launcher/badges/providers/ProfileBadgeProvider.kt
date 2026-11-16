package dev.kbzon.launcher.badges.providers

import dev.kbzon.launcher.badges.Badge
import dev.kbzon.launcher.badges.BadgeIcon
import dev.kbzon.launcher.badges.R
import dev.kbzon.launcher.profiles.Profile
import dev.kbzon.launcher.profiles.ProfileManager
import dev.kbzon.launcher.search.AppShortcut
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.Searchable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ProfileBadgeProvider : BadgeProvider, KoinComponent {
    private val profileManager: ProfileManager by inject()

    override fun getBadge(searchable: Searchable): Flow<Badge?> = flow {
        val userHandle = when (searchable) {
            is Application -> searchable.user
            is AppShortcut -> searchable.user
            else -> null
        }
        if (userHandle != null) {
            emitAll(
                profileManager.getProfileByUserHandle(userHandle).map {
                    when (it?.type) {
                        Profile.Type.Work -> WorkProfile
                        Profile.Type.Private -> PrivateProfile
                        else -> null
                    }
                }
            )
        } else {
            emit(null)
        }
    }

    companion object {
        private val WorkProfile = Badge(
            icon = BadgeIcon(R.drawable.enterprise_20px)
        )

        private val PrivateProfile = Badge(
            icon = BadgeIcon(R.drawable.encrypted_20px)
        )
    }
}