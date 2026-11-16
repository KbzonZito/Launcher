package de.mm20.launcher2.ui.launcher.scaffold.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import de.mm20.launcher2.ui.home.MinimalHome
import de.mm20.launcher2.ui.launcher.scaffold.LauncherScaffoldState

/**
 * [fork] Home minimalista como componente del scaffold: los gestos (deslizar para buscar, etc.)
 * los sigue manejando LauncherScaffold.
 */
internal object MinimalHomeComponent : ScaffoldComponent() {
    private val listState = LazyListState()

    override val drawBackground: Boolean = false
    override val showSearchBar: Boolean = false

    override val isAtTop: State<Boolean?> = derivedStateOf { !listState.canScrollBackward }
    override val isAtBottom: State<Boolean?> = derivedStateOf { !listState.canScrollForward }

    /** Solo se llama en reposo: la barra de búsqueda aparece al deslizar hacia la búsqueda. */
    override fun searchBarModifier(state: LauncherScaffoldState, defaultModifier: Modifier): Modifier =
        defaultModifier.alpha(0f)

    @Composable
    override fun Component(
        modifier: Modifier,
        insets: PaddingValues,
        state: LauncherScaffoldState,
    ) {
        MinimalHome(insets = insets, listState = listState, modifier = modifier)
    }
}
