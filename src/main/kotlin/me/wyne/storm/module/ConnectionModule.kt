package me.wyne.storm.module

import com.google.inject.AbstractModule
import me.wyne.storm.Storm
import me.wyne.storm.player.LocalPlayerEffectStorage
import me.wyne.storm.player.PlayerEffectStorage

object ConnectionModule : AbstractModule() {
    override fun configure() {
        if (isConnectionSourcePresent()) {
            install(ConnectionSourceModule)
            return
        }
        Storm.logger.warn("ConnectionSource not found, effects are kept in memory only and reset on restart")
        bind(PlayerEffectStorage::class.java).to(LocalPlayerEffectStorage::class.java)
    }

    private fun isConnectionSourcePresent() = try {
        Class.forName("me.wyne.connection.api.ConnectionProvider")
        Class.forName("com.j256.ormlite.support.ConnectionSource")
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}
