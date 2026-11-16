package de.mm20.launcher2.ui.privacy

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import de.mm20.launcher2.ui.R
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * [fork] Pide huella, rostro o el PIN/patrón del dispositivo. Devuelve true si se autorizó.
 */
suspend fun FragmentActivity.authenticateUser(): Boolean = suspendCancellableCoroutine { cont ->
    val prompt = BiometricPrompt(
        this,
        ContextCompat.getMainExecutor(this),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (cont.isActive) cont.resume(true)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (cont.isActive) cont.resume(false)
            }
            // onAuthenticationFailed: intento fallido, el diálogo sigue abierto.
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(getString(R.string.fork_unlock_hidden_items_title))
        .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        .build()
    prompt.authenticate(info)
    cont.invokeOnCancellation { prompt.cancelAuthentication() }
}
