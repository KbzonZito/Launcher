package dev.kbzon.launcher

import dev.kbzon.launcher.base.BuildConfig

object FeatureFlags {
    val feed = BuildConfig.BUILD_TYPE != "release"
    val smartspacerIntegration = BuildConfig.BUILD_TYPE != "release"
}