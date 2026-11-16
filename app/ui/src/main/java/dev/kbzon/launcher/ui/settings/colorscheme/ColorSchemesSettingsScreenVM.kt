package dev.kbzon.launcher.ui.settings.colorscheme

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.ktx.tryStartActivity
import dev.kbzon.launcher.preferences.ui.UiSettings
import dev.kbzon.launcher.themes.ThemeRepository
import dev.kbzon.launcher.themes.colors.Colors
import dev.kbzon.launcher.themes.toLegacyJson
import dev.kbzon.launcher.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.util.UUID

class ColorSchemesSettingsScreenVM : ViewModel(), KoinComponent {

    private val themeRepository: ThemeRepository by inject()
    private val uiSettings: UiSettings by inject()

    val selectedColors = uiSettings.colorsId
    val colors: Flow<List<Colors>> = themeRepository.colors.getAll()

    fun getTheme(id: UUID): Flow<Colors?> {
        return themeRepository.colors.get(id)
    }

    fun updateTheme(colors: Colors) {
        themeRepository.colors.update(colors)
    }

    fun selectTheme(colors: Colors) {
        uiSettings.setColorsId(colors.id)
    }

    fun duplicate(colors: Colors) {
        themeRepository.colors.create(colors.copy(id = UUID.randomUUID()))
    }

    fun delete(colors: Colors) {
        themeRepository.colors.delete(colors)
    }

    fun exportTheme(context: Context, colors: Colors) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "${colors.name}.kvtheme")
                file.writeText(colors.toLegacyJson())
                file
            }
            context.tryStartActivity(Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/json"
                putExtra(
                    Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".fileprovider",
                        file
                    )
                )
            }.let { Intent.createChooser(it, null) })
        }
    }

    fun createNew(context: Context): UUID {
        val uuid = UUID.randomUUID()
        themeRepository.colors.create(
            Colors(
                id = uuid,
                name = context.getString(R.string.new_theme_name)
            )
        )
        return uuid
    }
}