package me.wyne.storm.effect.impl

import me.wyne.storm.Storm
import me.wyne.storm.api.StormEffect
import me.wyne.storm.api.event.StormEffectApplyEvent
import me.wyne.storm.api.event.StormEffectClearEvent
import me.wyne.storm.api.event.StormEffectJoinEvent
import me.wyne.wutils.common.Ticks
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PotionEffect(config: ConfigurationSection) : StormEffect(config) {

    private val effects = config.getConfigurationSection("effects")?.getKeys(false)
        ?.associateWith { config.getInt("effects.$it") }
        ?.mapKeys { PotionEffectType.getByName(it.key)!! }
        ?: emptyMap()

    @EventHandler(ignoreCancelled = true)
    private fun onPotionEffect(event: EntityPotionEffectEvent) {
        val player = event.entity as? Player ?: return
        if (!isAffected(player)) return
        if (event.action != EntityPotionEffectEvent.Action.CLEARED
            && event.action != EntityPotionEffectEvent.Action.REMOVED) return
        if (event.cause == EntityPotionEffectEvent.Cause.PLUGIN) return
        applyEffects(player)
    }

    @EventHandler(ignoreCancelled = true)
    private fun onEffectApply(event: StormEffectApplyEvent) {
        if (effectInstanceKey != event.effectInstance.key) return
        event.player.player?.let { player ->
            applyEffects(player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun onEffectClear(event: StormEffectClearEvent) {
        if (effectInstanceKey != event.effectInstance.key) return
        event.player.player?.let { clearEffects(it) }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onJoin(event: StormEffectJoinEvent) {
        if (effectInstanceKey != event.effectInstanceKey) return
        val player = Bukkit.getPlayer(event.player) ?: return
        if (!isAffected(player)) return
        applyEffects(player)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private fun onQuit(event: PlayerQuitEvent) {
        if (!isAffected(event.player)) return
        clearEffects(event.player)
    }

    private fun applyEffects(player: Player) {
        Bukkit.getScheduler().runTask(Storm.instance, Runnable {
            effects
                .map { (effect, amplifier) ->
                    PotionEffect(
                        effect,
                        Ticks.ofMillis(getRemainingMillis(player)).toInt(),
                        amplifier,
                        true
                    )
                }
                .forEach { effect -> effect.apply(player) }
        })
    }

    private fun clearEffects(player: Player) {
        effects.keys
            .forEach { player.removePotionEffect(it) }
    }

}