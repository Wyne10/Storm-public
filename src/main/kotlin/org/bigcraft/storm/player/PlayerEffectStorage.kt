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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface PlayerEffectStorage : AutoCloseable {
    fun setEffect(player: UUID, effectInstanceKey: String, durationMillis: Long)
    fun updateRemaining(player: UUID, effectInstanceKey: String, durationMillis: Long)
    fun getEffects(player: UUID): CompletableFuture<List<PlayerEffect>>
    fun clearEffect(player: UUID, effectInstanceKey: String)
}

@Singleton
class SqlPlayerEffectStorage @Inject constructor(
    plugin: Storm,
    private val connectionProvider: ConnectionProvider
) : Loadable, PlayerEffectStorage {

    private var effectDao: Dao<PlayerEffect, Long>? = null

    private var executor = Executors.newFixedThreadPool(4)

    init {
        Loader.global.registerLoadable(this)
        plugin.bind(this)
    }

    override fun load(config: ConfigurationSection) {
        close()
        executor = Executors.newFixedThreadPool(4)
        effectDao = null
        if (connectionProvider.isActive) {
            effectDao = DaoManager.createDao(connectionProvider.connectionPool.source, PlayerEffect::class.java)
            if (!effectDao!!.isTableExists) {
                TableUtils.createTable(connectionProvider.connectionPool.source, PlayerEffect::class.java)
            }
            return
        }
        Storm.logger.error("Couldn't connect to effect database, subsequent requests will fail")
    }

    override fun setEffect(player: UUID, effectInstanceKey: String, durationMillis: Long) {
        executor.execute {
            val playerEffect = effectDao?.queryBuilder()?.where()
                ?.eq("uuid", player)
                ?.and()
                ?.eq(PlayerEffect.EFFECT_INSTANCE_FIELD, effectInstanceKey)
                ?.queryForFirst()
            if (playerEffect == null) {
                effectDao?.create(PlayerEffect(player, effectInstanceKey, durationMillis))
            } else {
                playerEffect.remainingMillis = durationMillis
                effectDao?.update(playerEffect)
            }
        }
    }

    override fun updateRemaining(player: UUID, effectInstanceKey: String, durationMillis: Long) {
        executor.execute {
            val playerEffect = effectDao?.queryBuilder()?.where()
                ?.eq("uuid", player)
                ?.and()
                ?.eq(PlayerEffect.EFFECT_INSTANCE_FIELD, effectInstanceKey)
                ?.queryForFirst() ?: return@execute
            if (durationMillis <= 0) {
                effectDao?.delete(playerEffect)
                return@execute
            }
            playerEffect.remainingMillis = durationMillis
            effectDao?.update(playerEffect)
        }
    }

    override fun getEffects(player: UUID): CompletableFuture<List<PlayerEffect>> =
        CompletableFuture.supplyAsync({
            requireNotNull(effectDao).queryBuilder().where()
                .eq("uuid", player)
                .query()
        }, executor).exceptionally { t ->
            Storm.logger.error("Player effects query error", t)
            return@exceptionally emptyList()
        }

    override fun clearEffect(player: UUID, effectInstanceKey: String) {
        executor.execute {
            val playerEffect = effectDao?.queryBuilder()?.where()
                ?.eq("uuid", player)
                ?.and()
                ?.eq(PlayerEffect.EFFECT_INSTANCE_FIELD, effectInstanceKey)
                ?.queryForFirst() ?: return@execute
            effectDao?.delete(playerEffect)
        }
    }

    override fun close() {
        executor.shutdown()
        if (!executor.awaitTermination(60, TimeUnit.SECONDS))
            executor.shutdownNow()
    }

}

object EmptyPlayerEffectStorage : PlayerEffectStorage {
    override fun setEffect(player: UUID, effectInstanceKey: String, durationMillis: Long) = Unit
    override fun updateRemaining(player: UUID, effectInstanceKey: String, durationMillis: Long) = Unit
    override fun getEffects(player: UUID): CompletableFuture<List<PlayerEffect>> = CompletableFuture.completedFuture(emptyList())
    override fun clearEffect(player: UUID, effectInstanceKey: String) = Unit
    override fun close() = Unit
}