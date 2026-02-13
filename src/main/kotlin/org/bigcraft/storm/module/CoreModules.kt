package org.bigcraft.storm.module

import com.google.inject.AbstractModule

//region Implementations

private class CoreModule(private vararg val modules: Class<out Any>) : AbstractModule() {
    override fun configure() {
        modules.forEach { bind(it) }
    }
}

//endregion

