package org.bigcraft.storm.effect

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.event.EventRegistry
import me.wyne.wutils.common.loadable.LoadableMeta
import me.wyne.wutils.common.terminable.Terminable
import org.bigcraft.storm.AbstractManager
import org.bigcraft.storm.Storm
import org.bigcraft.storm.api.StormEffect
import org.bigcraft.storm.api.StormEffectRegistry
import org.bukkit.configuration.ConfigurationSection
import java.io.File

@Singleton
@LoadableMeta(priority = 0)
class StormEffectManager @Inject constructor(plugin: Storm) : AbstractManager<StormEffectInstance>(),
    StormEffectRegistry, Terminable {

    override val sectionKey = "effect"
    override val valueLoader = StormEffectInstance.Factory
    private val effectDirectory = File(plugin.dataFolder, "effect")

    private val registeredEffects: MutableMap<String, Class<StormEffect>> = mutableMapOf()
    private val effects: MutableMap<String, StormEffect> = mutableMapOf()

    private val eventRegistry = EventRegistry(plugin)

    init {
        plugin.bind(this)
    }

    override fun register(effect: StormEffect) {
        registeredEffects[effect.key] = effect.javaClass
        loadedMap.values
            .filter { it.effectKey == effect.key }
            .forEach {
                runCatching {
                    val newEffect = effect.javaClass
                        .getConstructor(ConfigurationSection::class.java).newInstance(it.configuration)
                    effects[it.key] = newEffect
                    eventRegistry.register(newEffect)
                }.onFailure { t -> Storm.logger.error("An exception occurred trying to load effect instance {}", it.key, t) }
            }
    }

    override fun load(config: ConfigurationSection) {
        eventRegistry.clear()
        effects.clear()
        super.load(config)
        loadFiles(effectDirectory)
        loadedMap.values
            .forEach {
                Storm.logger.debug("Loading effect instance {}", it.key)
                val effect = registeredEffects[it.effectKey]
                if (effect == null)
                    Storm.logger.error("No effect with a key {} is registered", it.effectKey).also { return@forEach }
                runCatching {
                    val newEffect = effect
                        .getConstructor(ConfigurationSection::class.java).newInstance(it.configuration)
                    effects[it.key] = newEffect
                    eventRegistry.register(newEffect)
                }.onFailure { t -> Storm.logger.error("An exception occurred trying to load effect instance {}", it.key, t) }
            }
    }

    override fun close() {
        eventRegistry.closeAndReportException()
    }

}