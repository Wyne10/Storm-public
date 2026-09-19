package me.wyne.storm.module

import com.google.inject.AbstractModule
import me.wyne.storm.StormApiImpl
import me.wyne.storm.effect.StormEffectManager
import me.wyne.storm.effect.StormEffectTicker
import me.wyne.storm.player.PlayerEffectManager

//region Implementations

private class CoreModule(private vararg val modules: Class<out Any>) : AbstractModule() {
    override fun configure() {
        modules.forEach { bind(it) }
    }
}

//endregion

val EffectModule: AbstractModule = CoreModule(StormEffectManager::class.java, PlayerEffectManager::class.java,
    StormEffectTicker::class.java)

val ApiModule: AbstractModule = CoreModule(StormApiImpl::class.java)