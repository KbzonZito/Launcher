package dev.kbzon.launcher.searchactions.builders

import android.content.Context
import dev.kbzon.launcher.searchactions.R
import dev.kbzon.launcher.searchactions.TextClassificationResult
import dev.kbzon.launcher.searchactions.actions.SearchAction
import dev.kbzon.launcher.searchactions.actions.SearchActionIcon
import dev.kbzon.launcher.searchactions.actions.WebsearchAction

class WebsearchActionBuilder(
    override val label: String,
) : SearchActionBuilder {

    constructor(context: Context) : this(context.getString(R.string.search_action_websearch))

    override val icon: SearchActionIcon = SearchActionIcon.WebSearch
    override val key: String
        get() = "websearch"

    override fun build(context: Context, classifiedQuery: TextClassificationResult): SearchAction {
        return WebsearchAction(
            context.getString(R.string.search_action_websearch), classifiedQuery.text
        )
    }
}