package me.wyne.storm.player

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.loadable.Loadable
import me.wyne.wutils.common.loadable.LoadableMeta
import me.wyne.wutils.common.loadable.Loader
import me.wyne.wutils.common.scheduler.Schedulers
import me.wyne.wutils.common.terminable.Terminable
import me.wyne.storm.Storm
import me.wyne.storm.api.EffectSource
import me.wyne.storm.api.StormEffectManager
import me.wyne.storm.api.event.StormEffectApplyEvent
import me.wyne.storm.api.event.StormEffectClearEvent
import me.wyne.storm.api.event.StormEffectJoinEvent
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Singleton
@LoadableMeta(priority = 1)
class PlayerEffectManager @Inject constructor(
    plugin: Storm,
    private val effectStorage: PlayerEffectStorage,
    private val historyManager: EffectHistoryManager
) : StormEffectManager, Listener, Terminable, Loadable {

    private val effectExpiration = ConcurrentHashMap<UUID, ConcurrentHashMap<String, ActiveEffect>>()
    private val hardDuration = ConcurrentHashMap.newKeySet<String>()

    init {
        Loader.global.registerLoadable(this)
        Bukkit.getPluginManager().registerEvents(this, plugin)
        plugin.bind(this)
    }

    override fun load(config: ConfigurationSection) {
        val hard = me.wyne.storm.effect.StormEffectManager.instance.mapKeys
            .mapNotNull { me.wyne.storm.effect.StormEffectManager.instance.getEffectInstance(it) }
            .filter { it.config.getBoolean("hard") }
            .map { it.key }
        hardDuration.retainAll(hard.toSet())
        hardDuration.addAll(hard)
    }

    override fun isAffected(player: Player?, effectInstanceKey: String): Boolean {
        if (player == null) return false
        val expireAt = expireAt(player.uniqueId, effectInstanceKey) ?: return false
        return System.currentTimeMillis() < expireAt
    }

    override fun getRemainingMillis(player: Player?, effectInstanceKey: String): Long {
        if (player == null) return 0
        val expireAt = expireAt(player.uniqueId, effectInstanceKey) ?: return 0
        return (expireAt - System.currentTimeMillis()).coerceAtLeast(0)
    }

    override fun setEffect(player: OfflinePlayer?, effectInstanceKey: String, durationMillis: Long, source: EffectSource) {
        applyEffect(player, effectInstanceKey, durationMillis, source)
    }

    fun applyEffect(player: OfflinePlayer?, effectInstanceKey: String, durationMillis: Long, source: EffectSource): Boolean {
        if (player == null) return false
        val effectInstance = me.wyne.storm.effect.StormEffectManager.instance.getEffectInstance(effectInstanceKey)
            ?: throw IllegalArgumentException("Unknown effect: $effectInstanceKey")
        if (!StormEffectApplyEvent(player, effectInstance, durationMillis, source).callEvent())
            return false
        val now = System.currentTimeMillis()
        // A hard effect counts down whether the player is online or not, so it never opens a session
        val sessionStart = if (player.isOnline && !hardDuration.contains(effectInstanceKey)) now else null
        effectStorage.setEffect(player.uniqueId, effectInstanceKey, durationMillis, sessionStart)
        historyManager.record(player.uniqueId, effectInstanceKey, EffectHistoryAction.APPLY, durationMillis, source.source)
        if (player.isOnline)
            put(player.uniqueId, effectInstanceKey, now + durationMillis, sessionStart)
        return true
    }

    override fun clearEffect(player: OfflinePlayer?, effectInstanceKey: String) {
        if (player == null) return
        val effectInstance = me.wyne.storm.effect.StormEffectManager.instance.getEffectInstance(effectInstanceKey)
            ?: return
        val remaining = expireAt(player.uniqueId, effectInstanceKey)
            ?.let { (it - System.currentTimeMillis()).coerceAtLeast(0) }
            ?: 0
        effectExpiration[player.uniqueId]?.remove(effectInstanceKey)
        effectStorage.clearEffect(player.uniqueId, effectInstanceKey)
        historyManager.record(player.uniqueId, effectInstanceKey, EffectHistoryAction.CLEAR, remaining, null)
        StormEffectClearEvent(player, effectInstance).callEvent()
    }

    @EventHandler(ignoreCancelled = true)
    private fun onJoin(event: PlayerJoinEvent) {
        val sessionStart = System.currentTimeMillis()
        effectStorage.beginSession(event.player.uniqueId, hardDuration.toSet(), sessionStart)
            .thenAcceptAsync({ playerEffects ->
                playerEffects.forEach {
                    val hard = hardDuration.contains(it.effectInstanceKey)
                    // Anchored on the session the database opened, so the time charged on quit is exactly
                    // the time that elapsed in memory
                    val expireAt = if (hard)
                        it.timestamp.time + it.totalMillis
                    else
                        sessionStart + it.remainingMillis
                    if (expireAt <= System.currentTimeMillis()) {
                        effectStorage.clearEffect(it.uuid, it.effectInstanceKey)
                        return@forEach
                    }
                    put(it.uuid, it.effectInstanceKey, expireAt, if (hard) null else sessionStart)
                    StormEffectJoinEvent(it.uuid, it.effectInstanceKey, expireAt).callEvent()
                }
            }, Schedulers.sync())
    }

    @EventHandler(ignoreCancelled = true)
    private fun onQuit(event: PlayerQuitEvent) {
        val playerEffects = effectExpiration.remove(event.player.uniqueId) ?: return
        endSessions(event.player.uniqueId, playerEffects, System.currentTimeMillis())
    }

    override fun close() {
        val time = System.currentTimeMillis()
        effectExpiration.keys.forEach { player ->
            val playerEffects = effectExpiration.remove(player) ?: return@forEach
            endSessions(player, playerEffects, time)
        }
    }

    // Closing the session lets the database charge the elapsed time itself. A hard effect never opened
    // one, so a null session start is what excludes it here
    private fun endSessions(player: UUID, playerEffects: Map<String, ActiveEffect>, time: Long) {
        playerEffects.forEach { (effectInstanceKey, active) ->
            val sessionStart = active.sessionStart ?: return@forEach
            effectStorage.endSession(player, effectInstanceKey, sessionStart, time)
        }
    }

    private fun expireAt(player: UUID, effectInstanceKey: String): Long? =
        effectExpiration[player]?.get(effectInstanceKey)?.expireAt

    private fun put(player: UUID, effectInstanceKey: String, expireAt: Long, sessionStart: Long?) {
        effectExpiration.computeIfAbsent(player) { ConcurrentHashMap() }[effectInstanceKey] =
            ActiveEffect(expireAt, sessionStart)
    }

    private data class ActiveEffect(val expireAt: Long, val sessionStart: Long?)

}
