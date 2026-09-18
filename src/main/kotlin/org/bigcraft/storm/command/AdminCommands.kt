package org.bigcraft.storm.command

import dev.jorel.commandapi.CommandAPIBukkit
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.DoubleArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import me.wyne.infpoints.api.IPApi
import me.wyne.wutils.common.command.CommandUtils
import me.wyne.wutils.common.duration.Durations
import me.wyne.wutils.common.kotlin.command.suggest
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.kotlin.replace
import org.bigcraft.storm.api.EffectSource
import org.bigcraft.storm.effect.StormEffectManager
import org.bigcraft.storm.player.PlayerEffectManager
import org.bukkit.entity.Player

class ApplyCommand(effectManager: StormEffectManager, playerManager: PlayerEffectManager) : SubCommand("apply") {
    override val command: CommandAPICommand = super.command
        .withPermission("effects.apply")
        .withArguments(CommandUtils.offlinePlayer("target"))
        .withArguments(effectInstanceKeyArgument(effectManager, "key"))
        .withArguments(durationArgument("duration"))
        .executes(CommandExecutor { sender, args ->
            val effectInstanceKey = args.getRaw("key") ?: ""
            val target = args.getOfflinePlayer("target", sender)
            val duration = args.getRaw("duration") ?: "0"
            assertEffectInstanceKeyExists(effectManager, effectInstanceKey, sender)
            playerManager.setEffect(target, effectInstanceKey, Durations.getMillis(duration), EffectSource.APPLY)
            sender.placeholderComponent("success-effect-apply", "key" replace effectInstanceKey).sendMessage(sender)
        })
}

class ClearCommand(effectManager: StormEffectManager, playerManager: PlayerEffectManager) : SubCommand("clear") {
    override val command: CommandAPICommand = super.command
        .withPermission("effects.clear")
        .withArguments(CommandUtils.offlinePlayer("target"))
        .withOptionalArguments(effectInstanceKeyArgument(effectManager, "key"))
        .executes(CommandExecutor { sender, args ->
            val effectInstanceKey = args.getRaw("key") ?: ""
            val target = args.getOfflinePlayer("target", sender)
            if (effectInstanceKey.isBlank()) {
                effectManager.mapKeys.forEach {
                    playerManager.clearEffect(target, it)
                }
                sender.placeholderComponent("success-effects-clear").sendMessage(sender)
            } else {
                assertEffectInstanceKeyExists(effectManager, effectInstanceKey, sender)
                playerManager.clearEffect(target, effectInstanceKey)
                sender.placeholderComponent("success-effect-clear", "key" replace effectInstanceKey).sendMessage(sender)
            }
        })
}

class PurchaseCommand(effectManager: StormEffectManager, playerManager: PlayerEffectManager, allowPurchaseOverride: Boolean) : SubCommand("purchase") {
    override val command: CommandAPICommand = super.command
        .withPermission("effects.purchase")
        .withArguments(CommandUtils.onlinePlayer("target"))
        .withArguments(effectInstanceKeyArgument(effectManager, "key"))
        .withArguments(durationArgument("duration"))
        .withArguments(StringArgument("currency").suggest("<currency>"))
        .withArguments(DoubleArgument("price").suggest("<price>"))
        .executes(CommandExecutor { sender, args ->
            val effectInstanceKey = args.getRaw("key") ?: ""
            val target = args.getByClass("target", Player::class.java)!!
            val duration = args.getRaw("duration") ?: "0"
            val currency = args.getRaw("currency") ?: ""
            val point = IPApi.getInstance().getPoint(currency) ?: throw CommandAPIBukkit.failWithBaseComponents(
                *sender.placeholderComponent("error-currency-not-found", "currency" replace currency).bungee()
            )
            val price = args.getByClass("price", Double::class.java)!!
            assertEffectInstanceKeyExists(effectManager, effectInstanceKey, sender)
            val isActive = playerManager.isAffected(target, effectInstanceKey)
            if (isActive && !allowPurchaseOverride) {
                target.placeholderComponent("error-effect-already-active", "key" replace effectInstanceKey).sendMessage(target)
                return@CommandExecutor
            }
            if (point.subtract(target.uniqueId, price)) {
                playerManager.setEffect(target, effectInstanceKey, Durations.getMillis(duration), EffectSource.PURCHASE)
                target.placeholderComponent("success-effect-purchase",
                    "key" replace effectInstanceKey,
                    "price" replace price
                ).sendMessage(target)
            } else {
                target.placeholderComponent("error-insufficient-funds",
                    "key" replace effectInstanceKey,
                    "price" replace price
                ).sendMessage(target)
            }
        })
}

class PurchaseManyCommand(effectManager: StormEffectManager, playerManager: PlayerEffectManager, allowPurchaseOverride: Boolean) : SubCommand("purchase-many") {
    override val command: CommandAPICommand = super.command
        .withPermission("effects.purchase")
        .withArguments(CommandUtils.onlinePlayer("target"))
        .withArguments(durationArgument("duration"))
        .withArguments(StringArgument("currency").suggest("<currency>"))
        .withArguments(DoubleArgument("price").suggest("<price>"))
        .withArguments(effectInstanceKeyManyArgument(effectManager, "key"))
        .executes(CommandExecutor { sender, args ->
            val effectInstanceKeys = args.get("key") as List<String>
            val target = args.getByClass("target", Player::class.java)!!
            val duration = args.getRaw("duration") ?: "0"
            val currency = args.getRaw("currency") ?: ""
            val point = IPApi.getInstance().getPoint(currency) ?: throw CommandAPIBukkit.failWithBaseComponents(
                *sender.placeholderComponent("error-currency-not-found", "currency" replace currency).bungee()
            )
            val price = args.getByClass("price", Double::class.java)!!
            effectInstanceKeys.forEach {
                assertEffectInstanceKeyExists(effectManager, it, sender)
            }
            val activeEffect = effectInstanceKeys.firstOrNull {
                playerManager.isAffected(target, it)
            }
            if (activeEffect != null && !allowPurchaseOverride) {
                target.placeholderComponent("error-effect-already-active", "key" replace activeEffect).sendMessage(target)
                return@CommandExecutor
            }
            if (point.subtract(target.uniqueId, price)) {
                effectInstanceKeys.forEach {
                    playerManager.setEffect(target, it, Durations.getMillis(duration), EffectSource.PURCHASE)
                }
                target.placeholderComponent("success-effects-purchase",
                    "price" replace price
                ).sendMessage(target)
            } else {
                target.placeholderComponent("error-insufficient-funds",
                    "price" replace price
                ).sendMessage(target)
            }
        })
}
