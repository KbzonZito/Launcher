package dev.kbzon.launcher.searchactions.builders

import android.content.Context
import dev.kbzon.launcher.searchactions.R
import dev.kbzon.launcher.searchactions.TextClassificationResult
import dev.kbzon.launcher.searchactions.actions.EmailAction
import dev.kbzon.launcher.searchactions.actions.SearchAction
import dev.kbzon.launcher.searchactions.actions.SearchActionIcon

class EmailActionBuilder(
    override val label: String
): SearchActionBuilder {

    constructor(context: Context) : this(context.getString(R.string.search_action_email))

    override val key: String
        get() = "email"

    override val icon: SearchActionIcon = SearchActionIcon.Email

    override fun build(context: Context, classifiedQuery: TextClassificationResult): SearchAction? {
        if (classifiedQuery.email != null) {
            return EmailAction(
                context.getString(R.string.search_action_email), classifiedQuery.email
            )
        }
        return null
    }

}