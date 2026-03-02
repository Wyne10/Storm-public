package org.bigcraft.storm.effect

import me.wyne.wutils.config.configurables.attribute.GenericFactory
import org.bigcraft.storm.api.event.StormEffectInstance
import org.bukkit.configuration.ConfigurationSection

object StormEffectInstanceFactory : GenericFactory<StormEffectInstance> {
    override fun create(key: String, config: ConfigurationSection): StormEffectInstance {
        val section = config.getConfigurationSection(key)!!
        val effectKey = section.getString("effect")!!
        val name = section.getString("name") ?: key
        return StormEffectInstance(key, effectKey, name, section)
    }
}
