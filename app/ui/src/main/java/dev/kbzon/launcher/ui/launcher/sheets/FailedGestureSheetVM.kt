package dev.kbzon.launcher.ui.launcher.sheets

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import dev.kbzon.launcher.permissions.PermissionGroup
import dev.kbzon.launcher.permissions.PermissionsManager
import dev.kbzon.launcher.preferences.GestureAction
import dev.kbzon.launcher.preferences.ui.GestureSettings
import dev.kbzon.launcher.ui.launcher.scaffold.Gesture
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FailedGestureSheetVM : ViewModel(), KoinComponent {
    private val permissionsManager: PermissionsManager by inject()
    private val gestureSettings: GestureSettings by inject()

    fun requestPermission(context: AppCompatActivity) {
        permissionsManager.requestPermission(context, PermissionGroup.Accessibility)
    }

    fun disableGesture(gesture: Gesture) {
        when(gesture) {
            Gesture.DoubleTap -> gestureSettings.setDoubleTap(GestureAction.NoAction)
            Gesture.LongPress -> gestureSettings.setLongPress(GestureAction.NoAction)
            Gesture.SwipeDown -> gestureSettings.setSwipeDown(GestureAction.NoAction)
            Gesture.SwipeLeft -> gestureSettings.setSwipeLeft(GestureAction.NoAction)
            Gesture.SwipeRight -> gestureSettings.setSwipeRight(GestureAction.NoAction)
            //Gesture.HomeButton -> gestureSettings.setHomeButton(GestureAction.NoAction)
            else -> {}
        }
    }
}