package dev.kbzon.launcher.calendar

import dev.kbzon.launcher.calendar.providers.AndroidCalendarEvent
import dev.kbzon.launcher.calendar.providers.PluginCalendarEvent
import dev.kbzon.launcher.calendar.providers.TasksCalendarEvent
import dev.kbzon.launcher.search.CalendarEvent
import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.SearchableRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val calendarModule = module {
    factory<SearchableRepository<CalendarEvent>>(named<CalendarEvent>()) { get<CalendarRepository>() }
    factory<CalendarRepository> { CalendarRepositoryImpl(androidContext(), get(), get(), get()) }
    factory<SearchableDeserializer>(named(AndroidCalendarEvent.Domain)) { AndroidCalendarEventDeserializer(androidContext()) }
    factory<SearchableDeserializer>(named(TasksCalendarEvent.Domain)) { TasksCalendarEventDeserializer(androidContext()) }
    factory<SearchableDeserializer>(named(PluginCalendarEvent.Domain)) { PluginCalendarEventDeserializer(androidContext(), get()) }
}