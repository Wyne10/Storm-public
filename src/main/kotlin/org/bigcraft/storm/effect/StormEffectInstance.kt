package org.bigcraft.storm.effect

import me.wyne.wutils.config.configurables.attribute.GenericFactory
import org.bukkit.configuration.ConfigurationSection

data class StormEffectInstance(
    val key: String,
    val effectKey: String,
    val configuration: ConfigurationSection
) {
    companion object Factory : GenericFactory<StormEffectInstance> {
        override fun create(key: String, config: ConfigurationSection): StormEffectInstance {
            val section = config.getConfigurationSection(key)!!
            val effectKey = section.getString("effect")!!
            return StormEffectInstance(key, effectKey, section)
        }
    }
}
