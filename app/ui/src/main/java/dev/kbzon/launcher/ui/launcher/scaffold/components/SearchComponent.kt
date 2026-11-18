package dev.kbzon.launcher.ui.launcher.scaffold.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.kbzon.launcher.ui.launcher.scaffold.LauncherScaffoldState
import dev.kbzon.launcher.ui.launcher.search.SearchColumn
import dev.kbzon.launcher.ui.launcher.search.SearchVM

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
internal class SearchComponent(
    private val reverse: Boolean = false,
    private val openKeyboard: Boolean = true,
    private val minimalStyle: Boolean = false, // [fork]
) : ScaffoldComponent() {

    private val lazyListState = LazyListState()

    override val isAtTop: State<Boolean?> = derivedStateOf {
        reverse && !lazyListState.canScrollForward || !reverse && !lazyListState.canScrollBackward
    }

    override val isAtBottom: State<Boolean?> = derivedStateOf {
        !reverse && !lazyListState.canScrollForward || reverse && !lazyListState.canScrollBackward
    }

    override val reverseScrolling: Boolean = reverse

    override val hasIme: Boolean = true


    @Composable
    override fun Component(
        modifier: Modifier,
        insets: PaddingValues,
        state: LauncherScaffoldState
    ) {
        val searchVM = viewModel<SearchVM>()

        SideEffect(isActive) {
            if (!isActive) {
                searchVM.reset()
            }
        }

        // [fork] F16: el "atrás" que cierra el teclado cierra también la búsqueda (antes eran dos o más).
        // Cuando la app oculta el teclado (al desplazar, al tocar un resultado) antes pone el foco en false,
        // así que un teclado que se cierra con la barra aún enfocada lo cerró el usuario.
        val imeVisible = WindowInsets.isImeVisible
        val imeWasVisible = remember { mutableStateOf(false) }
        val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(imeVisible) {
            if (imeVisible) {
                imeWasVisible.value = true
            } else if (imeWasVisible.value) {
                imeWasVisible.value = false
                if (isActive && state.isSearchBarFocused && state.isSettledOnSecondaryPage &&
                    lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)
                ) {
                    state.navigateBack()
                }
            }
        }

        SideEffect(searchVM.searchQuery.value, searchVM.filters.value) {
            lazyListState.requestScrollToItem(0, 0)
        }


        val scrollConnection = remember(state) {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    searchVM.bestMatch.value = null
                    state.isSearchBarFocused = false
                    state.onComponentScroll(
                        if (reverse) consumed.y else -consumed.y,
                    )
                    return super.onPostScroll(consumed, available, source)
                }
            }
        }

        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {

            val column: @Composable () -> Unit = {
                SearchColumn(
                    modifier = Modifier
                        .nestedScroll(scrollConnection)
                        .widthIn(max = 916.dp)
                        .fillMaxHeight(),
                    paddingValues = insets,
                    state = lazyListState,
                    reverse = reverse,
                    userScrollEnabled = !state.isDragged,
                    onHideKeyboard = {
                        state.isSearchBarFocused = false
                    }
                )
            }
            if (minimalStyle) dev.kbzon.launcher.ui.search.MinimalSearchTheme(column) else column()
        }
    }

    override suspend fun onDismiss(state: LauncherScaffoldState) {
        super.onDismiss(state)
        lazyListState.scrollToItem(0, 0)
    }

    override fun onPreActivate(state: LauncherScaffoldState) {
        super.onPreActivate(state)
        if (openKeyboard) {
            state.isSearchBarFocused = true
        }
    }

    override fun onPreDismiss(state: LauncherScaffoldState) {
        super.onPreDismiss(state)
        state.isSearchBarFocused = false
    }
}