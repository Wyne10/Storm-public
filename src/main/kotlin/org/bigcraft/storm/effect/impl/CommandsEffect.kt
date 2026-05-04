package org.bigcraft.storm.effect.impl

import me.wyne.wutils.i18n.kotlin.placeholderString
import org.bigcraft.storm.api.StormEffect
import org.bigcraft.storm.api.event.StormEffectApplyEvent
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.EventHandler

class CommandsEffect(config: ConfigurationSection) : StormEffect(config) {

    private val commands = config.getStringList("commands")

    @EventHandler(ignoreCancelled = true)
    private fun onEffectApply(event: StormEffectApplyEvent) {
        if (effectInstanceKey != event.effectInstance.key) return
        commands
            .map { event.player.placeholderString(it).get() }
            .forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
    }

}