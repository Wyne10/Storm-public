package org.bigcraft.storm.effect.impl

import org.bigcraft.storm.api.StormEffect
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import kotlin.math.ceil

class HunterEffect(config: ConfigurationSection) : StormEffect(config) {

    private val multiplier = config.getDouble("multiplier", 1.0)

    @EventHandler(ignoreCancelled = true)
    private fun onEntityDeath(e: EntityDeathEvent) {
        if (e.entity is Player) return
        if (e.entity.killer == null) return
        val player = e.entity.killer!!
        if (!isAffected(player)) return
        e.droppedExp = ceil(e.droppedExp * multiplier).toInt()
    }

}