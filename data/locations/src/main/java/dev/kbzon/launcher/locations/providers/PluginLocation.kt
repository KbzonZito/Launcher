package dev.kbzon.launcher.locations.providers

import android.content.Context
import android.graphics.drawable.Drawable
import dev.kbzon.launcher.locations.PluginLocationSerializer
import dev.kbzon.launcher.plugin.config.StorageStrategy
import dev.kbzon.launcher.search.Location
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.SearchableSerializer
import dev.kbzon.launcher.search.UpdatableSearchable
import dev.kbzon.launcher.search.UpdateResult
import dev.kbzon.launcher.search.location.Address
import dev.kbzon.launcher.search.location.Attribution
import dev.kbzon.launcher.search.location.Departure
import dev.kbzon.launcher.search.location.LocationIcon
import dev.kbzon.launcher.search.location.OpeningSchedule
import dev.kbzon.launcher.search.location.PaymentMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PluginLocation(
    override val latitude: Double,
    override val longitude: Double,
    override val fixMeUrl: String?,
    override val icon: LocationIcon?,
    override val category: String?,
    override val address: Address?,
    override val openingSchedule: OpeningSchedule?,
    override val websiteUrl: String?,
    override val phoneNumber: String?,
    override val emailAddress: String?,
    override val userRating: Float?,
    override val userRatingCount: Int?,
    override val departures: List<Departure>?,
    override val label: String,
    override val timestamp: Long,
    override val attribution: Attribution?,
    override val acceptedPaymentMethods: Map<PaymentMethod, Boolean>?,
    override val updatedSelf: (suspend (SavableSearchable) -> UpdateResult<Location>)?,
    override val labelOverride: String? = null,
    val authority: String,
    val id: String,
    val storageStrategy: StorageStrategy,
) : Location, UpdatableSearchable<Location> {
    override val key: String
        get() = "$domain://$authority:$id"

    override fun overrideLabel(label: String): PluginLocation {
        return this.copy(labelOverride = label)
    }

    override val domain: String = DOMAIN

    override fun getSerializer(): SearchableSerializer {
        return PluginLocationSerializer()
    }

    override suspend fun getProviderIcon(context: Context): Drawable? {
        return withContext(Dispatchers.IO) {
            context.packageManager.resolveContentProvider(authority, 0)
                ?.loadIcon(context.packageManager)
        }
    }

    companion object {
        const val DOMAIN = "plugin.location"
    }
}