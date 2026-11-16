package dev.kbzon.launcher.icons

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.util.Log
import android.util.LruCache
import dev.kbzon.launcher.data.customattrs.AdaptifiedLegacyIcon
import dev.kbzon.launcher.data.customattrs.CustomAttributesRepository
import dev.kbzon.launcher.data.customattrs.CustomIcon
import dev.kbzon.launcher.data.customattrs.CustomIconPackIcon
import dev.kbzon.launcher.data.customattrs.CustomTextIcon
import dev.kbzon.launcher.data.customattrs.LegacyCustomIconPackIcon
import dev.kbzon.launcher.data.customattrs.CustomThemedIcon
import dev.kbzon.launcher.data.customattrs.DefaultPlaceholderIcon
import dev.kbzon.launcher.data.customattrs.ForceThemedIcon
import dev.kbzon.launcher.data.customattrs.UnmodifiedSystemDefaultIcon
import dev.kbzon.launcher.icons.providers.CalendarIconProvider
import dev.kbzon.launcher.icons.providers.CompatIconProvider
import dev.kbzon.launcher.icons.providers.CustomIconPackIconProvider
import dev.kbzon.launcher.icons.providers.CustomTextIconProvider
import dev.kbzon.launcher.icons.providers.LegacyCustomIconPackIconProvider
import dev.kbzon.launcher.icons.providers.CustomThemedIconProvider
import dev.kbzon.launcher.icons.providers.DynamicClockIconProvider
import dev.kbzon.launcher.icons.providers.IconPackIconProvider
import dev.kbzon.launcher.icons.providers.IconProvider
import dev.kbzon.launcher.icons.providers.PlaceholderIconProvider
import dev.kbzon.launcher.icons.providers.SystemIconProvider
import dev.kbzon.launcher.icons.providers.ThemedPlaceholderIconProvider
import dev.kbzon.launcher.icons.providers.getFirstIcon
import dev.kbzon.launcher.icons.transformations.ForceThemedIconTransformation
import dev.kbzon.launcher.icons.transformations.LauncherIconTransformation
import dev.kbzon.launcher.icons.transformations.LegacyToAdaptiveTransformation
import dev.kbzon.launcher.icons.transformations.transform
import dev.kbzon.launcher.ktx.isAtLeastApiLevel
import dev.kbzon.launcher.preferences.ui.IconSettings
import dev.kbzon.launcher.search.Application
import dev.kbzon.launcher.search.SavableSearchable
import dev.kbzon.launcher.search.Searchable
import dev.kbzon.launcher.search.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class IconService(
    val context: Context,
    private val iconPackManager: IconPackManager,
    private val settings: IconSettings,
    private val customAttributesRepository: CustomAttributesRepository,
) {

    private val appReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            requestIconPackListUpdate()
        }
    }

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private val cache = LruCache<String, LauncherIcon>(200)

    private val iconProviders: MutableStateFlow<List<IconProvider>> = MutableStateFlow(listOf())

    /**
     * Signal that installed icon packs have been updated. Force a reload of all icons.
     */
    private val iconPacksUpdated = MutableSharedFlow<Unit>(1)

    private val transformations: MutableStateFlow<List<LauncherIconTransformation>> =
        MutableStateFlow(
            listOf()
        )

    init {
        requestIconPackListUpdate()
        context.registerReceiver(appReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_MY_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        })

        iconPacksUpdated.tryEmit(Unit)

        scope.launch {
            settings.distinctUntilChanged().collectLatest { settings ->
                iconPacksUpdated.collectLatest {
                    val fallbackProvider = if (settings.themedIcons) {
                        ThemedPlaceholderIconProvider(context)
                    } else {
                        PlaceholderIconProvider(context)
                    }
                    val providers = mutableListOf<IconProvider>()

                    if (!settings.iconPack.isNullOrBlank()) {
                        val pack = iconPackManager.getIconPack(settings.iconPack!!)
                        if (pack != null) {
                            providers.add(
                                IconPackIconProvider(
                                    context,
                                    pack,
                                    iconPackManager,
                                    settings.themedIcons,
                                )
                            )
                        } else {
                            Log.w("MM20", "Icon pack ${settings.iconPack} not found")
                        }
                    }
                    providers.add(DynamicClockIconProvider(context, settings.themedIcons))
                    providers.add(CalendarIconProvider(context, settings.themedIcons))
                    if (!isAtLeastApiLevel(33)) {
                        providers.add(CompatIconProvider(context, settings.themedIcons))
                    }
                    providers.add(SystemIconProvider(context, settings.themedIcons))
                    providers.add(fallbackProvider)
                    cache.evictAll()

                    val transformations = mutableListOf<LauncherIconTransformation>()

                    if (settings.adaptify) transformations.add(LegacyToAdaptiveTransformation())
                    if (settings.themedIcons && settings.forceThemed) transformations.add(
                        ForceThemedIconTransformation()
                    )

                    iconProviders.value = providers
                    this@IconService.transformations.value = transformations
                }
            }
        }
    }

    fun getCustomIcon(searchable: SavableSearchable) : Flow<CustomIcon?> {
        return customAttributesRepository.getCustomIcon(searchable)
    }


    fun getIcon(searchable: SavableSearchable, size: Int): Flow<LauncherIcon?> {
        if (searchable is Application && searchable.isPrivate) {
            return transformations.map {
                searchable.getPlaceholderIcon(context).transform(it)
            }
        }

        val customIcon = getCustomIcon(searchable)

        return customIcon.flatMapLatest {
            resolveCustomIcon(searchable, size, it)
        }
    }

    fun resolveCustomIcon(searchable: SavableSearchable, size: Int, customIcon: CustomIcon?): Flow<LauncherIcon?> {
        return combine(iconProviders, transformations) { providers, transformations ->
            var icon: LauncherIcon? = cache.get(searchable.key + customIcon.hashCode() + providers.hashCode() + transformations.hashCode())
            if (icon != null) {
                return@combine icon
            }

            val provs = if (customIcon != null) getProviders(customIcon) + providers else providers
            val transforms = getTransformations(customIcon) ?: transformations

            icon = provs.getFirstIcon(searchable, size)

            if (icon != null) {
                icon = icon.transform(transforms)

                cache.put(searchable.key + customIcon.hashCode() + providers.hashCode() + transformations.hashCode(), icon)
            }
            return@combine icon
        }
    }

    private fun getProviders(customIcon: CustomIcon?): List<IconProvider> {
        if (customIcon is UnmodifiedSystemDefaultIcon) {
            return listOf(
                SystemIconProvider(context, false)
            )
        }
        if (customIcon is CustomIconPackIcon) {
            return listOf(
                CustomIconPackIconProvider(
                    customIcon,
                    iconPackManager
                )
            )
        }
        if (customIcon is LegacyCustomIconPackIcon) {
            return listOf(
                LegacyCustomIconPackIconProvider(
                    customIcon,
                    iconPackManager
                )
            )
        }
        if (customIcon is CustomThemedIcon) {
            return listOf(
                CustomThemedIconProvider(
                    customIcon,
                    iconPackManager
                )
            )
        }
        if (customIcon is DefaultPlaceholderIcon) {
            return iconProviders.value.lastOrNull()?.let { listOf(it) } ?: emptyList()
        }
        if (customIcon is CustomTextIcon) {
            return listOf(CustomTextIconProvider(customIcon))
        }
        return emptyList()
    }

    private fun getTransformations(customIcon: CustomIcon?): List<LauncherIconTransformation>? {
        customIcon ?: return null
        if (customIcon is AdaptifiedLegacyIcon) {
            return listOf(
                LegacyToAdaptiveTransformation(
                    foregroundScale = customIcon.fgScale,
                    backgroundColor = customIcon.bgColor
                )
            )
        }
        if (customIcon is ForceThemedIcon) {
            return listOf(
                ForceThemedIconTransformation()
            )
        }
        if (customIcon is UnmodifiedSystemDefaultIcon) {
            return emptyList()
        }
        return null
    }


    fun requestIconPackListUpdate() {
        scope.launch {
            iconPackManager.updateIconPacks().also {
                if (it)iconPacksUpdated.tryEmit(Unit)
            }
        }
    }

    fun reinstallAllIconPacks() {
        scope.launch {
            iconPackManager.updateIconPacks(forceReinstall = true)
            iconPacksUpdated.tryEmit(Unit)
        }
    }

    fun getInstalledIconPacks(): Flow<List<IconPack>> {
        return iconPackManager.getInstalledIconPacks()
    }

    suspend fun getCustomIconSuggestions(
        searchable: SavableSearchable,
        size: Int
    ): List<CustomIconWithPreview> {
        val suggestions = mutableListOf<CustomIconWithPreview>()

        val rawIcon = iconProviders.first().getFirstIcon(searchable, size) ?: return emptyList()

        val defaultTransformations = transformations.first()

        val transformationOptions = mutableListOf<CustomIcon>()

        if (searchable is Application) {
            transformationOptions.add(UnmodifiedSystemDefaultIcon)
        }

        if (rawIcon is StaticLauncherIcon && rawIcon.backgroundLayer is TransparentLayer) {
            // Legacy icons that simply fill the entire canvas
            transformationOptions.add(
                AdaptifiedLegacyIcon(
                    fgScale = 1f,
                    bgColor = 1
                )
            )
            // 48x48 with 5px padding used to be the default icon size for icons generated by
            // the Android Studio asset generator. Upscale these icons to remove that padding.

            transformationOptions.add(
                AdaptifiedLegacyIcon(
                    fgScale = 48f / 38f,
                    bgColor = 1
                )
            )

            // Android 7.1 round icons (48x48 circle with 1px padding)
            transformationOptions.add(
                AdaptifiedLegacyIcon(
                    fgScale = 48f / 44f,
                    bgColor = 1
                )
            )
            transformationOptions.add(
                AdaptifiedLegacyIcon(
                    fgScale = 0.7f,
                    bgColor = 0
                )
            )
            transformationOptions.add(
                AdaptifiedLegacyIcon(
                    fgScale = 0.7f,
                    bgColor = Color.WHITE,
                )
            )
        }

        val providerOptions = mutableListOf<CustomIcon>()

        if (searchable is Application) {
            val iconPackIcons = iconPackManager.getAllIconPackIcons(
                searchable.componentName
            )

            providerOptions.addAll(
                iconPackIcons.map {
                    val ent = it.toDatabaseEntity()
                    CustomIconPackIcon(
                        iconPackPackage = it.iconPack,
                        type = ent.type,
                        drawable = ent.drawable,
                        extras = ent.extras,
                        allowThemed = it.themed,
                    )
                }
            )
            transformationOptions.add(
                ForceThemedIcon
            )
        }

        if (searchable !is Tag) {
            providerOptions.add(DefaultPlaceholderIcon)
        }

        suggestions.addAll(
            transformationOptions.map {
                val transformations = getTransformations(it) ?: defaultTransformations
                val providers = getProviders(it)

                val icon = providers.getFirstIcon(searchable, size) ?: rawIcon

                CustomIconWithPreview(
                    preview = icon.transform(transformations),
                    customIcon = it,
                )

            }
        )

        suggestions.addAll(
            providerOptions.mapNotNull {
                val providers = getProviders(it)

                val icon = providers.getFirstIcon(searchable, size) ?: return@mapNotNull null

                CustomIconWithPreview(
                    preview = icon.transform(defaultTransformations),
                    customIcon = it,
                )

            }
        )

        return suggestions

    }

    suspend fun getUncustomizedDefaultIcon(
        searchable: SavableSearchable,
        size: Int
    ): CustomIconWithPreview? {
        val icon = iconProviders.first().getFirstIcon(searchable, size)
            ?.transform(transformations.first()) ?: return null
        return CustomIconWithPreview(
            customIcon = null,
            preview = icon
        )
    }

    suspend fun searchCustomIcons(query: String, iconPack: IconPack?): List<CustomIconWithPreview> {
        val transformations = this.transformations.first()
        val iconPackIcons = iconPackManager.searchIconPackIcon(query, iconPack).flatMap {
            val themedIcon = if (it.themed) {
                iconPackManager.getIcon(it.iconPack, it, true)
                    ?.transform(transformations)
            } else null
            val unthemedIcon = iconPackManager.getIcon(it.iconPack, it, false)
                ?.transform(transformations)

            buildList {
                val ent = it.toDatabaseEntity()
                if (unthemedIcon != null) {
                    add(CustomIconWithPreview(
                        customIcon = CustomIconPackIcon(
                            iconPackPackage = it.iconPack,
                            type = ent.type,
                            drawable = ent.drawable,
                            extras = ent.extras,
                            allowThemed = false,
                        ),
                        preview = unthemedIcon
                    ))
                }
                if (themedIcon != null) {
                    add(CustomIconWithPreview(
                        customIcon = CustomIconPackIcon(
                            iconPackPackage = it.iconPack,
                            type = ent.type,
                            drawable = ent.drawable,
                            extras = ent.extras,
                            allowThemed = true,
                        ),
                        preview = themedIcon
                    ))
                }
            }
        }

        return iconPackIcons
    }

    fun setCustomIcon(searchable: SavableSearchable, icon: CustomIcon?) {
        customAttributesRepository.setCustomIcon(searchable, icon)
    }

}

data class CustomIconWithPreview(
    val preview: LauncherIcon,
    val customIcon: CustomIcon?,
)