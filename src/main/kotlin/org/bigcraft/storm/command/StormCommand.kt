package org.bigcraft.storm.command

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import org.bigcraft.storm.Storm

@Singleton
class StormCommand @Inject constructor(private val plugin: Storm) {

    init {
        registerCommand()
    }

    private fun registerCommand() {
        CommandAPICommand("effects")
            .withSubcommand(ReloadCommand(plugin)())
            .register(plugin)
    }

}
