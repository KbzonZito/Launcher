package dev.kbzon.launcher.calendar.providers

import dev.kbzon.launcher.search.calendar.CalendarListType

data class CalendarList(
    val id: String,
    val name: String,
    val owner: String?,
    val color: Int,
    val types: List<CalendarListType>,
    val providerId: String,
)