@file:Suppress("ClassName")

package dev.kbzon.launcher.database

import android.content.Context
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import dev.kbzon.launcher.database.daos.PluginDao
import dev.kbzon.launcher.database.daos.ThemeDao
import dev.kbzon.launcher.database.entities.ColorsEntity
import dev.kbzon.launcher.database.entities.CurrencyEntity
import dev.kbzon.launcher.database.entities.CustomAttributeEntity
import dev.kbzon.launcher.database.entities.ForecastEntity
import dev.kbzon.launcher.database.entities.IconEntity
import dev.kbzon.launcher.database.entities.IconPackEntity
import dev.kbzon.launcher.database.entities.PluginEntity
import dev.kbzon.launcher.database.entities.SavedSearchableEntity
import dev.kbzon.launcher.database.entities.SearchActionEntity
import dev.kbzon.launcher.database.entities.ShapesEntity
import dev.kbzon.launcher.database.entities.TransparenciesEntity
import dev.kbzon.launcher.database.entities.TypographyEntity
import dev.kbzon.launcher.database.entities.WidgetEntity
import dev.kbzon.launcher.database.migrations.Migration_10_11
import dev.kbzon.launcher.database.migrations.Migration_11_12
import dev.kbzon.launcher.database.migrations.Migration_12_13
import dev.kbzon.launcher.database.migrations.Migration_13_14
import dev.kbzon.launcher.database.migrations.Migration_14_15
import dev.kbzon.launcher.database.migrations.Migration_15_16
import dev.kbzon.launcher.database.migrations.Migration_16_17
import dev.kbzon.launcher.database.migrations.Migration_17_18
import dev.kbzon.launcher.database.migrations.Migration_18_19
import dev.kbzon.launcher.database.migrations.Migration_19_20
import dev.kbzon.launcher.database.migrations.Migration_20_21
import dev.kbzon.launcher.database.migrations.Migration_21_22
import dev.kbzon.launcher.database.migrations.Migration_22_23
import dev.kbzon.launcher.database.migrations.Migration_23_24
import dev.kbzon.launcher.database.migrations.Migration_24_25
import dev.kbzon.launcher.database.migrations.Migration_25_26
import dev.kbzon.launcher.database.migrations.Migration_26_27
import dev.kbzon.launcher.database.migrations.Migration_27_28
import dev.kbzon.launcher.database.migrations.Migration_28_29
import dev.kbzon.launcher.database.migrations.Migration_29_30
import dev.kbzon.launcher.database.migrations.Migration_30_31
import dev.kbzon.launcher.database.migrations.Migration_31_32
import dev.kbzon.launcher.database.migrations.Migration_32_33
import dev.kbzon.launcher.database.migrations.Migration_33_34
import dev.kbzon.launcher.database.migrations.Migration_6_7
import dev.kbzon.launcher.database.migrations.Migration_7_8
import dev.kbzon.launcher.database.migrations.Migration_8_9
import dev.kbzon.launcher.database.migrations.Migration_9_10
import dev.kbzon.launcher.ktx.toBytes
import dev.kbzon.launcher.preferences.WidgetScreenTarget
import java.util.UUID

@Database(
    entities = [
        ForecastEntity::class,
        SavedSearchableEntity::class,
        CurrencyEntity::class,
        IconEntity::class,
        IconPackEntity::class,
        WidgetEntity::class,
        CustomAttributeEntity::class,
        SearchActionEntity::class,
        ColorsEntity::class,
        PluginEntity::class,
        ShapesEntity::class,
        TransparenciesEntity::class,
        TypographyEntity::class,
    ], version = 34, exportSchema = true
)
@ColumnTypeConverters(ComponentNameConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao
    abstract fun iconDao(): IconDao

    abstract fun searchableDao(): SearchableDao
    abstract fun widgetDao(): WidgetDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun backupDao(): BackupRestoreDao
    abstract fun customAttrsDao(): CustomAttrsDao

    abstract fun searchActionDao(): SearchActionDao

    abstract fun themeDao(): ThemeDao

    abstract fun pluginDao(): PluginDao

    companion object {
        private var _instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            val instance = _instance
                ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "room")
                    //.fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override suspend fun onCreate(connection: SQLiteConnection) {
                            super.onCreate(connection)
                            connection.execSQL(
                                "INSERT INTO `SearchAction` (`position`, `type`) VALUES" +
                                        "(0, 'call')," +
                                        "(1, 'message')," +
                                        "(2, 'email')," +
                                        "(3, 'contact')," +
                                        "(4, 'alarm')," +
                                        "(5, 'timer')," +
                                        "(6, 'calendar')," +
                                        "(7, 'website')," +
                                        "(8, 'websearch')"
                            )

                            connection.prepare(
                                "INSERT INTO `SearchAction` (`position`, `type`, `data`, `label`, `color`, `icon`, `customIcon`, `options`) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?), (?, ?, ?, ?, ?, ?, ?, ?)"
                            ).use {
                                it.bindInt(1, 9)
                                it.bindText(2, "url")
                                it.bindText(3, context.getString(R.string.default_websearch_2_url))
                                it.bindText(4, context.getString(R.string.default_websearch_2_name))
                                it.bindInt(5, 0)
                                it.bindInt(6, 0)
                                it.bindNull(7)
                                it.bindNull(8)
                                it.bindInt(9, 10)
                                it.bindText(10, "url")
                                it.bindText(11, context.getString(R.string.default_websearch_3_url))
                                it.bindText(
                                    12,
                                    context.getString(R.string.default_websearch_3_name)
                                )
                                it.bindInt(13, 0)
                                it.bindInt(14, 0)
                                it.bindNull(15)
                                it.bindNull(16)
                                it.step()
                            }

                            val defaultParentId = WidgetScreenTarget.Default.id

                            connection.prepare(
                                "INSERT INTO Widget (`type`, `position`, `id`, `parentId`) VALUES " +
                                        "('weather', 0, ?, ?)," +
                                        "('music', 1, ?, ?)," +
                                        "('calendar', 2, ?, ?);"
                            ).use {
                                it.bindBlob(1, UUID.randomUUID().toBytes())
                                it.bindBlob(2, defaultParentId.toBytes())
                                it.bindBlob(3, UUID.randomUUID().toBytes())
                                it.bindBlob(4, defaultParentId.toBytes())
                                it.bindBlob(5, UUID.randomUUID().toBytes())
                                it.bindBlob(6, defaultParentId.toBytes())
                                it.step()
                            }
                        }
                    })
                    .addMigrations(
                        Migration_6_7(),
                        Migration_7_8(),
                        Migration_8_9(),
                        Migration_9_10(),
                        Migration_10_11(),
                        Migration_11_12(),
                        Migration_12_13(),
                        Migration_13_14(),
                        Migration_14_15(),
                        Migration_15_16(),
                        Migration_16_17(),
                        Migration_17_18(),
                        Migration_18_19(),
                        Migration_19_20(),
                        Migration_20_21(),
                        Migration_21_22(),
                        Migration_22_23(),
                        Migration_23_24(),
                        Migration_24_25(),
                        Migration_25_26(),
                        Migration_26_27(),
                        Migration_27_28(),
                        Migration_28_29(),
                        Migration_29_30(),
                        Migration_30_31(),
                        Migration_31_32(),
                        Migration_32_33(),
                        Migration_33_34(),
                    )
                    .setDriver(AndroidSQLiteDriver())
                    .build()
            if (_instance == null) _instance = instance
            return instance
        }
    }
}
