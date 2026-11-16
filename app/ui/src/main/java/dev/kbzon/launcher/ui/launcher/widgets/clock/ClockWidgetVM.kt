package dev.kbzon.launcher.ui.launcher.widgets.clock

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbzon.launcher.ktx.isAtLeastApiLevel
import dev.kbzon.launcher.ktx.tryStartActivity
import dev.kbzon.launcher.preferences.ui.ClockWidgetSettings
import dev.kbzon.launcher.ui.launcher.widgets.clock.parts.AlarmPartProvider
import dev.kbzon.launcher.ui.launcher.widgets.clock.parts.BatteryPartProvider
import dev.kbzon.launcher.ui.launcher.widgets.clock.parts.DatePartProvider
import dev.kbzon.launcher.ui.launcher.widgets.clock.parts.FavoritesPartProvider
import dev.kbzon.launcher.ui.launcher.widgets.clock.parts.MusicPartProvider
import dev.kbzon.launcher.ui.launcher.widgets.clock.parts.PartProvider
import dev.kbzon.launcher.ui.launcher.widgets.clock.parts.SmartspacerPartProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ClockWidgetVM : ViewModel(), KoinComponent {
    private val settings: ClockWidgetSettings by inject()

    private val partProviders = settings.parts.combine(settings.useSmartspacer) { p, s ->
        p to s
    }.map { (parts, smartspacer) ->
        if (smartspacer && isAtLeastApiLevel(29)) {
            return@map listOf(SmartspacerPartProvider())
        }

        val providers = mutableListOf<PartProvider>()
        if (parts.date) providers += DatePartProvider()
        if (parts.music) providers += MusicPartProvider()
        providers += BatteryPartProvider(parts.battery)
        if (parts.alarm) providers += AlarmPartProvider()
        providers
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun getActivePart(context: Context): Flow<PartProvider?> = channelFlow {
        partProviders.collectLatest { providers ->
            if (providers.isEmpty()) {
                send(null)
                return@collectLatest
            }
            val rankings = providers.map { it.getRanking(context).map { r -> r to it } }
            combine(rankings) { r ->
                r.filter { it.first > 0 }.maxByOrNull { it.first }?.second
            }.collectLatest {
                send(it)
            }
        }
    }

    val compactLayout = settings.compact
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val clockStyle = settings.clockStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val color = settings.color
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val alignment = settings.alignment
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val dockProvider = settings.dock
        .map { if (it) FavoritesPartProvider() else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun updateTime(time: Long) {
        partProviders.value.forEach { it.setTime(time) }
    }

    fun launchClockApp(context: Context) {
        context.tryStartActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
