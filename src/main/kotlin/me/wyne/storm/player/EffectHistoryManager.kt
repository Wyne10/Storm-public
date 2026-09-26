package me.wyne.storm.player

import com.google.inject.Inject
import com.google.inject.Singleton
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import me.wyne.connection.api.ConnectionProvider
import me.wyne.wutils.common.loadable.Loadable
import me.wyne.wutils.common.loadable.Loader
import me.wyne.storm.Storm
import org.bukkit.configuration.ConfigurationSection
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface EffectHistoryManager : AutoCloseable {
    fun record(
        player: UUID,
        effectInstanceKey: String,
        action: EffectHistoryAction,
        durationMillis: Long,
        source: String?
    ): CompletableFuture<Void>
}

@Singleton
class SqlEffectHistoryManager @Inject constructor(
    plugin: Storm,
    private val connectionProvider: ConnectionProvider,
    private val serverIdentity: ServerIdentity
) : Loadable, EffectHistoryManager {

    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var bound: Bound? = null

    private class Bound(val source: ConnectionSource, val dao: Dao<PlayerEffectHistory, Long>)

    init {
        Loader.global.registerLoadable(this)
        plugin.bind(this)
    }

    override fun load(config: ConfigurationSection) {
        if (!connectionProvider.isActive) {
            Storm.logger.error("Couldn't connect to effect history database, subsequent requests will fail")
            return
        }
        dao(requireSource())
    }

    override fun record(
        player: UUID,
        effectInstanceKey: String,
        action: EffectHistoryAction,
        durationMillis: Long,
        source: String?
    ): CompletableFuture<Void> {
        val entry = PlayerEffectHistory(player, effectInstanceKey, action, durationMillis, source, serverIdentity.name)
        return loggedWrite(executor, "record $action of effect '$effectInstanceKey' of $player in the history") {
            dao(requireSource()).create(entry)
        }
    }

    override fun close() {
        executor.shutdown()
        if (!executor.awaitTermination(DRAIN_SECONDS, TimeUnit.SECONDS)) {
            executor.shutdownNow()
            if (!executor.awaitTermination(ABORT_SECONDS, TimeUnit.SECONDS))
                Storm.logger.error("Effect history executor didn't terminate")
        }
    }

    private fun requireSource(): ConnectionSource =
        checkNotNull(if (connectionProvider.isActive) connectionProvider.connectionPool?.source else null) {
            "Effect history storage is unavailable"
        }

    private fun dao(source: ConnectionSource): Dao<PlayerEffectHistory, Long> {
        bound?.takeIf { it.source === source }?.let { return it.dao }
        val dao: Dao<PlayerEffectHistory, Long> = DaoManager.createDao(source, PlayerEffectHistory::class.java)
        if (!dao.isTableExists)
            TableUtils.createTable(source, PlayerEffectHistory::class.java)
        bound = Bound(source, dao)
        return dao
    }

    companion object {
        private const val DRAIN_SECONDS = 5L
        private const val ABORT_SECONDS = 2L
    }

}

object EmptyEffectHistoryManager : EffectHistoryManager {
    override fun record(
        player: UUID,
        effectInstanceKey: String,
        action: EffectHistoryAction,
        durationMillis: Long,
        source: String?
    ): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun close() = Unit
}
