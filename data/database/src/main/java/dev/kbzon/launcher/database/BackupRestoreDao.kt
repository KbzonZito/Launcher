package dev.kbzon.launcher.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import dev.kbzon.launcher.database.entities.CustomAttributeEntity
import dev.kbzon.launcher.database.entities.SavedSearchableEntity
import dev.kbzon.launcher.database.entities.SearchActionEntity
import dev.kbzon.launcher.database.entities.WebsearchEntity
import dev.kbzon.launcher.database.entities.WidgetEntity

@Dao
interface BackupRestoreDao {

    @Query("DELETE FROM Searchable")
    suspend fun wipeFavorites()

    @Query("SELECT * FROM Searchable LIMIT :limit OFFSET :offset")
    suspend fun exportFavorites(limit: Int, offset: Int): List<SavedSearchableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun importFavorites(items: List<SavedSearchableEntity>)

    @Query("DELETE FROM Widget")
    suspend fun wipeWidgets()

    @Query("SELECT * FROM Widget LIMIT :limit OFFSET :offset")
    suspend fun exportWidgets(limit: Int, offset: Int): List<WidgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun importWidgets(items: List<WidgetEntity>)

    @Query("DELETE FROM SearchAction")
    suspend fun wipeSearchActions()

    @Query("SELECT * FROM SearchAction LIMIT :limit OFFSET :offset")
    suspend fun exportSearchActions(limit: Int, offset: Int): List<SearchActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun importSearchActions(items: List<SearchActionEntity>)

    @Query("DELETE FROM CustomAttributes")
    suspend fun wipeCustomAttributes()

    @Query("SELECT * FROM CustomAttributes LIMIT :limit OFFSET :offset")
    suspend fun exportCustomAttributes(limit: Int, offset: Int): List<CustomAttributeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun importCustomAttributes(items: List<CustomAttributeEntity>)

    @Query("DELETE FROM CustomAttributes WHERE (type = 'tag' OR type = 'label') AND NOT EXISTS(SELECT 1 FROM Searchable WHERE CustomAttributes.key = Searchable.key)")
    suspend fun cleanUp(): Int
}