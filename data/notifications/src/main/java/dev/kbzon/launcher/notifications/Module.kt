package dev.kbzon.launcher.notifications

import org.koin.dsl.module

val notificationsModule = module {
    single<NotificationRepository> { NotificationRepository() }
}