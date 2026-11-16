package dev.kbzon.launcher.licenses

import android.content.Context
import dev.kbzon.launcher.base.R

object AppLicense {
    fun get(context: Context): OpenSourceLibrary {
        return OpenSourceLibrary(
            name = context.getString(R.string.app_name),
            description = context.getString(R.string.preference_about_license),
            // Fork de Kvaesitso (GPL-3.0): se conserva el aviso de copyright original.
            copyrightNote = "Copyright (C) 2026 KbzonZito\nBased on Kvaesitso, Copyright (C) 2021–2026 MM2-0 and the Kvaesitso contributors",
            licenseName = R.string.gpl3_name,
            licenseText = R.raw.license_gpl_3,
            url = "https://github.com/KbzonZito/Launcher"
        )
    }
}
