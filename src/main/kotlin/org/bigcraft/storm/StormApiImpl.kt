package org.bigcraft.storm

import com.google.inject.Inject
import com.google.inject.Singleton
import org.bigcraft.storm.api.StormApi
import org.bigcraft.storm.api.StormEffectRegistry
import org.bigcraft.storm.effect.StormEffectManager
import org.bigcraft.storm.player.PlayerEffectManager
import org.bukkit.plugin.ServicePriority

@Singleton
class StormApiImpl @Inject constructor(plugin: Storm, effectManager: StormEffectManager, playerManager: PlayerEffectManager) {
    init {
        plugin.server.servicesManager.register(org.bigcraft.storm.api.StormEffectManager::class.java, playerManager, plugin, ServicePriority.Normal)
        plugin.server.servicesManager.register(StormEffectRegistry::class.java, effectManager, plugin, ServicePriority.Normal)
        StormApi.setPlugin(plugin)
        StormApi.setLogger(Storm.logger)
        StormApi.setEffectManager(playerManager)
        StormApi.setEffectRegistry(effectManager)
    }
}