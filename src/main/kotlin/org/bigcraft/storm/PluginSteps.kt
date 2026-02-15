package org.bigcraft.storm

import com.google.inject.CreationException
import com.google.inject.Guice
import com.google.inject.Stage
import me.wyne.wutils.common.loadable.Loader
import me.wyne.wutils.common.plugin.CompositeStep
import me.wyne.wutils.common.plugin.LevelWrapper
import me.wyne.wutils.common.plugin.LoggerWrapper
import me.wyne.wutils.common.plugin.PluginStep
import me.wyne.wutils.common.plugin.Step
import me.wyne.wutils.common.plugin.StepScope
import me.wyne.wutils.config.Config
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.PluginI18nBuilder
import me.wyne.wutils.i18n.language.component.BukkitComponentAudiences
import me.wyne.wutils.i18n.language.interpretation.ComponentInterpreters
import me.wyne.wutils.i18n.language.validation.EmptyValidator
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bigcraft.storm.Storm.Companion.EMPTY_CONFIGURATION
import org.bigcraft.storm.Storm.Companion.logger
import org.bigcraft.storm.effect.StormEffectManager
import org.bigcraft.storm.module.ApiModule
import org.bigcraft.storm.module.CommandModule
import org.bigcraft.storm.module.ConnectionModule
import org.bigcraft.storm.module.EffectModule
import org.bigcraft.storm.module.PlaceholderModule
import org.bigcraft.storm.module.PluginModule

@Step(priority = 0, scope = StepScope.ENABLE)
object LoadDefaultConfig : PluginStep<Storm> {
    override fun run(plugin: Storm) {
        plugin.saveDefaultConfig()
        plugin.config.setDefaults(EMPTY_CONFIGURATION)
    }
}

@Step(priority = 1, scope = StepScope.ENABLE)
object InitializeLogger : PluginStep<Storm> {
    override fun run(plugin: Storm) {
        logger = LoggerWrapper(plugin.slF4JLogger, LevelWrapper.valueOf(plugin.config.getString("logLevel", "INFO")!!))
    }
}

@Step(priority = 3, scope = StepScope.ENABLE)
object InitializeI18n : PluginStep<Storm> {
    override fun run(plugin: Storm) {
        I18n.global = PluginI18nBuilder(plugin)
            .setLogger(logger)
            .setComponentAudience(BukkitComponentAudiences(BukkitAudiences.create(plugin)))
            .setComponentInterpreter(
                ComponentInterpreters.valueOf(
                    plugin.config.getString("serializer", "MINI_MESSAGE")!!
                ).get(EmptyValidator())
            )
            .setUsePlayerLanguage(plugin.config.getBoolean("usePlayerLanguage", true))
            .loadLanguage("lang/ru.yml")
            .build()
    }
}

@Step(priority = 4, scope = StepScope.ENABLE)
object InitializeInjector : PluginStep<Storm> {
    override fun run(plugin: Storm) {
        try {
            Storm.instance.injector = Guice.createInjector(
                Stage.PRODUCTION,
                PluginModule(plugin),
                EffectModule,
                ApiModule,
                ConnectionModule,
                PlaceholderModule,
                CommandModule
            )
        } catch (e: CreationException) {
            logger.error("Guice injector creation exception", e)
        }
    }
}

@Step(priority = 5, scope = StepScope.ENABLE)
object InitializeConfig : CompositeStep<Storm>(ReloadConfig) {
    override fun before(plugin: Storm) {
        Config.global.apply {
            logger = logger
            setConfigGenerator(plugin, "config.yml")
            generateConfig()
        }
    }
}

@Step(priority = 6, scope = StepScope.ENABLE)
object InitializeLoader : PluginStep<Storm> {
    override fun run(plugin: Storm) {
        Loader.global.registerConfig(Loader.DEFAULT_PATH, plugin.config)
    }
}

@Step(priority = 7, scope = StepScope.ENABLE)
object Load : PluginStep<Storm> {
    override fun run(plugin: Storm) {
        Loader.global.load(plugin)
    }
}

@Step(priority = 8, scope = StepScope.ENABLE)
object RegisterEffects : PluginStep<Storm> {
    override fun run(plugin: Storm) {
        StormEffectManager.registerImplementations()
    }
}

@Step(scope = StepScope.RELOAD)
object Reload : CompositeStep<Storm>(ReloadConfig, InitializeLogger, InitializeLoader, InitializeI18n, Load)

object ReloadConfig : PluginStep<Storm> {
    override fun run(plugin: Storm) {
        plugin.reloadConfig()
        plugin.config.setDefaults(EMPTY_CONFIGURATION)
        Config.global.reloadConfig(plugin.config)
    }
}