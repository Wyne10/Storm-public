package org.bigcraft.storm.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import me.wyne.wutils.common.command.CommandUtils
import me.wyne.wutils.common.duration.Durations
import org.bigcraft.storm.effect.StormEffectManager
import org.bigcraft.storm.player.PlayerEffectManager

class ApplyCommand(effectManager: StormEffectManager, playerManager: PlayerEffectManager) : SubCommand("apply") {
    override val command: CommandAPICommand = super.command
        .withPermission("effects.apply")
        .withArguments(effectInstanceKeyArgument(effectManager, "key"))
        .withArguments(CommandUtils.offlinePlayer("target"))
        .withArguments(durationArgument("duration"))
        .executes(CommandExecutor { sender, args ->
            val effectInstanceKey = args.getRaw("key") ?: ""
            val target = getOfflinePlayer(args, "target", sender)
            val duration = args.getRaw("duration") ?: "0"
            assertEffectInstanceKeyExists(effectManager, effectInstanceKey, sender)
            playerManager.setEffect(target, effectInstanceKey, Durations.getMillis(duration))
        })
}

class ClearCommand(effectManager: StormEffectManager, playerManager: PlayerEffectManager) : SubCommand("clear") {
    override val command: CommandAPICommand = super.command
        .withPermission("effects.clear")
        .withArguments(effectInstanceKeyArgument(effectManager, "key"))
        .withArguments(CommandUtils.offlinePlayer("target"))
        .executes(CommandExecutor { sender, args ->
            val effectInstanceKey = args.getRaw("key") ?: ""
            val target = getOfflinePlayer(args, "target", sender)
            assertEffectInstanceKeyExists(effectManager, effectInstanceKey, sender)
            playerManager.clearEffect(target, effectInstanceKey)
        })
}