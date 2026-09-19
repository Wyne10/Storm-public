package me.wyne.storm.module

import com.google.inject.AbstractModule
import me.wyne.storm.Storm
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin

class PluginModule(private val plugin: Storm) : AbstractModule() {
    override fun configure() {
        bind(Storm::class.java)
            .toInstance(plugin)
        bind(Plugin::class.java)
            .toInstance(plugin)
        bind(FileConfiguration::class.java)
            .toInstance(plugin.getConfig())
    }
}
