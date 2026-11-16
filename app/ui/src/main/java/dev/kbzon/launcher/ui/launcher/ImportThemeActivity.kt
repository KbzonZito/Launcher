package dev.kbzon.launcher.ui.launcher

import android.os.Bundle
import androidx.activity.compose.setContent
import dev.kbzon.launcher.ui.base.BaseActivity
import dev.kbzon.launcher.ui.base.ProvideSettings
import dev.kbzon.launcher.ui.overlays.OverlayHost
import dev.kbzon.launcher.ui.settings.appearance.ImportThemeSettingsScreen
import dev.kbzon.launcher.ui.theme.LauncherTheme

class ImportThemeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.data ?: return finish()

        setContent {
            LauncherTheme {
                ProvideSettings {
                    OverlayHost {
                        ImportThemeSettingsScreen(uri)
                    }
                }
            }
        }
    }
}