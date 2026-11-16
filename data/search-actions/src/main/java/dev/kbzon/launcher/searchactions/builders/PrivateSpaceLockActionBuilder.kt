package dev.kbzon.launcher.searchactions.builders

import android.content.Context
import dev.kbzon.launcher.ktx.isAtLeastApiLevel
import dev.kbzon.launcher.profiles.Profile
import dev.kbzon.launcher.profiles.ProfileManager
import dev.kbzon.launcher.search.ResultScore
import dev.kbzon.launcher.searchactions.R
import dev.kbzon.launcher.searchactions.TextClassificationResult
import dev.kbzon.launcher.searchactions.actions.PrivateSpaceLockAction
import dev.kbzon.launcher.searchactions.actions.SearchAction
import dev.kbzon.launcher.searchactions.actions.SearchActionIcon
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PrivateSpaceLockActionBuilder(
    override val label: String,
) : SearchActionBuilder, KoinComponent {

    private val profileManager: ProfileManager by inject()

    constructor(context: Context) : this(context.getString(R.string.search_query_private_space))

    override val key = "private_space"
    override val icon = SearchActionIcon.PrivateSpace

    override fun build(context: Context, classifiedQuery: TextClassificationResult): SearchAction? {
        if (!isAtLeastApiLevel(35)) return null

        val privateProfile = profileManager.getProfile(Profile.Type.Private) ?: return null
        val profileState = profileManager.getCurrentProfileState(privateProfile) ?: return null

        val keyword = context.getString(R.string.search_query_private_space).lowercase()
        if (keyword != classifiedQuery.text.trim().lowercase()) return null

        return PrivateSpaceLockAction(
            label = context.getString(
                if (profileState.locked) R.string.search_action_private_space_unlock
                else R.string.search_action_private_space_lock
            ),
            isLocked = profileState.locked,
            profile = privateProfile,
            profileManager = profileManager,
        )
    }
}
