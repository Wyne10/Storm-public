package org.bigcraft.storm.effect

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.event.EventRegistry
import me.wyne.wutils.common.loadable.Loadable
import me.wyne.wutils.common.loadable.LoadableMeta
import me.wyne.wutils.common.terminable.Terminable
import org.bigcraft.storm.Storm
import org.bigcraft.storm.api.StormEffect
import org.bigcraft.storm.api.StormEffectRegistry
import org.bukkit.configuration.ConfigurationSection

@Singleton
@LoadableMeta(priority = 0)
class StormEffectManager @Inject constructor(plugin: Storm) : StormEffectRegistry, Loadable, Terminable {

    private val registeredEffects: MutableMap<String, StormEffect> = mutableMapOf()

    private val eventRegistry = EventRegistry(plugin)

    override fun register(effect: StormEffect) {
        registeredEffects[effect.key] = effect
        eventRegistry.register(effect)
    }

    override fun load(config: ConfigurationSection) {
        eventRegistry.clear()
        registeredEffects.values.forEach { eventRegistry.register(it) }
    }

    override fun close() {
        eventRegistry.closeAndReportException()
    }

}