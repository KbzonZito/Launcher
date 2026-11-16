package de.mm20.launcher2.fork

import android.net.Uri
import coil.intercept.Interceptor
import coil.request.CachePolicy
import coil.request.ImageResult
import de.mm20.launcher2.preferences.privacy.PrivacySettings
import kotlinx.coroutines.flow.first

/**
 * [fork] Con el modo offline activo, las imágenes remotas solo se sirven desde la caché:
 * Coil no hace ninguna petición de red.
 */
class OfflineImageInterceptor(
    private val privacySettings: PrivacySettings,
) : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        if (isRemote(request.data) && privacySettings.offlineMode.first()) {
            return chain.proceed(
                request.newBuilder().networkCachePolicy(CachePolicy.DISABLED).build()
            )
        }
        return chain.proceed(request)
    }

    private fun isRemote(data: Any): Boolean = when (data) {
        is String -> data.startsWith("http://") || data.startsWith("https://")
        is Uri -> data.scheme == "http" || data.scheme == "https"
        else -> false
    }
}
