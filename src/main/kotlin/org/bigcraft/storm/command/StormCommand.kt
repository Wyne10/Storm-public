package org.bigcraft.storm.command

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import me.wyne.wutils.config.Config
import me.wyne.wutils.config.ConfigEntry
import org.bigcraft.storm.Storm
import org.bigcraft.storm.effect.StormEffectManager
import org.bigcraft.storm.player.PlayerEffectManager

@Singleton
class StormCommand @Inject constructor(
    private val plugin: Storm,
    private val effectManager: StormEffectManager,
    private val playerManager: PlayerEffectManager
) {

    @ConfigEntry(section = "Commands", comment = "Если true разрешает приобретать эффекты даже когда они еще действуют")
    private val allowPurchaseOverride = false

    init {
        Config.global.registerConfigObject(this)
        registerCommand()
    }

    private fun registerCommand() {
        CommandAPICommand("effects")
            .withSubcommand(ReloadCommand(plugin)())
            .withSubcommand(ApplyCommand(effectManager, playerManager)())
            .withSubcommand(ClearCommand(effectManager, playerManager)())
            .withSubcommand(PurchaseCommand(effectManager, playerManager, allowPurchaseOverride)())
            .register(plugin)
    }

}
