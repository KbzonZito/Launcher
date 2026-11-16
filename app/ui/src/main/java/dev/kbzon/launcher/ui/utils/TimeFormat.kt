package dev.kbzon.launcher.ui.utils

import android.content.Context
import android.text.format.DateFormat
import dev.kbzon.launcher.preferences.TimeFormat
import dev.kbzon.launcher.preferences.TimeFormat.TwentyFourHour

fun TimeFormat.isTwentyFourHours(context: Context): Boolean {
    return this == TimeFormat.TwentyFourHour || this == TimeFormat.System && DateFormat.is24HourFormat(context)
}