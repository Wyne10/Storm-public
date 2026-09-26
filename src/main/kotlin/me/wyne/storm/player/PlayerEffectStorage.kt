package me.wyne.storm.player

import com.google.inject.Inject
import com.google.inject.Singleton
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.misc.TransactionManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import me.wyne.connection.api.ConnectionProvider
import me.wyne.wutils.common.loadable.Loadable
import me.wyne.wutils.common.loadable.Loader
import me.wyne.storm.Storm
import org.bukkit.configuration.ConfigurationSection
import java.util.Date
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface PlayerEffectStorage : AutoCloseable {
    fun setEffect(player: UUID, effectInstanceKey: String, totalMillis: Long, sessionStart: Long?): CompletableFuture<Void>
    fun beginSession(player: UUID, hardKeys: Set<String>, sessionStart: Long): CompletableFuture<List<PlayerEffect>>
    fun endSession(player: UUID, effectInstanceKey: String, sessionStart: Long, endedAt: Long): CompletableFuture<Void>
    fun clearEffect(player: UUID, effectInstanceKey: String): CompletableFuture<Void>
}

@Singleton
class SqlPlayerEffectStorage @Inject constructor(
    plugin: Storm,
    private val connectionProvider: ConnectionProvider,
    private val serverIdentity: ServerIdentity
) : Loadable, PlayerEffectStorage {

    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var bound: Bound? = null

    private class Bound(val source: ConnectionSource, val dao: Dao<PlayerEffect, Long>)

    init {
        Loader.global.registerLoadable(this)
        plugin.bind(this)
    }

    override fun load(config: ConfigurationSection) {
        if (!connectionProvider.isActive) {
            Storm.logger.error("Couldn't connect to effect database, subsequent requests will fail")
            return
        }
        dao(requireSource())
    }

    override fun setEffect(player: UUID, effectInstanceKey: String, totalMillis: Long, sessionStart: Long?): CompletableFuture<Void> =
        loggedWrite(executor, "apply effect '$effectInstanceKey' to $player") {
            inTransaction { dao ->
                val owner = sessionStart?.let { serverIdentity.name }
                val existing = query(dao, player, effectInstanceKey)
                if (existing == null)
                    dao.create(PlayerEffect(player, effectInstanceKey, totalMillis, sessionStart, owner))
                else
                    dao.update(existing.copy(totalMillis = totalMillis, consumedMillis = 0,
                        sessionStart = sessionStart, sessionOwner = owner, timestamp = Date()))
            }
        }

    override fun beginSession(player: UUID, hardKeys: Set<String>, sessionStart: Long): CompletableFuture<List<PlayerEffect>> =
        loggedRead(executor, "begin the effect session of $player", emptyList()) {
            inTransaction { dao ->
                val update = dao.updateBuilder()
                val where = update.where().eq(PlayerEffect.UUID_FIELD, player)
                if (hardKeys.isNotEmpty())
                    where.and().notIn(PlayerEffect.EFFECT_INSTANCE_FIELD, hardKeys)
                update.updateColumnExpression(PlayerEffect.CONSUMED_FIELD,
                    "${PlayerEffect.CONSUMED_FIELD} + COALESCE($sessionStart - ${PlayerEffect.SESSION_START_FIELD}, 0)")
                update.updateColumnValue(PlayerEffect.SESSION_START_FIELD, sessionStart)
                update.updateColumnValue(PlayerEffect.SESSION_OWNER_FIELD, serverIdentity.name)
                update.update()
                deleteExhausted(dao, player, null)
                dao.queryBuilder().where().eq(PlayerEffect.UUID_FIELD, player).query()
            }
        }

    override fun endSession(player: UUID, effectInstanceKey: String, sessionStart: Long, endedAt: Long): CompletableFuture<Void> =
        loggedWrite(executor, "end the session of effect '$effectInstanceKey' of $player") {
            inTransaction { dao ->
                val update = dao.updateBuilder()
                update.where()
                    .eq(PlayerEffect.UUID_FIELD, player)
                    .and().eq(PlayerEffect.EFFECT_INSTANCE_FIELD, effectInstanceKey)
                    .and().eq(PlayerEffect.SESSION_OWNER_FIELD, serverIdentity.name)
                    .and().eq(PlayerEffect.SESSION_START_FIELD, sessionStart)
                update.updateColumnExpression(PlayerEffect.CONSUMED_FIELD,
                    "${PlayerEffect.CONSUMED_FIELD} + ($endedAt - ${PlayerEffect.SESSION_START_FIELD})")
                update.updateColumnExpression(PlayerEffect.SESSION_START_FIELD, "NULL")
                update.updateColumnExpression(PlayerEffect.SESSION_OWNER_FIELD, "NULL")
                if (update.update() > 0)
                    deleteExhausted(dao, player, effectInstanceKey)
            }
        }

    override fun clearEffect(player: UUID, effectInstanceKey: String): CompletableFuture<Void> =
        loggedWrite(executor, "clear effect '$effectInstanceKey' of $player") {
            inTransaction { dao ->
                val playerEffect = query(dao, player, effectInstanceKey) ?: return@inTransaction
                dao.delete(playerEffect)
            }
        }

    override fun close() {
        executor.shutdown()
        if (!executor.awaitTermination(DRAIN_SECONDS, TimeUnit.SECONDS)) {
            executor.shutdownNow()
            if (!executor.awaitTermination(ABORT_SECONDS, TimeUnit.SECONDS))
                Storm.logger.error("Effect storage executor didn't terminate")
        }
    }

    private fun query(dao: Dao<PlayerEffect, Long>, player: UUID, effectInstanceKey: String): PlayerEffect? =
        dao.queryBuilder().where()
            .eq(PlayerEffect.UUID_FIELD, player)
            .and().eq(PlayerEffect.EFFECT_INSTANCE_FIELD, effectInstanceKey)
            .queryForFirst()

    private fun deleteExhausted(dao: Dao<PlayerEffect, Long>, player: UUID, effectInstanceKey: String?) {
        val delete = dao.deleteBuilder()
        val where = delete.where().eq(PlayerEffect.UUID_FIELD, player)
        if (effectInstanceKey != null)
            where.and().eq(PlayerEffect.EFFECT_INSTANCE_FIELD, effectInstanceKey)
        where.and().raw("${PlayerEffect.CONSUMED_FIELD} >= ${PlayerEffect.TOTAL_FIELD}")
        delete.delete()
    }

    private fun <T> inTransaction(action: (Dao<PlayerEffect, Long>) -> T): T {
        val source = requireSource()
        val dao = dao(source)
        return TransactionManager.callInTransaction(source) { action(dao) }
    }

    private fun requireSource(): ConnectionSource =
        checkNotNull(if (connectionProvider.isActive) connectionProvider.connectionPool?.source else null) { STORAGE_UNAVAILABLE }

    private fun dao(source: ConnectionSource): Dao<PlayerEffect, Long> {
        bound?.takeIf { it.source === source }?.let { return it.dao }
        val dao: Dao<PlayerEffect, Long> = DaoManager.createDao(source, PlayerEffect::class.java)
        if (!dao.isTableExists)
            TableUtils.createTable(source, PlayerEffect::class.java)
        bound = Bound(source, dao)
        return dao
    }

    companion object {
        private const val STORAGE_UNAVAILABLE = "Effect storage is unavailable"
        private const val DRAIN_SECONDS = 5L
        private const val ABORT_SECONDS = 2L
    }

}

object EmptyPlayerEffectStorage : PlayerEffectStorage {
    override fun setEffect(player: UUID, effectInstanceKey: String, totalMillis: Long, sessionStart: Long?): CompletableFuture<Void> = done()
    override fun beginSession(player: UUID, hardKeys: Set<String>, sessionStart: Long): CompletableFuture<List<PlayerEffect>> =
        CompletableFuture.completedFuture(emptyList())
    override fun endSession(player: UUID, effectInstanceKey: String, sessionStart: Long, endedAt: Long): CompletableFuture<Void> = done()
    override fun clearEffect(player: UUID, effectInstanceKey: String): CompletableFuture<Void> = done()
    override fun close() = Unit

    private fun done(): CompletableFuture<Void> = CompletableFuture.completedFuture(null)
}
