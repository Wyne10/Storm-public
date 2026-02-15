package org.bigcraft.storm.module

import com.google.inject.AbstractModule
import org.bigcraft.connection.api.ConnectionProvider
import org.bigcraft.storm.Storm
import org.bigcraft.storm.player.EmptyPlayerEffectStorage
import org.bigcraft.storm.player.PlayerEffectStorage
import org.bigcraft.storm.player.SqlPlayerEffectStorage
import org.bukkit.Bukkit

object ConnectionModule : AbstractModule() {
    override fun configure() {
        try {
            Class.forName("org.bigcraft.connection.api.ConnectionProvider")
            bind(ConnectionProvider::class.java)
                .toInstance(Bukkit.getServicesManager().getRegistration(ConnectionProvider::class.java)!!.provider)
            bind(PlayerEffectStorage::class.java).to(SqlPlayerEffectStorage::class.java)
        } catch (_: ClassNotFoundException) {
            Storm.logger.warn("ConnectionSource not found, can't establish database connection")
            bind(PlayerEffectStorage::class.java).toInstance(EmptyPlayerEffectStorage)
        }
    }
}