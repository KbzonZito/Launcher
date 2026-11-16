package dev.kbzon.launcher.icons

import dev.kbzon.launcher.icons.transformations.LauncherIconTransformation

internal interface TransformableDynamicLauncherIcon {
    fun setTransformations(transformations: List<LauncherIconTransformation>)
}