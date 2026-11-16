package dev.kbzon.launcher.appshortcuts

import android.content.Context
import android.content.Intent
import dev.kbzon.launcher.search.AppShortcut


fun AppShortcut(context: Context, pinRequestIntent: Intent): AppShortcut? {
    return LauncherShortcut.fromPinRequestIntent(context, pinRequestIntent)
        ?: LegacyShortcut.fromPinRequestIntent(context, pinRequestIntent)
}