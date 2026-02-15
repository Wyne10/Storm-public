package org.bigcraft.storm.module

import com.google.inject.AbstractModule
import org.bigcraft.storm.StormApiImpl
import org.bigcraft.storm.effect.StormEffectManager
import org.bigcraft.storm.effect.impl.HunterEffect
import org.bigcraft.storm.player.PlayerEffectManager

//region Implementations

private class CoreModule(private vararg val modules: Class<out Any>) : AbstractModule() {
    override fun configure() {
        modules.forEach { bind(it) }
    }
}

//endregion

val EffectModule: AbstractModule = CoreModule(StormEffectManager::class.java, PlayerEffectManager::class.java)

val ApiModule: AbstractModule = CoreModule(StormApiImpl::class.java)