package de.mm20.launcher2.fork

import de.mm20.launcher2.search.Article
import de.mm20.launcher2.search.SearchableRepository
import de.mm20.launcher2.plugin.Plugin
import de.mm20.launcher2.plugin.PluginRepository
import de.mm20.launcher2.plugin.PluginType
import kotlinx.coroutines.Job
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
    // Plugins eliminados: los repositorios de datos que todavía preguntan reciben "ningún plugin".
    single<PluginRepository> { NoPluginRepository }
}

private object NoPluginRepository : PluginRepository {
    override fun findMany(type: PluginType?, enabled: Boolean?, packageName: String?): Flow<List<Plugin>> = flowOf(emptyList())
    override fun get(authority: String): Flow<Plugin?> = flowOf(null)
    override fun insertMany(plugins: List<Plugin>): Job = Job().apply { complete() }
    override fun insert(plugin: Plugin): Job = Job().apply { complete() }
    override fun update(plugin: Plugin): Job = Job().apply { complete() }
    override fun updateMany(plugins: List<Plugin>): Job = Job().apply { complete() }
    override fun deleteMany(): Job = Job().apply { complete() }
}

private object EmptyArticleRepository : SearchableRepository<Article> {
    override fun search(query: String, allowNetwork: Boolean): Flow<List<Article>> = flowOf(emptyList())
}
