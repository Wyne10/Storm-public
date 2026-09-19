package me.wyne.storm.command

import com.google.common.base.Supplier
import dev.jorel.commandapi.CommandAPIBukkit
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.StringTooltip
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.ListArgument
import dev.jorel.commandapi.arguments.ListArgumentBuilder
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import me.wyne.wutils.common.kotlin.command.tooltip
import me.wyne.wutils.common.kotlin.player.exists
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.kotlin.replace
import me.wyne.storm.Storm
import me.wyne.storm.effect.StormEffectManager
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender

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

fun effectInstanceKeyArgument(effectManager: StormEffectManager, nodeName: String): Argument<String> =
    StringArgument(nodeName)
        .replaceSuggestions(ArgumentSuggestions.stringCollection { effectManager.mapKeys })

fun effectInstanceKeyManyArgument(effectManager: StormEffectManager, nodeName: String): ListArgument<String> =
    ListArgumentBuilder<String>(nodeName)
        .withList(Supplier { effectManager.mapKeys })
        .withStringMapper()
        .buildGreedy()

fun assertEffectInstanceKeyExists(effectManager: StormEffectManager, effectInstanceKey: String, sender: CommandSender) {
    if (!effectManager.mapKeys.contains(effectInstanceKey))
        throw CommandAPIBukkit.failWithBaseComponents(
            *sender.placeholderComponent("error-effect-not-found", "key" replace effectInstanceKey).bungee()
        )
}

fun durationArgument(nodeName: String) =
    StringArgument(nodeName).tooltip(
        StringTooltip.ofString("<duration>", "Like 30m, 1h, 1m30s, etc.")
    )

fun CommandArguments.getOfflinePlayer(nodeName: String, sender: CommandSender): OfflinePlayer {
    val playerName = getRaw(nodeName) ?: ""
    val playerUuid = Bukkit.getPlayerUniqueId(playerName)
        ?: throw CommandAPIBukkit.failWithBaseComponents(
            *sender.placeholderComponent("error-player-not-found", "name" replace playerName).bungee()
        )
    val player = Bukkit.getOfflinePlayer(playerUuid)
    if (!player.exists)
        throw CommandAPIBukkit.failWithBaseComponents(
            *sender.placeholderComponent("error-player-not-found", "name" replace playerName).bungee()
        )
    return player
}