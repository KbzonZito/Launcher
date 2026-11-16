package dev.kbzon.launcher.ui.launcher.sheets

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import dev.kbzon.launcher.ui.settings.SettingsActivity

class HiddenItemsSheetVM: ViewModel() {

    fun showHiddenItems(context: Context) {
        context.startActivity(
            Intent(context, SettingsActivity::class.java).apply {
                putExtra(SettingsActivity.EXTRA_ROUTE, SettingsActivity.ROUTE_HIDDEN_ITEMS)
            }
        )
    }
}