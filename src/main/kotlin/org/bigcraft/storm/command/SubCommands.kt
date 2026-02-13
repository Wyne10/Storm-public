package org.bigcraft.storm.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import org.bigcraft.storm.Storm

abstract class SubCommand(argument: String) {
    open val command = CommandAPICommand(argument)
    operator fun invoke() = command
}

class ReloadCommand(plugin: Storm) : SubCommand("reload") {
    override val command: CommandAPICommand = super.command
        .withPermission("effects.reload")
        .executes(CommandExecutor { sender, _ ->
            plugin.reload()
            sender.placeholderComponent("success-plugin-reload").sendMessage(sender)
            Storm.logger.info("Plugin reloaded")
        })
}