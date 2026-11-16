package dev.kbzon.launcher.searchactions.builders

import android.content.Context
import dev.kbzon.launcher.searchactions.R
import dev.kbzon.launcher.searchactions.TextClassificationResult
import dev.kbzon.launcher.searchactions.actions.SearchAction
import dev.kbzon.launcher.searchactions.actions.SearchActionIcon
import dev.kbzon.launcher.searchactions.actions.ShareAction

class ShareActionBuilder(
    override val label: String,
) : SearchActionBuilder {

    constructor(context: Context) : this(context.getString(R.string.search_action_share))

    override val key: String
        get() = "share"

    override val icon: SearchActionIcon = SearchActionIcon.Share

    override fun build(context: Context, classifiedQuery: TextClassificationResult): SearchAction? {
        return ShareAction(
            context.getString(R.string.search_action_share),
            classifiedQuery.text
        )
    }
}