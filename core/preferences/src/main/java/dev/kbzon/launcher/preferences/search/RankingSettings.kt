package dev.kbzon.launcher.preferences.search

import dev.kbzon.launcher.preferences.LauncherDataStore
import dev.kbzon.launcher.preferences.WeightFactor
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RankingSettings internal constructor(
    private val launcherDataStore: LauncherDataStore,
){
    val weightFactor
        get() = launcherDataStore.data.map { it.rankingWeightFactor }.distinctUntilChanged()

    fun setWeightFactor(weightFactor: WeightFactor) {
        launcherDataStore.update {
            it.copy(rankingWeightFactor = weightFactor)
        }
    }
}