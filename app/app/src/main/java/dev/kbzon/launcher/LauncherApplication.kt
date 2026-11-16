package dev.kbzon.launcher

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dev.kbzon.launcher.applications.applicationsModule
import dev.kbzon.launcher.appshortcuts.appShortcutsModule
import dev.kbzon.launcher.backup.backupModule
import dev.kbzon.launcher.badges.badgesModule
import dev.kbzon.launcher.calculator.calculatorModule
import dev.kbzon.launcher.calendar.calendarModule
import dev.kbzon.launcher.contacts.contactsModule
import dev.kbzon.launcher.data.customattrs.customAttrsModule
import dev.kbzon.launcher.data.i18nDataModule
import dev.kbzon.launcher.searchable.searchableModule
import dev.kbzon.launcher.files.filesModule
import dev.kbzon.launcher.icons.iconsModule
import dev.kbzon.launcher.music.musicModule
import dev.kbzon.launcher.search.searchModule
import dev.kbzon.launcher.unitconverter.unitConverterModule
import dev.kbzon.launcher.websites.websitesModule
import dev.kbzon.launcher.widgets.widgetsModule
import dev.kbzon.launcher.fork.forkModule
import dev.kbzon.launcher.database.databaseModule
import dev.kbzon.launcher.debug.initDebugMode
import dev.kbzon.launcher.globalactions.globalActionsModule
import dev.kbzon.launcher.notifications.notificationsModule
import dev.kbzon.launcher.locations.locationsModule
import dev.kbzon.launcher.permissions.permissionsModule
import dev.kbzon.launcher.devicepose.devicePoseModule
import dev.kbzon.launcher.feed.feedModule
import dev.kbzon.launcher.preferences.preferencesModule
import dev.kbzon.launcher.profiles.profilesModule
import dev.kbzon.launcher.searchactions.searchActionsModule
import dev.kbzon.launcher.services.favorites.favoritesModule
import dev.kbzon.launcher.services.tags.servicesTagsModule
import dev.kbzon.launcher.services.widgets.widgetsServiceModule
import dev.kbzon.launcher.themes.themesModule
import dev.kbzon.launcher.weather.weatherModule
import kotlinx.coroutines.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import kotlin.coroutines.CoroutineContext

class LauncherApplication : Application(), CoroutineScope, ImageLoaderFactory {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    override fun onCreate() {
        super.onCreate()

        // [fork] Tras renombrar los paquetes (de.mm20.launcher2 → dev.kbzon.launcher), las tareas de
        // WorkManager guardadas apuntan a clases que ya no existen. Se cancelan una vez; los
        // repositorios (clima, tipo de cambio) las vuelven a programar al iniciarse.
        val migrations = getSharedPreferences("fork_migrations", MODE_PRIVATE)
        if (!migrations.getBoolean("package_rename_v1", false)) {
            androidx.work.WorkManager.getInstance(this).cancelAllWork()
            migrations.edit().putBoolean("package_rename_v1", true).apply()
        }

        if (BuildConfig.BUILD_TYPE == "debug") initDebugMode()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@LauncherApplication)
            modules(
                listOf(
                    applicationsModule,
                    appShortcutsModule,
                    baseModule,
                    calculatorModule,
                    badgesModule,
                    calendarModule,
                    contactsModule,
                    customAttrsModule,
                    databaseModule,
                    favoritesModule,
                    searchableModule,
                    filesModule,
                    globalActionsModule,
                    iconsModule,
                    musicModule,
                    notificationsModule,
                    permissionsModule,
                    preferencesModule,
                    searchModule,
                    searchActionsModule,
                    themesModule,
                    unitConverterModule,
                    weatherModule,
                    websitesModule,
                    widgetsModule,
                    forkModule,
                    locationsModule,
                    servicesTagsModule,
                    widgetsServiceModule,
                    backupModule,
                    devicePoseModule,
                    profilesModule,
                    i18nDataModule,
                    feedModule,
                )
            )
        }

        // [fork] Fase 11: gestos propuestos — ↓ abre la búsqueda (el "bajar dividido" pasa a ser opcional).
        if (!migrations.getBoolean("gestures_v2", false)) {
            org.koin.java.KoinJavaComponent.get<dev.kbzon.launcher.preferences.home.MinimalHomeSettings>(
                dev.kbzon.launcher.preferences.home.MinimalHomeSettings::class.java
            ).setSplitSwipeDown(false)
            migrations.edit().putBoolean("gestures_v2", true).apply()
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(applicationContext)
            .components {
                add(SvgDecoder.Factory())
                add(dev.kbzon.launcher.fork.OfflineImageInterceptor(org.koin.java.KoinJavaComponent.get(dev.kbzon.launcher.preferences.privacy.PrivacySettings::class.java))) // [fork]
            }
            .crossfade(true)
            .crossfade(200)
            .build()
    }
}