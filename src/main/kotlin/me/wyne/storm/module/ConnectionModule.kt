package me.wyne.storm.module

import com.google.inject.AbstractModule
import me.wyne.connection.api.ConnectionProvider
import me.wyne.storm.Storm
import me.wyne.storm.player.EffectHistoryManager
import me.wyne.storm.player.EmptyEffectHistoryManager
import me.wyne.storm.player.EmptyPlayerEffectStorage
import me.wyne.storm.player.PlayerEffectStorage
import me.wyne.storm.player.SqlEffectHistoryManager
import me.wyne.storm.player.SqlPlayerEffectStorage
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