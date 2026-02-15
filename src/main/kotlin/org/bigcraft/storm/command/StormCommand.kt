package org.bigcraft.storm.command

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import org.bigcraft.storm.Storm
import org.bigcraft.storm.effect.StormEffectManager
import org.bigcraft.storm.player.PlayerEffectManager

@Singleton
class StormCommand @Inject constructor(
    private val plugin: Storm,
    private val effectManager: StormEffectManager,
    private val playerManager: PlayerEffectManager
) {

    init {
        registerCommand()
    }

    private fun registerCommand() {
        CommandAPICommand("effects")
            .withSubcommand(ReloadCommand(plugin)())
            .withSubcommand(ApplyCommand(effectManager, playerManager)())
            .withSubcommand(ClearCommand(effectManager, playerManager)())
            .register(plugin)
    }

}
