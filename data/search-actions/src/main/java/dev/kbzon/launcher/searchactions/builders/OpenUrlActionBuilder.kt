package dev.kbzon.launcher.searchactions.builders

import android.content.Context
import dev.kbzon.launcher.searchactions.R
import dev.kbzon.launcher.searchactions.TextClassificationResult
import dev.kbzon.launcher.searchactions.actions.MessageAction
import dev.kbzon.launcher.searchactions.actions.OpenUrlAction
import dev.kbzon.launcher.searchactions.actions.SearchAction
import dev.kbzon.launcher.searchactions.actions.SearchActionIcon

class OpenUrlActionBuilder(
    override val label: String
) : SearchActionBuilder {

    constructor(context: Context) : this(context.getString(R.string.search_action_open_url))

    override val key: String
        get() = "website"

    override val icon: SearchActionIcon = SearchActionIcon.Website

    override fun build(context: Context, classifiedQuery: TextClassificationResult): SearchAction? {
        if (classifiedQuery.url != null) {
            return OpenUrlAction(
                context.getString(R.string.search_action_open_url), classifiedQuery.url
            )
        }
        return null
    }
}