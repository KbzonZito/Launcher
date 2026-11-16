package de.mm20.launcher2.fork

import de.mm20.launcher2.search.Article
import de.mm20.launcher2.search.SearchableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * [fork] Bindings propios del fork. Se mantienen aquí para no tocar módulos de upstream.
 */
val forkModule = module {
    // Wikipedia eliminada (privacidad): SearchService sigue pidiendo un repositorio de artículos,
    // así que se entrega uno vacío en vez de modificar services/search.
    single<SearchableRepository<Article>>(named<Article>()) { EmptyArticleRepository }
}

private object EmptyArticleRepository : SearchableRepository<Article> {
    override fun search(query: String, allowNetwork: Boolean): Flow<List<Article>> = flowOf(emptyList())
}
