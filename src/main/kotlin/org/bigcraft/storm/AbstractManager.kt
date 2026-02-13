package org.bigcraft.storm

import me.wyne.wutils.common.loadable.Loadable
import me.wyne.wutils.common.loadable.Loader
import me.wyne.wutils.config.configurables.attribute.GenericFactory
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

@Suppress("LeakingThis")
abstract class AbstractManager<V> : Loadable {

    protected abstract val sectionKey: String
    protected abstract val valueLoader: GenericFactory<V>

    protected val loadedMap = mutableMapOf<String, V>()

    val mapKeys
        get() = loadedMap.keys

    init {
        Loader.global.registerLoadable(this)
    }

    override fun load(config: ConfigurationSection) {
        loadedMap.clear()
        val section = config.getConfigurationSection(sectionKey) ?: return
        section.getKeys(false).forEach { key ->
            Storm.logger.debug("Loading key '{}' from '{}'", key, sectionKey)
            runCatching {
                loadedMap[key] = valueLoader.create(key, section)
            }.onFailure { Storm.logger.error("Failed loading '{}' from '{}'", key, sectionKey, it) }
        }
    }

    protected fun loadFiles(directory: File) {
        if (!directory.exists())
            directory.mkdirs()
        directory.listFiles()
            ?.forEach { file ->
                val key = file.nameWithoutExtension
                Storm.logger.debug("Loading key '{}' from '{}'", key, directory.name)
                runCatching {
                    loadedMap[key] = valueLoader.create(key, YamlConfiguration.loadConfiguration(file))
                }.onFailure { Storm.logger.error("Failed loading '{}' from '{}'", key, directory.name, it) }
            }
    }

}