package dev.kbzon.launcher.themes

import android.content.Context
import dev.kbzon.launcher.backup.Backupable
import dev.kbzon.launcher.crashreporter.CrashReporter
import dev.kbzon.launcher.database.AppDatabase
import dev.kbzon.launcher.database.entities.ColorsEntity
import dev.kbzon.launcher.database.entities.ShapesEntity
import dev.kbzon.launcher.database.entities.TransparenciesEntity
import dev.kbzon.launcher.database.entities.TypographyEntity
import dev.kbzon.launcher.serialization.Json
import dev.kbzon.launcher.themes.colors.ColorsRepository
import dev.kbzon.launcher.themes.shapes.ShapesRepository
import dev.kbzon.launcher.themes.transparencies.TransparenciesRepository
import dev.kbzon.launcher.themes.typography.TypographyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.File

class ThemeRepository(
    private val context: Context,
    private val database: AppDatabase,
) : Backupable {
    val colors = ColorsRepository(context, database)
    val shapes = ShapesRepository(context, database)
    val transparencies = TransparenciesRepository(context, database)
    val typographies = TypographyRepository(context, database)


    override suspend fun backup(toDir: File) = withContext(Dispatchers.IO) {
        val dao = database.themeDao()

        val colors = dao.getAllColors().first()
        val colorsFile = File(toDir, "colors.0000")
        colorsFile.bufferedWriter().use {
            it.write(Json.Lenient.encodeToString(colors))
        }

        val shapes = dao.getAllShapes().first()
        val shapesFile = File(toDir, "shapes.0000")
        shapesFile.bufferedWriter().use {
            it.write(Json.Lenient.encodeToString(shapes))
        }

        val transparencies = dao.getAllTransparencies().first()
        val transparenciesFile = File(toDir, "transparencies.0000")
        transparenciesFile.bufferedWriter().use {
            it.write(Json.Lenient.encodeToString(transparencies))
        }

        val typographies = dao.getAllTypographies().first()
        val typographiesFile = File(toDir, "typographies.0000")
        typographiesFile.bufferedWriter().use {
            it.write(Json.Lenient.encodeToString(typographies))
        }
    }

    override suspend fun restore(fromDir: File) = withContext(Dispatchers.IO) {
        val dao = database.themeDao()
        dao.deleteAllColors()

        val colorFiles =
            fromDir.listFiles { _, name -> name.startsWith("colors.") }
                ?: return@withContext

        for (file in colorFiles) {
            val data = file.readText()
            val colors: List<ColorsEntity> = try {
                Json.Lenient.decodeFromString(data)
            } catch (e: SerializationException) {
                CrashReporter.logException(e)
                continue
            } catch (e: IllegalArgumentException) {
                CrashReporter.logException(e)
                continue
            }
            dao.insertAllColors(colors)
        }

        dao.deleteAllShapes()
        val shapeFiles =
            fromDir.listFiles { _, name -> name.startsWith("shapes.") }
                ?: return@withContext
        for (file in shapeFiles) {
            val data = file.readText()
            val shapes: List<ShapesEntity> = try {
                Json.Lenient.decodeFromString(data)
            } catch (e: SerializationException) {
                CrashReporter.logException(e)
                continue
            } catch (e: IllegalArgumentException) {
                CrashReporter.logException(e)
                continue
            }
            dao.insertAllShapes(shapes)
        }

        dao.deleteAllTransparencies()
        val transparencyFiles =
            fromDir.listFiles { _, name -> name.startsWith("transparencies.") }
                ?: return@withContext
        for (file in transparencyFiles) {
            val data = file.readText()
            val transparencies: List<TransparenciesEntity>  = try {
                Json.Lenient.decodeFromString(data)
            } catch (e: SerializationException) {
                CrashReporter.logException(e)
                continue
            } catch (e: IllegalArgumentException) {
                CrashReporter.logException(e)
                continue
            }
            dao.insertAllTransparencies(transparencies)
        }

        dao.deleteAllTypographies()
        val typographyFiles =
            fromDir.listFiles { _, name -> name.startsWith("typographies.") }
                ?: return@withContext
        for (file in typographyFiles) {
            val data = file.readText()
            val typographies: List<TypographyEntity> = try {
                Json.Lenient.decodeFromString(data)
            } catch (e: SerializationException) {
                CrashReporter.logException(e)
                continue
            } catch (e: IllegalArgumentException) {
                CrashReporter.logException(e)
                continue
            }
            dao.insertAllTypographies(typographies)
        }


    }
}