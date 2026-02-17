package org.bigcraft.storm.player

import com.google.common.collect.HashBasedTable
import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.terminable.Terminable
import org.bigcraft.storm.Storm
import org.bigcraft.storm.api.EffectSource
import org.bigcraft.storm.api.StormEffectManager
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

@Singleton
class PlayerEffectManager @Inject constructor(
    private val plugin: Storm,
    private val effectStorage: PlayerEffectStorage,
    private val historyManager: EffectHistoryManager
) : StormEffectManager, Listener, Terminable {

    private val effectExpirationTable = HashBasedTable.create<UUID, String, Long>()

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        plugin.bind(this)
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
        effectStorage.setEffect(player.uniqueId, effectInstanceKey, durationMillis)
        historyManager.add(player.uniqueId, effectInstanceKey, durationMillis, source.source)
        if (player.isOnline)
            effectExpirationTable.put(player.uniqueId, effectInstanceKey, System.currentTimeMillis() + durationMillis)
    }

    override fun clearEffect(player: OfflinePlayer?, effectInstanceKey: String) {
        if (player == null) return
        effectExpirationTable.remove(player.uniqueId, effectInstanceKey) ?: return
        effectStorage.clearEffect(player.uniqueId, effectInstanceKey)
    }

    @EventHandler(ignoreCancelled = true)
    private fun onJoin(event: PlayerJoinEvent) {
        effectStorage.getEffects(event.player.uniqueId)
            .thenAcceptAsync({ playerEffects ->
                playerEffects.forEach {
                    effectExpirationTable.put(it.uuid, it.effectInstanceKey, System.currentTimeMillis() + it.remainingMillis)
                }
            }, Bukkit.getScheduler().getMainThreadExecutor(plugin))
    }

    @EventHandler(ignoreCancelled = true)
    private fun onQuit(event: PlayerQuitEvent) {
        val playerEffects = effectExpirationTable.row(event.player.uniqueId)
        playerEffects.forEach { (effectInstanceKey, expireAtMillis) ->
            effectStorage.updateRemaining(event.player.uniqueId, effectInstanceKey, expireAtMillis - System.currentTimeMillis())
        }
        playerEffects.clear()
    }

    override fun close() {
        effectExpirationTable.cellSet()
            .forEach {
                val time = System.currentTimeMillis()
                effectStorage.updateRemaining(it.rowKey, it.columnKey, it.value - time)
            }
    }

}