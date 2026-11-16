package dev.kbzon.launcher.search

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dev.kbzon.launcher.icons.ColorLayer
import dev.kbzon.launcher.icons.StaticLauncherIcon
import dev.kbzon.launcher.icons.TextLayer
import dev.kbzon.launcher.ktx.tryStartActivity
import java.net.URLEncoder
import java.text.SimpleDateFormat

interface CalendarEvent : SavableSearchable {
    val color: Int?
    val startTime: Long?
    val endTime: Long
    val allDay: Boolean
    val description: String?
    val calendarName: String?
    val location: String?
    val attendees: List<String>
    val isCompleted: Boolean?
        get() = null

    val isTask: Boolean
        get() = isCompleted != null


    override val preferDetailsOverLaunch: Boolean
        get() = true


    override fun getPlaceholderIcon(context: Context): StaticLauncherIcon {
        val df = SimpleDateFormat("dd")
        return StaticLauncherIcon(
            foregroundLayer = TextLayer(
                text = df.format(startTime ?: endTime),
                color = color ?: 0,
            ),
            backgroundLayer = ColorLayer(color ?: 0)
        )
    }

    fun openLocation(context: Context) {
        if (location == null) return
        context.tryStartActivity(
            Intent(Intent.ACTION_VIEW)
                .setData(
                    "geo:0,0?q=${
                        URLEncoder.encode(
                            location,
                            "utf8"
                        )
                    }".toUri()
                )
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}