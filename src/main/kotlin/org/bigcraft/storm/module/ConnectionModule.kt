package org.bigcraft.storm.module

import com.google.inject.AbstractModule
import me.wyne.connection.api.ConnectionProvider
import org.bigcraft.storm.Storm
import org.bigcraft.storm.player.EffectHistoryManager
import org.bigcraft.storm.player.EmptyEffectHistoryManager
import org.bigcraft.storm.player.EmptyPlayerEffectStorage
import org.bigcraft.storm.player.PlayerEffectStorage
import org.bigcraft.storm.player.SqlEffectHistoryManager
import org.bigcraft.storm.player.SqlPlayerEffectStorage
import org.bukkit.Bukkit

object ConnectionModule : AbstractModule() {
    override fun configure() {
        try {
            Class.forName("me.wyne.connection.api.ConnectionProvider")
            bind(ConnectionProvider::class.java)
                .toInstance(Bukkit.getServicesManager().getRegistration(ConnectionProvider::class.java)!!.provider)
            bind(PlayerEffectStorage::class.java).to(SqlPlayerEffectStorage::class.java)
            bind(EffectHistoryManager::class.java).to(SqlEffectHistoryManager::class.java)
        } catch (_: ClassNotFoundException) {
            Storm.logger.warn("ConnectionSource not found, can't establish database connection")
            bind(PlayerEffectStorage::class.java).toInstance(EmptyPlayerEffectStorage)
            bind(EffectHistoryManager::class.java).to(EmptyEffectHistoryManager::class.java)
        }
    }
}