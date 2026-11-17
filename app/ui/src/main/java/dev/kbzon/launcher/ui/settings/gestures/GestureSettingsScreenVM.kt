package dev.kbzon.launcher.ui.settings.gestures

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.icons.IconService
import dev.kbzon.launcher.icons.LauncherIcon
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.GestureAction
import dev.kbzon.launcher.preferences.WidgetScreenTarget
import dev.kbzon.launcher.preferences.ui.GestureSettings
import dev.kbzon.launcher.preferences.ui.UiSettings
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.searchable.SavableSearchableRepository
import dev.kbzon.launcher.widgets.Widget
import dev.kbzon.launcher.widgets.WidgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class GestureSettingsScreenVM : ViewModel(), KoinComponent {
    private val gestureSettings: GestureSettings by inject()
    private val uiSettings: UiSettings by inject()
    private val permissionsManager: PermissionsManager by inject()
    private val searchableRepository: SavableSearchableRepository by inject()
    private val iconService: IconService by inject()
    private val widgetRepository: WidgetRepository by inject()

    val hasPermission = permissionsManager.hasPermission(PermissionGroup.Accessibility)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val swipeDown = gestureSettings.swipeDown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val swipeLeft = gestureSettings.swipeLeft
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val swipeRight = gestureSettings.swipeRight
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val swipeUp = gestureSettings.swipeUp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val doubleTap = gestureSettings.doubleTap
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val longPress = gestureSettings.longPress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val homeButton = gestureSettings.homeButton
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setSwipeDown(action: GestureAction, searchable: SavableSearchable?) {
        saveShortcut(searchable)
        gestureSettings.setSwipeDown(action)
    }

    fun setSwipeLeft(action: GestureAction, searchable: SavableSearchable?) {
        saveShortcut(searchable)
        gestureSettings.setSwipeLeft(action)
    }

    fun setSwipeRight(action: GestureAction, searchable: SavableSearchable?) {
        saveShortcut(searchable)
        gestureSettings.setSwipeRight(action)
    }

    fun setSwipeUp(action: GestureAction, searchable: SavableSearchable?) {
        saveShortcut(searchable)
        gestureSettings.setSwipeUp(action)
    }

    fun setDoubleTap(action: GestureAction, searchable: SavableSearchable?) {
        saveShortcut(searchable)
        gestureSettings.setDoubleTap(action)
    }

    fun setLongPress(action: GestureAction, searchable: SavableSearchable?) {
        saveShortcut(searchable)
        gestureSettings.setLongPress(action)
    }

    fun setHomeButton(action: GestureAction, searchable: SavableSearchable?) {
        saveShortcut(searchable)
        gestureSettings.setHomeButton(action)
    }

    // [fork] Gestos propios de la home y botón de acción (todo en esta pantalla).
    private val homeSettings: dev.kbzon.launcher.preferences.home.MinimalHomeSettings by inject()
    val splitSwipeDown = homeSettings.splitSwipeDown
    fun setSplitSwipeDown(enabled: Boolean) = homeSettings.setSplitSwipeDown(enabled)
    val twoFingerUp = homeSettings.twoFingerUp
    val twoFingerDown = homeSettings.twoFingerDown
    fun setTwoFingerUp(action: GestureAction, searchable: SavableSearchable?) {
        saveShortcut(searchable); homeSettings.setTwoFingerUp(action)
    }
    fun setTwoFingerDown(action: GestureAction, searchable: SavableSearchable?) {
        saveShortcut(searchable); homeSettings.setTwoFingerDown(action)
    }
    val actionButton = homeSettings.actionButton
    fun setActionButton(enabled: Boolean) = homeSettings.setActionButton(enabled)
    val actionButtonActions = homeSettings.actionButtonActions
    val actionButtonPosition = homeSettings.actionButtonPosition
    fun setActionButtonPosition(p: dev.kbzon.launcher.preferences.home.ActionButtonPosition) = homeSettings.setActionButtonPosition(p)
    fun setActionButtonAction(
        searchable: SavableSearchable?,
        transform: (dev.kbzon.launcher.preferences.home.ActionButtonActions) -> dev.kbzon.launcher.preferences.home.ActionButtonActions,
    ) {
        saveShortcut(searchable); homeSettings.updateActionButtonActions(transform)
    }

    private fun saveShortcut(searchable: SavableSearchable?) {
        searchable?.let { searchableRepository.insert(it) }
    }

    val shortcutOptions: Flow<List<SavableSearchable>> = combine(
        gestureSettings, homeSettings.twoFingerUp, homeSettings.twoFingerDown, homeSettings.actionButtonActions,
    ) { g, up, down, button -> listOf(g.swipeUp, g.swipeLeft, g.swipeRight, g.swipeDown, g.doubleTap, g.homeButton, g.longPress, up, down) + button.all }
        .flatMapLatest { actions ->
            searchableRepository.getByKeys(actions.mapNotNull { (it as? GestureAction.Launch)?.key })
        }

    @Suppress("unused")
    private val legacyShortcutOptions: Flow<List<SavableSearchable>> = gestureSettings.flatMapLatest {
        val keys = listOfNotNull(
            (it.swipeUp as? GestureAction.Launch)?.key,
            (it.swipeLeft as? GestureAction.Launch)?.key,
            (it.swipeRight as? GestureAction.Launch)?.key,
            (it.swipeDown as? GestureAction.Launch)?.key,
            (it.doubleTap as? GestureAction.Launch)?.key,
            (it.homeButton as? GestureAction.Launch)?.key,
            (it.longPress as? GestureAction.Launch)?.key,
        )
        searchableRepository.getByKeys(keys)
    }

    val widgetOptions: Flow<List<WidgetPageOption>> =
        combine(uiSettings.homeScreenWidgets, gestureSettings) { home, gestures ->
            val options = mutableListOf<WidgetPageOption>()
            for (target in WidgetScreenTarget.entries) {
                // [fork] La home minimalista no lleva widgets: el panel por defecto siempre es una opción.
                val widgets = widgetRepository.get(
                    parent = target.id,
                ).first()

                options += WidgetPageOption(
                    widgets = widgets,
                    id = target,
                )
            }

            options
        }


    fun requestPermission(context: AppCompatActivity) {
        permissionsManager.requestPermission(context, PermissionGroup.Accessibility)
    }

    fun getIcon(searchable: SavableSearchable?, size: Int): Flow<LauncherIcon?> {
        if (searchable == null) return emptyFlow()
        return iconService.getIcon(searchable, size)
    }
}

internal data class WidgetPageOption(
    val id: WidgetScreenTarget,
    val widgets: List<Widget>,
)