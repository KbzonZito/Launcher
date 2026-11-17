package dev.kbzon.launcher.locations

import dev.kbzon.launcher.locations.providers.openstreetmaps.OsmLocation
import dev.kbzon.launcher.locations.providers.openstreetmaps.OsmLocationProvider
import dev.kbzon.launcher.search.Location
import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.SearchableRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val locationsModule = module {
    single<OsmLocationProvider> { OsmLocationProvider(androidContext(), get()) }
    single<LocationsRepository> { LocationsRepository(androidContext(), get(), get(), get()) }
    factory<SearchableRepository<Location>>(named<Location>()) { get<LocationsRepository>() }
    factory<SearchableDeserializer>(named(OsmLocation.DOMAIN)) { OsmLocationDeserializer(get()) }
}