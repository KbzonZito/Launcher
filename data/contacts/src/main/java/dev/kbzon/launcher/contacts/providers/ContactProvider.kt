package dev.kbzon.launcher.contacts.providers

import dev.kbzon.launcher.search.Contact

internal interface ContactProvider {
    suspend fun search(query: String, allowNetwork: Boolean): List<Contact>
}