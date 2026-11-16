package dev.kbzon.launcher.ui.settings.shapes

import android.content.Context
import androidx.lifecycle.ViewModel
import dev.kbzon.launcher.preferences.ui.UiSettings
import dev.kbzon.launcher.themes.ThemeRepository
import dev.kbzon.launcher.themes.shapes.Shapes
import dev.kbzon.launcher.ui.R
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class ShapeSchemesSettingsScreenVM : ViewModel(), KoinComponent {

    private val themeRepository: ThemeRepository by inject()
    private val uiSettings: UiSettings by inject()

    val selectedShapesId = uiSettings.shapesId
    val shapes: Flow<List<Shapes>> = themeRepository.shapes.getAll()

    fun getShapes(id: UUID): Flow<Shapes?> {
        return themeRepository.shapes.get(id)
    }

    fun updateShapes(shapes: Shapes) {
        themeRepository.shapes.update(shapes)
    }

    fun selectShapes(shapes: Shapes) {
        uiSettings.setShapesId(shapes.id)
    }

    fun duplicate(shapes: Shapes) {
        themeRepository.shapes.create(shapes.copy(id = UUID.randomUUID()))
    }

    fun delete(shapes: Shapes) {
        themeRepository.shapes.delete(shapes)
    }

    fun createNew(context: Context): UUID {
        val uuid = UUID.randomUUID()
        themeRepository.shapes.create(
            Shapes(
                id = uuid,
                name = context.getString(R.string.new_theme_name)
            )
        )
        return uuid
    }
}