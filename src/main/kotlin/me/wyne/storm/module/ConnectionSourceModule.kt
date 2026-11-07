package me.wyne.storm.module

import com.google.inject.AbstractModule
import me.wyne.connection.api.ConnectionProvider
import me.wyne.storm.Storm
import me.wyne.storm.entity.PlayerEffectRepository
import me.wyne.storm.player.LocalPlayerEffectStorage
import me.wyne.storm.player.PlayerEffectStorage
import org.bukkit.Bukkit

/** Installed by [ConnectionModule] only once ConnectionSource's classes are known to be present. */
internal object ConnectionSourceModule : AbstractModule() {
    override fun configure() {
        val provider = Bukkit.getServicesManager().getRegistration(ConnectionProvider::class.java)?.provider
        if (provider == null) {
            Storm.logger.warn("ConnectionSource didn't register its provider, effects are kept in memory only and reset on restart")
            bind(PlayerEffectStorage::class.java).to(LocalPlayerEffectStorage::class.java)
            return
        }
        bind(ConnectionProvider::class.java).toInstance(provider)
        bind(PlayerEffectStorage::class.java).to(PlayerEffectRepository::class.java)
    }
}
