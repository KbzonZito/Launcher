package dev.kbzon.launcher.contacts

import dev.kbzon.launcher.contacts.providers.AndroidContact
import dev.kbzon.launcher.contacts.providers.PluginContact
import dev.kbzon.launcher.search.Contact
import dev.kbzon.launcher.search.SearchableDeserializer
import dev.kbzon.launcher.search.SearchableRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val contactsModule = module {
    factory { ContactRepository(androidContext(), get(), get()) }
    factory<SearchableRepository<Contact>>(named<Contact>()) { get<ContactRepository>() }
    factory<SearchableDeserializer>(named(AndroidContact.Domain)) { AndroidContactDeserializer(androidContext(), get()) }
    factory<SearchableDeserializer>(named(PluginContact.Domain)) { PluginContactDeserializer(androidContext(), get()) }
}