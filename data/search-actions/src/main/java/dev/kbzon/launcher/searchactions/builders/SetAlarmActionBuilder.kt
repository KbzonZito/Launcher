package dev.kbzon.launcher.searchactions.builders

import android.content.Context
import dev.kbzon.launcher.searchactions.R
import dev.kbzon.launcher.searchactions.TextClassificationResult
import dev.kbzon.launcher.searchactions.actions.SearchAction
import dev.kbzon.launcher.searchactions.actions.SearchActionIcon
import dev.kbzon.launcher.searchactions.actions.SetAlarmAction
import java.time.LocalDate

class SetAlarmActionBuilder(
    override val label: String
) : SearchActionBuilder {

    constructor(context: Context) : this(context.getString(R.string.search_action_alarm))

    override val key: String
        get() = "alarm"

    override val icon: SearchActionIcon = SearchActionIcon.Alarm

    override fun build(context: Context, classifiedQuery: TextClassificationResult): SearchAction? {
        if (classifiedQuery.time != null) {
            return SetAlarmAction(
                context.getString(R.string.search_action_alarm), classifiedQuery.time
            )
        }
        return null
    }

}