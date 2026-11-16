package dev.kbzon.launcher.ui.launcher.scaffold.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.focusable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.kbzon.launcher.ui.launcher.search.SearchVM
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.alpha
import dev.kbzon.launcher.ui.home.MinimalHome
import dev.kbzon.launcher.ui.launcher.scaffold.LauncherScaffoldState

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
        // Teclado físico (Tab S9 con funda teclado): escribir en la home abre la búsqueda con ese texto.
        val searchVM: SearchVM = viewModel()
        val scope = rememberCoroutineScope()
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(state.currentComponent) {
            if (state.currentComponent == null) runCatching { focusRequester.requestFocus() }
        }
        val scaffoldScope = rememberCoroutineScope()
        MinimalHome(
            insets = insets,
            listState = listState,
            openSearch = { scaffoldScope.launch { state.onSearchBarTap() } },
            openWidgets = {
                scaffoldScope.launch {
                    state.gestureFor { it is WidgetsComponent }?.let { state.navigateToPage(it) }
                }
            },
            modifier = modifier
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                    if (event.isCtrlPressed || event.isAltPressed || event.isMetaPressed) return@onKeyEvent false
                    val codePoint = event.utf16CodePoint
                    if (codePoint <= 0 || Character.isISOControl(codePoint) || Character.isWhitespace(codePoint)) {
                        return@onKeyEvent false
                    }
                    searchVM.search(String(Character.toChars(codePoint)))
                    scope.launch { state.onSearchBarTap() }
                    state.isSearchBarFocused = true
                    true
                },
        )
    }
}
