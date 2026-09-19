package me.wyne.storm.command

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import me.wyne.wutils.config.Config
import me.wyne.wutils.config.ConfigEntry
import me.wyne.storm.Storm
import me.wyne.storm.effect.StormEffectManager
import me.wyne.storm.player.PlayerEffectManager

@Singleton
class StormCommand @Inject constructor(
    private val plugin: Storm,
    private val effectManager: StormEffectManager,
    private val playerManager: PlayerEffectManager
) {

    @ConfigEntry(section = "Commands", comment = "If true, allows purchasing effects even while they are still active")
    private var allowPurchaseOverride = false

    init {
        Config.global.registerConfigObject(this)
        registerCommand()
    }

    private fun registerCommand() {
        CommandAPICommand("effects")
            .withSubcommand(ReloadCommand(plugin)())
            .withSubcommand(ApplyCommand(effectManager, playerManager)())
            .withSubcommand(ClearCommand(effectManager, playerManager)())
            .withSubcommand(PurchaseCommand(effectManager, playerManager) { allowPurchaseOverride }())
            .withSubcommand(PurchaseManyCommand(effectManager, playerManager) { allowPurchaseOverride }())
            .register(plugin)
    }

}
