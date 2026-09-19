package me.wyne.storm

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.storm.api.StormApi
import me.wyne.storm.api.StormEffectRegistry
import me.wyne.storm.effect.StormEffectManager
import me.wyne.storm.player.PlayerEffectManager
import org.bukkit.plugin.ServicePriority

@Singleton
class StormApiImpl @Inject constructor(plugin: Storm, effectManager: StormEffectManager, playerManager: PlayerEffectManager) {
    init {
        plugin.server.servicesManager.register(me.wyne.storm.api.StormEffectManager::class.java, playerManager, plugin, ServicePriority.Normal)
        plugin.server.servicesManager.register(StormEffectRegistry::class.java, effectManager, plugin, ServicePriority.Normal)
        StormApi.setPlugin(plugin)
        StormApi.setLogger(Storm.logger)
        StormApi.setEffectManager(playerManager)
        StormApi.setEffectRegistry(effectManager)
    }
}