package org.bigcraft.storm.effect.impl

import me.wyne.wutils.common.Ticks
import org.bigcraft.storm.Storm
import org.bigcraft.storm.api.StormEffect
import org.bigcraft.storm.api.event.StormEffectApplyEvent
import org.bigcraft.storm.api.event.StormEffectClearEvent
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.potion.PotionEffectType

class PotionEffect(config: ConfigurationSection) : StormEffect(config) {

    private val effects = config.getConfigurationSection("effects")?.getKeys(false)
        ?.associateWith { config.getInt("effects.$it") }
        ?.mapKeys { PotionEffectType.getByName(it.key)!! }
        ?: emptyMap()

    @EventHandler(ignoreCancelled = true)
    private fun onPotionEffect(e: EntityPotionEffectEvent) {
        val player = e.entity as? Player ?: return
        if (!isAffected(player)) return
        if (e.action != EntityPotionEffectEvent.Action.CLEARED
            && e.action != EntityPotionEffectEvent.Action.REMOVED) return
        Bukkit.getScheduler().runTask(Storm.instance, Runnable {
            applyEffects(player)
        })
    }

    @EventHandler(ignoreCancelled = true)
    private fun onEffectApply(event: StormEffectApplyEvent) {
        if (effectInstanceKey != event.effectInstance.key) return
        event.player.player?.let { player ->
            Bukkit.getScheduler().runTask(Storm.instance, Runnable {
                applyEffects(player)
            })
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun onEffectClear(event: StormEffectClearEvent) {
        if (effectInstanceKey != event.effectInstance.key) return
        event.player.player?.let { player ->
            effects.keys
                .forEach { player.removePotionEffect(it) }
        }
    }

    private fun applyEffects(player: Player) {
        effects
            .map { (effect, amplifier) -> effect.createEffect(Ticks.ofMillis(getRemainingMillis(player)).toInt(), amplifier) }
            .forEach { effect -> effect.apply(player) }
    }

}