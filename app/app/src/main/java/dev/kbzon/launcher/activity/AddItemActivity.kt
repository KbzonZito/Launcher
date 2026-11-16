package dev.kbzon.launcher.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import dev.kbzon.launcher.appshortcuts.AppShortcut
import dev.kbzon.launcher.services.favorites.FavoritesService
import org.koin.android.ext.android.inject

class AddItemActivity : Activity() {

    private val favoritesService: FavoritesService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shortcut = AppShortcut(this, intent)
        if (shortcut != null) {
            favoritesService.pinItem(shortcut)
        } else {
            Log.w("MM20", "Shortcut could not be added")
        }
        finish()
    }
}
