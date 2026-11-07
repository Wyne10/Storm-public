package me.wyne.storm.entity

import com.google.inject.Inject
import com.google.inject.Singleton
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.misc.TransactionManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import me.wyne.connection.api.ConnectionProvider
import me.wyne.storm.Storm
import me.wyne.storm.player.EffectHistoryAction
import me.wyne.storm.player.PlayerEffectStorage
import me.wyne.storm.player.ServerIdentity
import me.wyne.storm.player.StoredEffect
import me.wyne.wutils.common.loadable.Loadable
import me.wyne.wutils.common.loadable.LoadableMeta
import me.wyne.wutils.common.loadable.Loader
import me.wyne.wutils.common.terminable.Terminable
import org.bukkit.configuration.ConfigurationSection
import java.util.Date
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Singleton
@LoadableMeta(persist = false)
class PlayerEffectRepository @Inject constructor(
    plugin: Storm,
    private val connectionProvider: ConnectionProvider,
    private val serverIdentity: ServerIdentity
) : PlayerEffectStorage, Loadable, Terminable {

    private var effectDao: Dao<PlayerEffect, Long>? = null
    private var historyDao: Dao<PlayerEffectHistory, Long>? = null

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        Loader.global.registerLoadable(this)
        plugin.bind(this)
    }

    override fun load(config: ConfigurationSection) {
        if (!connectionProvider.isActive) {
            Storm.logger.error("Couldn't connect to effect database, subsequent requests will fail")
            return
        }
        val source = requireSource()
        effectDao = DaoManager.createDao(source, PlayerEffect::class.java)
        if (!effectDao!!.isTableExists)
            TableUtils.createTable(source, PlayerEffect::class.java)
        historyDao = DaoManager.createDao(source, PlayerEffectHistory::class.java)
        if (!historyDao!!.isTableExists)
            TableUtils.createTable(source, PlayerEffectHistory::class.java)
    }

    override fun setEffect(player: UUID, effectInstanceKey: String, totalMillis: Long, sessionStart: Long?): CompletableFuture<Unit> = supplyAsync {
        val effectDao = requireNotNull(effectDao)
        transaction {
            val owner = sessionStart?.let { serverIdentity.name }
            val existing = query(effectDao, player, effectInstanceKey)
            if (existing == null)
                effectDao.create(PlayerEffect(player, effectInstanceKey, totalMillis, sessionStart, owner))
            else
                effectDao.update(existing.copy(totalMillis = totalMillis, consumedMillis = 0,
                    sessionStart = sessionStart, sessionOwner = owner, timestamp = Date()))
        }
    }

    override fun beginSession(player: UUID, hardKeys: Set<String>, sessionStart: Long): CompletableFuture<List<StoredEffect>> = supplyAsync {
        val effectDao = requireNotNull(effectDao)
        transaction {
            effectDao.updateBuilder().apply {
                val where = where().eq(PlayerEffect.UUID_FIELD, player)
                if (hardKeys.isNotEmpty())
                    where.and().notIn(PlayerEffect.EFFECT_INSTANCE_FIELD, hardKeys)
                // A session still open here was left by a server that never closed it, charge it up to now
                updateColumnExpression(PlayerEffect.CONSUMED_FIELD,
                    "${PlayerEffect.CONSUMED_FIELD} + COALESCE($sessionStart - ${PlayerEffect.SESSION_START_FIELD}, 0)")
                updateColumnValue(PlayerEffect.SESSION_START_FIELD, sessionStart)
                updateColumnValue(PlayerEffect.SESSION_OWNER_FIELD, serverIdentity.name)
            }.update()
            deleteExhausted(effectDao, player, null)
            effectDao.queryForEq(PlayerEffect.UUID_FIELD, player).map { it.toStoredEffect() }
        }
    }

    override fun endSession(player: UUID, effectInstanceKey: String, sessionStart: Long, endedAt: Long): CompletableFuture<Unit> = supplyAsync {
        val effectDao = requireNotNull(effectDao)
        transaction {
            val updated = effectDao.updateBuilder().apply {
                where()
                    .eq(PlayerEffect.UUID_FIELD, player)
                    .and().eq(PlayerEffect.EFFECT_INSTANCE_FIELD, effectInstanceKey)
                    .and().eq(PlayerEffect.SESSION_OWNER_FIELD, serverIdentity.name)
                    .and().eq(PlayerEffect.SESSION_START_FIELD, sessionStart)
                updateColumnExpression(PlayerEffect.CONSUMED_FIELD,
                    "${PlayerEffect.CONSUMED_FIELD} + ($endedAt - ${PlayerEffect.SESSION_START_FIELD})")
                updateColumnExpression(PlayerEffect.SESSION_START_FIELD, "NULL")
                updateColumnExpression(PlayerEffect.SESSION_OWNER_FIELD, "NULL")
            }.update()
            if (updated > 0)
                deleteExhausted(effectDao, player, effectInstanceKey)
        }
    }

    override fun clearEffect(player: UUID, effectInstanceKey: String): CompletableFuture<Unit> = supplyAsync {
        val effectDao = requireNotNull(effectDao)
        transaction {
            effectDao.deleteBuilder().apply {
                where().eq(PlayerEffect.UUID_FIELD, player).and().eq(PlayerEffect.EFFECT_INSTANCE_FIELD, effectInstanceKey)
            }.delete()
        }
    }

    override fun record(player: UUID, effectInstanceKey: String, action: EffectHistoryAction, durationMillis: Long, source: String?): CompletableFuture<Unit> {
        val entry = PlayerEffectHistory(player, effectInstanceKey, action, durationMillis, source, serverIdentity.name)
        return supplyAsync {
            val historyDao = requireNotNull(historyDao)
            transaction { historyDao.create(entry) }
        }
    }

    private fun query(effectDao: Dao<PlayerEffect, Long>, player: UUID, effectInstanceKey: String): PlayerEffect? =
        effectDao.queryBuilder().where()
            .eq(PlayerEffect.UUID_FIELD, player)
            .and().eq(PlayerEffect.EFFECT_INSTANCE_FIELD, effectInstanceKey)
            .queryForFirst()

    private fun deleteExhausted(effectDao: Dao<PlayerEffect, Long>, player: UUID, effectInstanceKey: String?) {
        effectDao.deleteBuilder().apply {
            val where = where().eq(PlayerEffect.UUID_FIELD, player)
            if (effectInstanceKey != null)
                where.and().eq(PlayerEffect.EFFECT_INSTANCE_FIELD, effectInstanceKey)
            where.and().raw("${PlayerEffect.CONSUMED_FIELD} >= ${PlayerEffect.TOTAL_FIELD}")
        }.delete()
    }

    private fun <T> transaction(block: () -> T): T =
        TransactionManager.callInTransaction(requireSource(), block)

    private fun <T> supplyAsync(operation: () -> T): CompletableFuture<T> =
        CompletableFuture.supplyAsync({ operation() }, executor)
            .whenComplete { _, throwable ->
                if (throwable != null)
                    Storm.logger.error("Effect repository operation failed", throwable)
            }

    private fun requireSource(): ConnectionSource =
        if (connectionProvider.isActive) connectionProvider.connectionPool?.source
            ?: throw IllegalStateException("Effect database is not connected")
        else throw IllegalStateException("Effect database is not connected")

    override fun close() {
        executor.shutdown()
        if (executor.awaitTermination(DRAIN_SECONDS, TimeUnit.SECONDS)) return
        val dropped = executor.shutdownNow()
        Storm.logger.error(
            "Gave up after {}s waiting for the database, {} write(s) were not applied",
            DRAIN_SECONDS, dropped.size
        )
    }

    private companion object {
        const val DRAIN_SECONDS = 5L
    }

}
