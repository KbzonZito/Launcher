package dev.kbzon.launcher.locations.providers

import dev.kbzon.launcher.search.Location
import dev.kbzon.launcher.search.UpdateResult

internal typealias AndroidLocation = android.location.Location

internal interface LocationProvider<TId> {
    suspend fun search(
        query: String,
        userLocation: AndroidLocation,
        allowNetwork: Boolean,
        searchRadiusMeters: Int,
        hideUncategorized: Boolean
    ): List<Location>
}