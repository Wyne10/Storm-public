package org.bigcraft.storm.effect.impl

import org.bigcraft.storm.api.StormEffect
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent

class CropsEffect(config: ConfigurationSection) : StormEffect(config) {

    private val multiplier = config.getDouble("multiplier", 1.0)

    @EventHandler(ignoreCancelled = true)
    private fun onBlockBreak(e: BlockBreakEvent) {
        if (!isAffected(e.player)) return
        if (!CROPS.contains(e.block.type)) return
        e.isDropItems = false
        e.block.getDrops(e.player.inventory.itemInMainHand, e.player).forEach { item ->
            item.amount = (item.amount * multiplier).toInt()
            e.block.location.world.dropItemNaturally(e.block.location, item)
        }
    }

    companion object {
        val CROPS: Set<Material> = setOf(
            Material.COCOA, Material.NETHER_WART, Material.MELON,
            Material.CARROTS, Material.WHEAT, Material.POTATOES, Material.BEETROOTS,
            Material.SWEET_BERRY_BUSH, Material.CHORUS_PLANT
        )
    }

}