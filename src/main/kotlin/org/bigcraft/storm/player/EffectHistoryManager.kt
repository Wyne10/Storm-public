package org.bigcraft.storm.player

import com.google.inject.Inject
import com.google.inject.Singleton
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils
import me.wyne.wutils.common.loadable.Loadable
import me.wyne.wutils.common.loadable.Loader
import org.bigcraft.connection.api.ConnectionProvider
import org.bigcraft.storm.Storm
import org.bukkit.configuration.ConfigurationSection
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface EffectHistoryManager : AutoCloseable {
    fun add(player: UUID, effectInstanceKey: String, durationMillis: Long, source: String)
}

@Singleton
class SqlEffectHistoryManager @Inject constructor(
    plugin: Storm,
    private val connectionProvider: ConnectionProvider
) : Loadable, EffectHistoryManager {

    private var historyDao: Dao<PlayerEffectHistory, Long>? = null

    private var executor = Executors.newSingleThreadExecutor()

    init {
        Loader.global.registerLoadable(this)
        plugin.bind(this)
    }

    override fun load(config: ConfigurationSection) {
        close()
        executor = Executors.newSingleThreadExecutor()
        historyDao = null
        if (connectionProvider.isActive) {
            historyDao = DaoManager.createDao(connectionProvider.connectionPool.source, PlayerEffectHistory::class.java)
            if (!historyDao!!.isTableExists) {
                TableUtils.createTable(connectionProvider.connectionPool.source, PlayerEffectHistory::class.java)
            }
            return
        }
        Storm.logger.error("Couldn't connect to effect history database, subsequent requests will fail")
    }

    override fun add(player: UUID, effectInstanceKey: String, durationMillis: Long, source: String) {
        executor.execute {
            historyDao?.create(PlayerEffectHistory(player, effectInstanceKey, durationMillis, source))
        }
    }

    override fun close() {
        executor.shutdown()
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow()
            if (!executor.awaitTermination(10, TimeUnit.SECONDS))
                Storm.logger.error("Effect history executor didn't terminate")
        }
    }

}

object EmptyEffectHistoryManager : EffectHistoryManager {
    override fun add(player: UUID, effectInstanceKey: String, durationMillis: Long, source: String) = Unit
    override fun close() = Unit
}