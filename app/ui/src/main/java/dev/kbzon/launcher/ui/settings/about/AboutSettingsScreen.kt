package dev.kbzon.launcher.ui.settings.about

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import dev.kbzon.launcher.licenses.OpenSourceLicenses
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.preferences.Preference
import dev.kbzon.launcher.ui.component.preferences.PreferenceCategory
import dev.kbzon.launcher.ui.component.preferences.PreferenceScreen
import dev.kbzon.launcher.ui.locals.LocalBackStack
import dev.kbzon.launcher.ui.settings.buildinfo.BuildInfoSettingsRoute
import dev.kbzon.launcher.ui.settings.easteregg.EasterEggSettingsRoute
import dev.kbzon.launcher.ui.settings.license.LicenseRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data object AboutSettingsRoute: NavKey

@Composable
fun AboutSettingsScreen() {
    val viewModel: AboutSettingsScreenVM = viewModel()
    val backStack = LocalBackStack.current
    val context = LocalContext.current
    PreferenceScreen(title = stringResource(R.string.preference_screen_about)) {
        item {
            PreferenceCategory {
                var appVersion by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(null) {
                    appVersion = withContext(Dispatchers.IO) {
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            0
                        ).versionName
                    }
                }
                var easterEggCounter by remember { mutableStateOf(0) }
                Preference(
                    title = stringResource(R.string.preference_version),
                    summary = appVersion,
                    onClick = {
                        easterEggCounter++
                        if (easterEggCounter >= 9) {
                            backStack.add(EasterEggSettingsRoute)
                            easterEggCounter = 0
                        }
                    }
                )
                Preference(
                    title = stringResource(R.string.preference_screen_buildinfo),
                    summary = stringResource(R.string.preference_screen_buildinfo_summary),
                    onClick = {
                        backStack.add(BuildInfoSettingsRoute)
                    }
                )
                Preference( // [fork] Fase 15: depuración movida aquí desde la lista principal
                    title = stringResource(R.string.preference_screen_debug),
                    summary = stringResource(R.string.preference_screen_debug_summary),
                    onClick = {
                        backStack.add(dev.kbzon.launcher.ui.settings.debug.DebugSettingsRoute)
                    }
                )
            }
        }
        item {
            PreferenceCategory(title = stringResource(id = R.string.preference_category_license)) {
                Preference(
                    icon = R.drawable.info_24px,
                    title = stringResource(id = R.string.preference_about_license),
                    summary = stringResource(id = R.string.preference_about_license_summary),
                    onClick = {
                        backStack.add(LicenseRoute())
                    }
                )
            }
        }
        item {
            PreferenceCategory(title = stringResource(id = R.string.preference_category_donate)) {
                Preference(
                    icon = R.drawable.favorite_24px,
                    title = "GitHub Sponsors",
                    summary = "github.com/sponsors/MM2-0",
                    onClick = {

                        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = "https://github.com/sponsors/MM2-0".toUri()
                        })
                    }
                )
            }
        }
        item {
            // [fork] Enlaces propios + atribución al proyecto original (GPL-3.0).
            PreferenceCategory(title = stringResource(id = R.string.preference_category_links)) {
                Preference(
                    icon = R.drawable.github,
                    title = stringResource(R.string.fork_about_source),
                    summary = "github.com/KbzonZito/Launcher",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = "https://github.com/KbzonZito/Launcher".toUri()
                        })
                    }
                )
                Preference(
                    icon = R.drawable.favorite_24px,
                    title = stringResource(R.string.fork_about_kofi),
                    summary = "ko-fi.com/kbzonzito",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = "https://ko-fi.com/kbzonzito".toUri()
                        })
                    }
                )
                Preference(
                    icon = R.drawable.github,
                    title = stringResource(R.string.fork_about_based_on),
                    summary = "github.com/MM2-0/Kvaesitso · GPL-3.0",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = "https://github.com/MM2-0/Kvaesitso".toUri()
                        })
                    }
                )
            }
        }
        item {
            PreferenceCategory(title = stringResource(id = R.string.preference_category_licenses)) {
                for (library in OpenSourceLicenses.sortedBy { it.name.lowercase() }) {
                    Preference(
                        title = library.name,
                        summary = library.description,
                        onClick = {
                            backStack.add(LicenseRoute(libraryName = library.name))
                        }
                    )
                }
            }
        }
    }
}