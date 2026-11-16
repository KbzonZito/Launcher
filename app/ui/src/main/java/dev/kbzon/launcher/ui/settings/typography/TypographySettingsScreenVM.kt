package dev.kbzon.launcher.ui.settings.typography

import android.content.Context
import androidx.lifecycle.ViewModel
import dev.kbzon.launcher.preferences.ui.UiSettings
import dev.kbzon.launcher.themes.ThemeRepository
import dev.kbzon.launcher.themes.typography.Typography
import dev.kbzon.launcher.ui.R
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class TypographySettingsScreenVM : ViewModel(), KoinComponent {

    private val themeRepository: ThemeRepository by inject()
    private val uiSettings: UiSettings by inject()

    val selectedTypography = uiSettings.typographyId
    val typography: Flow<List<Typography>> = themeRepository.typographies.getAll()

    fun getTypography(id: UUID): Flow<Typography?> {
        return themeRepository.typographies.get(id)
    }

    fun updateTypography(typography: Typography) {
        themeRepository.typographies.update(typography)
    }

    fun selectTypography(typography: Typography) {
        uiSettings.setTypographyId(typography.id)
    }

    fun duplicate(typography: Typography) {
        themeRepository.typographies.create(typography.copy(id = UUID.randomUUID()))
    }

    fun delete(typography: Typography) {
        themeRepository.typographies.delete(typography)
    }

    fun createNew(context: Context): UUID {
        val uuid = UUID.randomUUID()
        themeRepository.typographies.create(
            Typography(
                id = uuid,
                name = context.getString(R.string.new_theme_name)
            )
        )
        return uuid
    }
}