package dev.kbzon.launcher.ui.privacy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [fork] Candado de las apps ocultas, compartido por todo el proceso.
 *
 * La UI pide desbloquear con [requestUnlock]; [LauncherPrivacyGuard] atiende la petición
 * (pide huella/rostro/PIN si la protección está activa) y ejecuta la acción si se autoriza.
 * Se vuelve a bloquear cuando el launcher deja de estar visible.
 */
object HiddenItemsLock {
    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private val _requests = MutableSharedFlow<() -> Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    internal val requests: SharedFlow<() -> Unit> = _requests.asSharedFlow()

    /** Ejecuta [onUnlocked] ya mismo si está desbloqueado, o tras autenticar. */
    fun requestUnlock(onUnlocked: () -> Unit) {
        if (_unlocked.value) onUnlocked() else _requests.tryEmit(onUnlocked)
    }

    internal fun markUnlocked() {
        _unlocked.value = true
    }

    fun lock() {
        _unlocked.value = false
    }

    /** true mientras alguna pantalla del launcher muestra apps ocultas (activa FLAG_SECURE). */
    var hiddenContentVisible by mutableStateOf(false)
}
