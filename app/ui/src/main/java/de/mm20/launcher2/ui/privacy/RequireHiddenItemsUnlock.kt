package de.mm20.launcher2.ui.privacy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import de.mm20.launcher2.preferences.privacy.PrivacySettings
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

/**
 * [fork] Muestra [content] solo tras desbloquear las apps ocultas (si la protección está activa).
 * Si el usuario cancela, se llama a [onDenied].
 */
@Composable
fun RequireHiddenItemsUnlock(onDenied: () -> Unit, content: @Composable () -> Unit) {
    val privacySettings: PrivacySettings = koinInject()
    val activity = LocalContext.current as FragmentActivity
    var authorized by remember { mutableStateOf(HiddenItemsLock.unlocked.value) }

    LaunchedEffect(Unit) {
        if (authorized) return@LaunchedEffect
        if (!privacySettings.protectHiddenItems.first() || activity.authenticateUser()) {
            HiddenItemsLock.markUnlocked()
            authorized = true
        } else {
            onDenied()
        }
    }
    if (authorized) content()
}
