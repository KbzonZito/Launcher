package dev.kbzon.launcher.searchactions.builders

import android.content.Context
import dev.kbzon.launcher.searchactions.R
import dev.kbzon.launcher.searchactions.actions.SearchAction
import dev.kbzon.launcher.searchactions.TextClassificationResult
import dev.kbzon.launcher.searchactions.actions.CallAction
import dev.kbzon.launcher.searchactions.actions.SearchActionIcon

class CallActionBuilder(
    override val label: String
): SearchActionBuilder {

    constructor(context: Context): this(context.getString(R.string.search_action_call))

    override val key: String
        get() = "call"

    override val icon: SearchActionIcon = SearchActionIcon.Phone

    override fun build(context: Context, classifiedQuery: TextClassificationResult): SearchAction? {
        if (classifiedQuery.phoneNumber != null) {
            return CallAction(
                context.getString(R.string.search_action_call), classifiedQuery.phoneNumber
            )
        }
        return null
    }

}