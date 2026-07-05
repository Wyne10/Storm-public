package org.bigcraft.storm.player

import com.google.common.collect.HashBasedTable
import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.loadable.Loadable
import me.wyne.wutils.common.loadable.LoadableMeta
import me.wyne.wutils.common.loadable.Loader
import me.wyne.wutils.common.scheduler.Schedulers
import me.wyne.wutils.common.terminable.Terminable
import org.bigcraft.storm.Storm
import org.bigcraft.storm.api.EffectSource
import org.bigcraft.storm.api.StormEffectManager
import org.bigcraft.storm.api.event.StormEffectApplyEvent
import org.bigcraft.storm.api.event.StormEffectClearEvent
import org.bigcraft.storm.api.event.StormEffectJoinEvent
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

@Singleton
@LoadableMeta(priority = 1)
class PlayerEffectManager @Inject constructor(
    plugin: Storm,
    private val effectStorage: PlayerEffectStorage,
    private val historyManager: EffectHistoryManager
) : StormEffectManager, Listener, Terminable, Loadable {

    private val effectExpirationTable = HashBasedTable.create<UUID, String, Long>()
    private val hardDuration = mutableSetOf<String>()

    init {
        Loader.global.registerLoadable(this)
        Bukkit.getPluginManager().registerEvents(this, plugin)
        plugin.bind(this)
    }

    override fun load(config: ConfigurationSection) {
        hardDuration.clear()
        org.bigcraft.storm.effect.StormEffectManager.instance.mapKeys
            .mapNotNull { org.bigcraft.storm.effect.StormEffectManager.instance.getEffectInstance(it) }
            .filter { it.config.getBoolean("hard") }
            .forEach { hardDuration.add(it.key) }
    }

    override fun isAffected(player: Player?, effectInstanceKey: String): Boolean {
        if (player == null) return false
        val expireAt = effectExpirationTable.get(player.uniqueId, effectInstanceKey) ?: return false
        return System.currentTimeMillis() < expireAt
    }

    override fun getRemainingMillis(player: Player?, effectInstanceKey: String): Long {
        if (player == null) return 0
        val expireAt = effectExpirationTable.get(player.uniqueId, effectInstanceKey) ?: return 0
        return (expireAt - System.currentTimeMillis()).coerceAtLeast(0)
    }

    override fun setEffect(player: OfflinePlayer?, effectInstanceKey: String, durationMillis: Long, source: EffectSource) {
        if (player == null) return
        val effectInstance = org.bigcraft.storm.effect.StormEffectManager.instance.getEffectInstance(effectInstanceKey)
            ?: throw IllegalArgumentException("Unknown effect: $effectInstanceKey")
        if (!StormEffectApplyEvent(player, effectInstance, source).callEvent())
            return
        effectStorage.setEffect(player.uniqueId, effectInstanceKey, durationMillis)
        historyManager.add(player.uniqueId, effectInstanceKey, durationMillis, source.source)
        if (player.isOnline)
            effectExpirationTable.put(player.uniqueId, effectInstanceKey, System.currentTimeMillis() + durationMillis)
    }

    override fun clearEffect(player: OfflinePlayer?, effectInstanceKey: String) {
        if (player == null) return
        val effectInstance = org.bigcraft.storm.effect.StormEffectManager.instance.getEffectInstance(effectInstanceKey)
            ?: return
        effectExpirationTable.remove(player.uniqueId, effectInstanceKey)
        effectStorage.clearEffect(player.uniqueId, effectInstanceKey)
        StormEffectClearEvent(player, effectInstance).callEvent()
    }

    @EventHandler(ignoreCancelled = true)
    private fun onJoin(event: PlayerJoinEvent) {
        effectStorage.getEffects(event.player.uniqueId)
            .thenAcceptAsync({ playerEffects ->
                playerEffects.forEach {
                    val hard = hardDuration.contains(it.effectInstanceKey)
                    val expireAt = if (hard)
                        it.timestamp.time + it.remainingMillis
                    else
                        System.currentTimeMillis() + it.remainingMillis
                    if (hard && expireAt <= System.currentTimeMillis()) {
                        effectStorage.clearEffect(it.uuid, it.effectInstanceKey)
                        return@forEach
                    }
                    effectExpirationTable.put(it.uuid, it.effectInstanceKey, expireAt)
                    StormEffectJoinEvent(it.uuid, it.effectInstanceKey, expireAt).callEvent()
                }
            }, Schedulers.sync())
    }

    @EventHandler(ignoreCancelled = true)
    private fun onQuit(event: PlayerQuitEvent) {
        val playerEffects = effectExpirationTable.row(event.player.uniqueId)
        playerEffects.forEach { (effectInstanceKey, expireAtMillis) ->
            if (hardDuration.contains(effectInstanceKey)) return@forEach
            effectStorage.updateRemaining(event.player.uniqueId, effectInstanceKey, expireAtMillis - System.currentTimeMillis())
        }
        playerEffects.clear()
    }

    override fun close() {
        val time = System.currentTimeMillis()
        effectExpirationTable.cellSet()
            .forEach {
                if (hardDuration.contains(it.columnKey)) return@forEach
                effectStorage.updateRemaining(it.rowKey, it.columnKey, it.value - time)
            }
    }

}