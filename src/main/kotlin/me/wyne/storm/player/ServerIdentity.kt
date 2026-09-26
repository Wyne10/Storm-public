package me.wyne.storm.player

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.config.Config
import me.wyne.wutils.config.ConfigEntry

@Suppress("FieldMayBeFinal")
@Singleton
class ServerIdentity @Inject constructor() {

    @ConfigEntry(
        section = "Storage",
        comment = "Name of this server, used to tell this server's effect sessions from another's and stored with every history entry"
    )
    private var serverName = "server"

    val name: String
        get() = serverName

    init {
        Config.global.registerConfigObject(this)
    }

}
