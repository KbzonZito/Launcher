package dev.kbzon.launcher.ui.settings.search

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import dev.kbzon.launcher.ui.R
import dev.kbzon.launcher.ui.component.DismissableBottomSheet
import dev.kbzon.launcher.ui.component.SmallMessage
import dev.kbzon.launcher.ui.component.preferences.GuardedPreference
import dev.kbzon.launcher.ui.component.preferences.ListPreference
import dev.kbzon.launcher.ui.component.preferences.Preference
import dev.kbzon.launcher.ui.component.preferences.PreferenceCategory
import dev.kbzon.launcher.ui.component.preferences.PreferenceScreen
import dev.kbzon.launcher.ui.component.preferences.PreferenceWithSwitch
import dev.kbzon.launcher.ui.component.preferences.SwitchPreference
import dev.kbzon.launcher.ui.launcher.search.filters.SearchFilters
import dev.kbzon.launcher.ui.locals.LocalBackStack
import dev.kbzon.launcher.ui.settings.apps.AppSearchSettingsRoute
import dev.kbzon.launcher.ui.settings.appshortcuts.AppShortcutsSettingsRoute
import dev.kbzon.launcher.ui.settings.calendarsearch.CalendarProviderSettingsRoute
import dev.kbzon.launcher.ui.settings.calendarsearch.CalendarSearchSettingsRoute
import dev.kbzon.launcher.ui.settings.contacts.ContactsSettingsRoute
import dev.kbzon.launcher.ui.settings.favorites.FavoritesSettingsRoute
import dev.kbzon.launcher.ui.settings.filesearch.FileSearchSettingsRoute
import dev.kbzon.launcher.ui.settings.filterbar.FilterBarSettingsRoute
import dev.kbzon.launcher.ui.settings.hiddenitems.HiddenItemsSettingsRoute
import dev.kbzon.launcher.ui.settings.locations.LocationsSettingsRoute
import dev.kbzon.launcher.ui.settings.searchactions.SearchActionsSettingsRoute
import dev.kbzon.launcher.ui.settings.tags.TagsSettingsRoute
import dev.kbzon.launcher.ui.settings.unitconverter.UnitConverterSettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object SearchSettingsRoute : NavKey

@Composable
fun SearchSettingsScreen() {

    val viewModel: SearchSettingsScreenVM = viewModel()
    val context = LocalContext.current

    val backStack = LocalBackStack.current

    var showFilterEditor by remember { mutableStateOf(false) }

    val isTasksAppInstalled by viewModel.isTasksAppInstalled.collectAsStateWithLifecycle()

    val hasAppShortcutsPermission by viewModel.hasAppShortcutPermission.collectAsStateWithLifecycle(
        null
    )
    val hasContactsPermission by viewModel.hasContactsPermission.collectAsStateWithLifecycle(null)
    val hasCalendarPermission by viewModel.hasCalendarPermission.collectAsStateWithLifecycle(null)
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle(null)

    val favorites by viewModel.favorites.collectAsStateWithLifecycle(null)
    val allApps by viewModel.allApps.collectAsStateWithLifecycle(null)
    val appShortcuts by viewModel.appShortcuts.collectAsStateWithLifecycle(null)
    val calendar by viewModel.calendarSearch.collectAsStateWithLifecycle(null)
    val places by viewModel.placesSearch.collectAsStateWithLifecycle(null)
    val contacts by viewModel.contacts.collectAsStateWithLifecycle(null)
    val calculator by viewModel.calculator.collectAsStateWithLifecycle(null)
    val unitConverter by viewModel.unitConverter.collectAsStateWithLifecycle(null)
    val websites by viewModel.websites.collectAsStateWithLifecycle(null)


    val autoFocus by viewModel.autoFocus.collectAsStateWithLifecycle(null)
    val launchOnEnter by viewModel.launchOnEnter.collectAsStateWithLifecycle(null)
    val reverseSearchResults by viewModel.reverseSearchResults.collectAsStateWithLifecycle(null)
    val filterBar by viewModel.filterBar.collectAsStateWithLifecycle(null)

    PreferenceScreen(title = stringResource(R.string.preference_screen_search)) {
        item { // [fork] Fase 15: opciones propias arriba de todo
            PreferenceCategory {
                val forkBackStack = dev.kbzon.launcher.ui.locals.LocalBackStack.current
                Preference(
                    title = stringResource(R.string.fork_settings_search_extras),
                    summary = stringResource(R.string.fork_settings_search_extras_summary),
                    icon = R.drawable.keyboard_24px,
                    onClick = { forkBackStack.add(dev.kbzon.launcher.ui.settings.home.MinimalSearchSettingsRoute) },
                )
            }
        }
        item {
            PreferenceCategory {
                PreferenceWithSwitch(
                    title = stringResource(R.string.preference_search_favorites),
                    summary = stringResource(R.string.preference_search_favorites_summary),
                    icon = R.drawable.star_24px,
                    switchValue = favorites == true,
                    onSwitchChanged = {
                        viewModel.setFavorites(it)
                    },
                    onClick = {
                        backStack.add(FavoritesSettingsRoute)
                    }
                )
                PreferenceWithSwitch(
                    title = stringResource(R.string.preference_search_apps),
                    summary = stringResource(R.string.preference_search_apps_summary),
                    icon = R.drawable.apps_24px,
                    switchValue = allApps == true,
                    onSwitchChanged = {
                        viewModel.setAllApps(it)
                    },
                    onClick = {
                        backStack.add(AppSearchSettingsRoute)
                    }
                )
            }
        }
        item {
            PreferenceCategory {

                Preference(
                    title = stringResource(R.string.preference_search_files),
                    summary = stringResource(R.string.preference_search_files_summary),
                    icon = R.drawable.description_24px,
                    onClick = {
                        backStack.add(FileSearchSettingsRoute)
                    }
                )

                GuardedPreference(
                    locked = hasContactsPermission == false,
                    onUnlock = {
                        viewModel.requestContactsPermission(context as AppCompatActivity)
                    },
                    description = stringResource(R.string.missing_permission_contact_search_settings),
                ) {
                    PreferenceWithSwitch(
                        title = stringResource(R.string.preference_search_contacts),
                        summary = stringResource(R.string.preference_search_contacts_summary),
                        icon = R.drawable.person_24px,
                        switchValue = contacts == true && hasContactsPermission == true,
                        onSwitchChanged = {
                            viewModel.setContacts(it)
                        },
                        onClick = {
                            backStack.add(ContactsSettingsRoute)
                        },
                        enabled = hasContactsPermission == true
                    )
                }
                

                if (isTasksAppInstalled != false) {
                    Preference(
                        title = stringResource(R.string.preference_search_calendar),
                        summary = stringResource(R.string.preference_search_calendar_summary),
                        icon = R.drawable.today_24px,
                        onClick = {
                            backStack.add(CalendarSearchSettingsRoute)
                        },
                    )
                } else {

                    GuardedPreference(
                        locked = hasCalendarPermission == false,
                        onUnlock = {
                            viewModel.requestCalendarPermission(context as AppCompatActivity)
                        },
                        description = stringResource(R.string.missing_permission_calendar_search_settings),
                    ) {
                        PreferenceWithSwitch(
                            title = stringResource(R.string.preference_search_calendar),
                            summary = stringResource(R.string.preference_search_calendar_summary),
                            switchValue = calendar == true,
                            onSwitchChanged = {
                                viewModel.setCalendarSearch(it)
                            },
                            icon = R.drawable.today_24px,
                            enabled = hasCalendarPermission == true,
                            onClick = {
                                backStack.add(CalendarProviderSettingsRoute(providerId = "local"))
                            }
                        )
                    }
                }
                GuardedPreference(
                    locked = hasAppShortcutsPermission == false,
                    onUnlock = {
                        viewModel.requestAppShortcutsPermission(context as AppCompatActivity)
                    },
                    description = stringResource(
                        R.string.missing_permission_appshortcuts_search_settings,
                        stringResource(R.string.app_name),
                    ),
                ) {
                    PreferenceWithSwitch(
                        title = stringResource(R.string.preference_search_appshortcuts),
                        summary = stringResource(R.string.preference_search_appshortcuts_summary),
                        icon = R.drawable.mobile_arrow_up_right_24px,
                        switchValue = appShortcuts == true && hasAppShortcutsPermission == true,
                        onSwitchChanged = {
                            viewModel.setAppShortcuts(it)
                        },
                        enabled = hasAppShortcutsPermission == true,
                        onClick = {
                            backStack += AppShortcutsSettingsRoute
                        }
                    )
                }

                SwitchPreference(
                    title = stringResource(R.string.preference_search_calculator),
                    summary = stringResource(R.string.preference_search_calculator_summary),
                    icon = R.drawable.calculate_24px,
                    value = calculator == true,
                    onValueChanged = {
                        viewModel.setCalculator(it)
                    }
                )

                PreferenceWithSwitch(
                    title = stringResource(R.string.preference_search_unitconverter),
                    summary = stringResource(R.string.preference_search_unitconverter_summary),
                    icon = R.drawable.autorenew_24px,
                    switchValue = unitConverter == true,
                    onSwitchChanged = {
                        viewModel.setUnitConverter(it)
                    },
                    onClick = {
                        backStack.add(UnitConverterSettingsRoute)
                    }
                )

                SwitchPreference(
                    title = stringResource(R.string.preference_search_websites),
                    summary = stringResource(R.string.preference_search_websites_summary),
                    icon = R.drawable.public_24px,
                    value = websites == true,
                    onValueChanged = {
                        viewModel.setWebsites(it)
                    }
                )
                GuardedPreference(
                    locked = hasLocationPermission == false,
                    onUnlock = {
                        viewModel.requestLocationPermission(context as AppCompatActivity)
                    },
                    description = stringResource(R.string.missing_permission_location_search),
                ) {
                    PreferenceWithSwitch(
                        title = stringResource(R.string.preference_search_locations),
                        summary = stringResource(R.string.preference_search_locations_summary),
                        icon = R.drawable.location_on_24px,
                        onClick = {
                            backStack.add(LocationsSettingsRoute)
                        },
                        switchValue = places == true,
                        onSwitchChanged = {
                            viewModel.setPlacesSearch(it)
                        },
                        enabled = hasLocationPermission == true,
                    )
                    
                }

                Preference(
                    title = stringResource(R.string.preference_screen_search_actions),
                    summary = stringResource(R.string.preference_search_search_actions_summary),
                    icon = R.drawable.arrow_outward_24px,
                    onClick = {
                        backStack.add(SearchActionsSettingsRoute)
                    }
                )
            }
        }
        item {
            PreferenceCategory {
                Preference(
                    title = stringResource(R.string.preference_hidden_items),
                    summary = stringResource(R.string.preference_hidden_items_summary),
                    icon = R.drawable.visibility_off_24px,
                    onClick = {
                        backStack.add(HiddenItemsSettingsRoute)
                    }
                )
                Preference(
                    title = stringResource(R.string.preference_screen_tags),
                    summary = stringResource(R.string.preference_screen_tags_summary),
                    icon = R.drawable.tag_24px,
                    onClick = {
                        backStack.add(TagsSettingsRoute)
                    }
                )
            }
        }
        item {
            PreferenceCategory {
                Preference(
                    title = stringResource(R.string.preference_default_filter),
                    summary = stringResource(R.string.preference_default_filter_summary),
                    icon = R.drawable.filter_alt_24px,
                    onClick = {
                        showFilterEditor = true
                    },
                )
                SwitchPreference(
                    title = stringResource(R.string.preference_filter_bar),
                    iconPadding = true,
                    summary = stringResource(R.string.preference_filter_bar_summary),
                    value = filterBar == true,
                    onValueChanged = {
                        viewModel.setFilterBar(it)
                    }
                )
                AnimatedVisibility(filterBar == true) {
                    Preference(
                        title = stringResource(R.string.preference_customize_filter_bar),
                        iconPadding = true,
                        summary = stringResource(R.string.preference_customize_filter_bar_summary),
                        onClick = {
                            backStack.add(FilterBarSettingsRoute)
                        }
                    )
                }
            }
        }
        item {
            PreferenceCategory {
                SwitchPreference(
                    title = stringResource(R.string.preference_search_bar_auto_focus),
                    summary = stringResource(R.string.preference_search_bar_auto_focus_summary),
                    icon = R.drawable.keyboard_24px,
                    value = autoFocus == true,
                    onValueChanged = {
                        viewModel.setAutoFocus(it)
                    }
                )
                SwitchPreference(
                    title = stringResource(R.string.preference_search_bar_launch_on_enter),
                    iconPadding = true,
                    summary = stringResource(R.string.preference_search_bar_launch_on_enter_summary),
                    value = launchOnEnter == true,
                    onValueChanged = {
                        viewModel.setLaunchOnEnter(it)
                    }
                )
            }
        }
        item {
            PreferenceCategory {
                ListPreference(
                    title = stringResource(R.string.preference_layout_search_results),
                    items = listOf(
                        stringResource(R.string.search_results_order_top_down) to false,
                        stringResource(R.string.search_results_order_bottom_up) to true,
                    ),
                    value = reverseSearchResults,
                    onValueChanged = {
                        if (it != null) viewModel.setReverseSearchResults(it)
                    },
                    icon = R.drawable.sort_24px
                )
            }
        }
    }

    DismissableBottomSheet(
        expanded = showFilterEditor,
        onDismissRequest = { showFilterEditor = false }) {
        val filters by viewModel.searchFilters.collectAsStateWithLifecycle()
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            AnimatedVisibility(filters.allowNetwork) {
                SmallMessage(
                    modifier = Modifier.padding(bottom = 16.dp),
                    icon = R.drawable.warning_24px,
                    text = stringResource(R.string.filter_settings_network_warning)
                )
            }
            SearchFilters(
                filters = filters,
                onFiltersChange = {
                    viewModel.setSearchFilters(it)
                },
                settings = true,
            )
        }
    }
}