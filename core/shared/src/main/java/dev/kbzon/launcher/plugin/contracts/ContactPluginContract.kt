package dev.kbzon.launcher.plugin.contracts

import dev.kbzon.launcher.search.contact.CustomContactAction
import dev.kbzon.launcher.search.contact.EmailAddress
import dev.kbzon.launcher.search.contact.PhoneNumber
import dev.kbzon.launcher.search.contact.PostalAddress

abstract class ContactPluginContract {
    object ContactColumns: Columns() {
        /**
         * The unique ID of the contact.
         */
        val Id = column<String>("id")

        /**
         * Uri to view the contact.
         */
        val Uri = column<String>("uri")

        /**
         * The display name of the contact.
         * First name + last name, if applicable.
         */
        val Name = column<String>("name")

        /**
         * List of phone numbers associated with the contact.
         */
        val PhoneNumbers = column<List<PhoneNumber>>("phone_numbers")


        /**
         * List of email addresses associated with the contact.
         */
        val EmailAddresses = column<List<EmailAddress>>("email_addresses")

        /**
         * List of postal addresses associated with the contact.
         */
        val PostalAddresses = column<List<PostalAddress>>("postal_addresses")

        /**
         * List of custom actions associated with the contact.
         */
        val CustomActions = column<List<CustomContactAction>>("custom_actions")

        /**
         * Uri to the contact's photo.
         */
        val PhotoUri = column<String>("photo_uri")
    }
}