package org.bigcraft.storm.effect

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.event.EventRegistry
import me.wyne.wutils.common.loadable.LoadableMeta
import me.wyne.wutils.common.terminable.Terminable
import org.bigcraft.storm.AbstractManager
import org.bigcraft.storm.Storm
import org.bigcraft.storm.api.StormApi
import org.bigcraft.storm.api.StormEffect
import org.bigcraft.storm.api.StormEffectRegistry
import org.bigcraft.storm.api.TickableStormEffect
import org.bigcraft.storm.api.event.StormEffectInstance
import org.bigcraft.storm.effect.impl.CommandsEffect
import org.bigcraft.storm.effect.impl.CropsEffect
import org.bigcraft.storm.effect.impl.EmptyEffect
import org.bigcraft.storm.effect.impl.HunterEffect
import org.bukkit.configuration.ConfigurationSection
import java.io.File

@Singleton
@LoadableMeta(priority = 0)
class StormEffectManager @Inject constructor(plugin: Storm, private val ticker: StormEffectTicker) : AbstractManager<StormEffectInstance>(),
    StormEffectRegistry, Terminable {

    override val sectionKey = "effect"
    override val valueLoader = StormEffectInstanceFactory
    private val effectDirectory = File(plugin.dataFolder, "effect")

    private val registeredEffects: MutableMap<String, Class<out StormEffect>> = mutableMapOf()
    private val effects: MutableMap<String, StormEffect> = mutableMapOf()

    private val eventRegistry = EventRegistry(plugin)

    init {
        instance = this
        plugin.bind(this)
    }

    override fun isRegistered(effectKey: String): Boolean =
        registeredEffects.containsKey(effectKey)

    override fun isRegistered(effect: Class<out StormEffect>): Boolean =
        registeredEffects.containsValue(effect)

    override fun getEffectInstance(effectInstanceKey: String): StormEffectInstance? =
        loadedMap[effectInstanceKey]

    override fun register(effect: Class<out StormEffect>, effectKey: String) {
        registeredEffects[effectKey] = effect
        Storm.logger.debug("Registered effect '{}'", effectKey)
        loadedMap.values
            .filter { it.effectKey == effectKey }
            .forEach {
                runCatching {
                    Storm.logger.debug("Loading effect instance '{}'", it.key)
                    val newEffect = effect
                        .getConstructor(ConfigurationSection::class.java).newInstance(it.config)
                    effects[it.key] = newEffect
                    if (newEffect is TickableStormEffect) {
                        ticker.registerEffect(effectKey, newEffect.periodTicks)
                        ticker.registerEffectInstance(it, newEffect)
                    } else {
                        eventRegistry.register(newEffect)
                    }
                }.onFailure { t -> Storm.logger.error("An exception occurred trying to load effect instance '{}'", it.key, t) }
            }
    }

    override fun load(config: ConfigurationSection) {
        if (loadedMap.isEmpty()) {
            super.load(config)
            loadFiles(effectDirectory)
            return
        } else {
            super.load(config)
            loadFiles(effectDirectory)
        }
        eventRegistry.clear()
        effects.clear()
        ticker.clear()
        loadedMap.values
            .forEach {
                Storm.logger.debug("Loading effect instance '{}'", it.key)
                val effect = registeredEffects[it.effectKey]
                if (effect == null)
                    Storm.logger.error("No effect with a key '{}' is registered", it.effectKey).also { return@forEach }
                runCatching {
                    val newEffect = effect
                        .getConstructor(ConfigurationSection::class.java).newInstance(it.config)
                    effects[it.key] = newEffect
                    if (newEffect is TickableStormEffect) {
                        ticker.registerEffect(it.effectKey, newEffect.periodTicks)
                        ticker.registerEffectInstance(it, newEffect)
                    } else {
                        eventRegistry.register(newEffect)
                    }
                }.onFailure { t -> Storm.logger.error("An exception occurred trying to load effect instance '{}'", it.key, t) }
            }
    }

    override fun close() {
        eventRegistry.closeAndReportException()
    }

    companion object {
        lateinit var instance: StormEffectManager
            private set

        fun registerImplementations() {
            StormApi.getEffectRegistry().register(EmptyEffect::class.java, "empty")
            StormApi.getEffectRegistry().register(HunterEffect::class.java, "hunter")
            StormApi.getEffectRegistry().register(CropsEffect::class.java, "crops")
            StormApi.getEffectRegistry().register(CommandsEffect::class.java, "commands")
        }
    }

}